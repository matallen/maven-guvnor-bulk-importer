/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.drools.guvnor.importgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.drools.guvnor.importgenerator.utils.FileIOHelper;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

/**
 * a BRMS import file generator for drl and xml decision table files
 */
public class ImportFileGenerator implements Constants {
    class Logger{
      public void debug(String msg){System.out.println(msg);}
      public void error(String msg, Throwable t){System.err.println(msg); t.printStackTrace();}
      public void trace(String msg){System.out.println(msg);}
    }
    protected final Logger logger=new Logger();
    private Configuration options=null;
    private String BASE_DIR = System.getProperty("user.dir");

    public enum PackageObjectType {PACKAGE, PACKAGE_SNAPSHOT, MODEL}

    public enum RuleObjectType {RULE, SNAPSHOT_RULE}

    private Map<String, PackageFile> packages;
    private void setPackages(Map<String, PackageFile> packages){
      this.packages=packages;
    }
    
    private String generateImportFile() throws IOException {
        // go through each replacer definition creating drl template replacements
        //TODO: what is the org.drools.io.RuleSetReader ??? is this what Guvnor uses this to read the .drl file parts?
        String draftStateReferenceUUID = GeneratedData.generateUUID();
        String categoryReferenceUUID = GeneratedData.generateUUID();
        
        //reporting only
        int cok = 0, terror = 0, total = 0;

        StringBuffer packageContents = new StringBuffer();
        StringBuffer snapshotContents = new StringBuffer();
        double i = 0;
        for (Map.Entry<String, PackageFile> packagesEntry : packages.entrySet()) {
            String packageName = packagesEntry.getKey();
            double pct = (int) (++i / (double) packages.size() * 100);
            logger.debug(new DecimalFormat("##0").format(pct) + "% - " + packageName);
            PackageFile packageFile = packagesEntry.getValue();

            Map<String, Object> context = new HashMap<String, Object>();
            context.put("draftStateReferenceUUID", draftStateReferenceUUID);
            context.put("categoryReferenceUUID", categoryReferenceUUID);
            context.put("packageFile", packageFile);

            //extract the rule contents
            StringBuffer ruleContents = new StringBuffer();
            StringBuffer snapshotRuleContents = new StringBuffer();
            Map<String, Rule> rules = packageFile.getRules();
            packageFile.buildPackage();

            for (Map.Entry<String, Rule> rulesEntry : rules.entrySet()) {
//                String ruleName = rulesEntry.getKey();
                Rule rule = (Rule) rulesEntry.getValue();
                context.put("file", rule.getFile());
                context.put("rule", rule);
                String format = FilenameUtils.getExtension(rule.getFile().getName());
                context.put("format", format);
                //inject the rule values into the rule template
                ruleContents.append(MessageFormat.format(readTemplate(MessageFormat.format(TEMPLATES_RULE, format)), getRuleObjects(context/*, RuleObjectType.RULE*/)));

                //inject the snapshot rule values in the the snapshot rule template
                snapshotRuleContents.append(MessageFormat.format(readTemplate(MessageFormat.format(TEMPLATES_SNAPSHOT_RULE, format)), getRuleObjects(context/*, RuleObjectType.SNAPSHOT_RULE*/)));
            }

            String modelTemplate = readTemplate(TEMPLATES_MODEL);
            for (Model model : packageFile.getModelFiles()) {
                context.put("model", model);
                ruleContents.append(MessageFormat.format(modelTemplate, getPackageObjects(context, new StringBuffer(model.getContent()), PackageObjectType.MODEL)));
            }
            // If no models in directory but parameter specified then upload the parameterized model
            if (packageFile.getModelFiles().size() <= 0 && options.getModelFile() != null) {
                File modelFile = new File(options.getModelFile());
                String modelFileContent = FileIOHelper.readAllAsBase64(modelFile);
                context.put("model", new Model(modelFile, modelFileContent));
                ruleContents.append(MessageFormat.format(modelTemplate, getPackageObjects(context, new StringBuffer(modelFileContent), PackageObjectType.MODEL)));
            }

            //inject the rule(s) into the package into the package contents
            String packageTemplate = readTemplate(TEMPLATES_PACKAGE);// FileUtils.readAll(new FileInputStream(new File(TEMPLATES_FOLDER, TEMPLATES_PACKAGE)));
            packageContents.append(MessageFormat.format(packageTemplate, getPackageObjects(context, ruleContents, PackageObjectType.PACKAGE)));

            //inject the snapshot values into the snapshot contents
            if (options.getSnapshotName() != null) {
                snapshotContents.append(MessageFormat.format(readTemplate(TEMPLATES_SNAPSHOT), getPackageObjects(context, snapshotRuleContents, PackageObjectType.PACKAGE_SNAPSHOT)));
            }

            //display status of each packageFile
            total++;
            if (packageFile.hasErrors()) {
                terror++;
                if (packageFile.hasCompilationErrors()) {
//                    cerror++;
                    logger.debug(" - [COMPILATION/DEPENDENCY ERRORS]");
                    logger.trace(packageFile.getCompilationErrors().trim());
                    logger.trace(packageFile.getDependencyErrors().trim());
                } else if (packageFile.hasDependencyErrors()) {
//                    derror++;
                    logger.debug(" - [DEPENDENCY ERRORS]");
                    logger.trace(packageFile.getDependencyErrors().trim());
                }
            } else {
                cok++; //increment the "total rules compiled successfully"
                logger.debug(" - [OK]");
            }
        }

        //replace the placemarkers with the package data
        String parentContents = MessageFormat.format(readTemplate(TEMPLATES_PARENT), new Object[]{
                packageContents
                , categoryReferenceUUID
                , draftStateReferenceUUID
                , GeneratedData.getTimestamp()
                , getSnapshotContents(snapshotContents)
        });

        //write a summary report
        logger.debug("==========================");
        logger.debug("===  PACKAGE SUMMARY   ===");
        logger.debug("==========================");
        logger.debug(" Rules compiled OK:   " + NumberFormat.getInstance().format(cok));
        logger.debug(" Errors:              " + NumberFormat.getInstance().format(terror));
        logger.debug("                      ____");
        logger.debug(" Total:               " + NumberFormat.getInstance().format(total));
        logger.debug("==========================");

        return parentContents;
    }

    /**
     * returns a drools-5.0 formatted xml file for use with a drools 5.0 knowledge agent
     *
     * @param packages
     * @return
     */
    public String generateKnowledgeAgentInitFile(Map<String, PackageFile> packages) {
        StringBuffer kagentInitContents = new StringBuffer();
        String kagentChildTemplate = readTemplate(TEMPLATES_KAGENT_CHILD_INIT);
        StringBuffer kagentChildContents = new StringBuffer();
        for (Map.Entry<String, PackageFile> packagesEntry : packages.entrySet()) {
            String packageName = packagesEntry.getKey();
            PackageFile packageFile = packagesEntry.getValue();
            kagentChildContents.append(MessageFormat.format(kagentChildTemplate,
                    new Object[]{options.getKagentChangeSetServer(),
                            packageFile.getName() + "/" + options.getSnapshotName(), "PKG"}));
        }
        String kagentParentTemplate = readTemplate(TEMPLATES_KAGENT_PARENT_INIT);
        kagentInitContents.append(MessageFormat.format(kagentParentTemplate, new Object[]{kagentChildContents.toString()}));
        return kagentInitContents.toString();
    }


    private StringBuffer getSnapshotContents(StringBuffer snapshotContents) {
        if (options.getSnapshotName() != null) {
            return snapshotContents;
        }
        return new StringBuffer("");
    }

    private String readTemplate(String templateConst) {
        String path = TEMPLATES_FOLDER + "/" + templateConst;
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(path);
            if (null != in) {
                return IOUtils.toString(in);
            } else {
                File file = new File(new File(BASE_DIR, TEMPLATES_FOLDER), templateConst);
                try {
                    return FileUtils.readFileToString(file);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Error reading file (" + file +")", e);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem reading path (" + path + ").", e);
        }
    }

    private Object[] getPackageObjects(Map<String, Object> context, StringBuffer contents, PackageObjectType type) throws IOException {
        List<String> objects = new LinkedList<String>();
        PackageFile packageFile = (PackageFile) context.get("packageFile");
        switch (type) {
            case MODEL:
                Model model = (Model) context.get("model");
                objects.add(model.getFile().getName().substring(0, model.getFile().getName().lastIndexOf(".")));//wrapper title
                objects.add(getCreator());//creator
                objects.add(contents.toString());// packageFile.getModelAsBase64());//content
                objects.add(GeneratedData.generateUUID());//uuid
                objects.add(model.getFile().getName());//filename
                objects.add((String) context.get("draftStateReferenceUUID"));//state
                objects.add(GeneratedData.getTimestamp());//timestamp
                objects.add(packageFile.getName()); //package name
                break;

            case PACKAGE:
                objects.add(packageFile.getName());
                objects.add(getCreator());
                objects.add(packageFile.getImports());
                objects.add(contents.toString());
                objects.add(GeneratedData.generateUUID());
                objects.add(GeneratedData.generateUUID());
                objects.add(GeneratedData.generateUUID());
                objects.add((String) context.get("draftStateReferenceUUID"));
                objects.add(GeneratedData.getTimestamp());
                break;
            case PACKAGE_SNAPSHOT:
                objects.add(packageFile.getName());
                objects.add(packageFile.getName().substring(packageFile.getName().lastIndexOf(".") + 1));// //aka the title
                objects.add(options.getSnapshotName());
                objects.add(getCreator()); //3
                objects.add(packageFile.getImports()); //4
                objects.add(contents.toString()); //5
                objects.add((String) context.get("draftStateReferenceUUID"));
                objects.add(GeneratedData.getTimestamp()); //7
                //objects.add(FileIOHelper.toBase64(DroolsHelper.compileRuletoPKG(packageFile))); //8
                objects.add(FileIOHelper.toBase64(packageFile.toByteArray()));
                objects.add(GeneratedData.generateUUID()); //snapshot uuid
                objects.add(GeneratedData.generateUUID()); //snapshot base+predecessor uuid
                objects.add(GeneratedData.generateUUID()); //assets uuid
                objects.add(GeneratedData.generateUUID()); //assets base+predecessor uuid
                objects.add(GeneratedData.generateUUID()); //drools uuid
                objects.add(GeneratedData.generateUUID()); //drools base+predecessor uuid
                break;
        }
        return objects.toArray(new Object[objects.size()]);
    }


    private Object[] getRuleObjects(Map<String, Object> context/*, RuleObjectType type*/) {
        List<String> objects = new LinkedList<String>();
        PackageFile packageFile = (PackageFile) context.get("packageFile");
        Rule rule = (Rule) context.get("rule");

        objects.add(rule.getRuleName());
        objects.add(packageFile.getName());
        objects.add(rule.getContent());
        objects.add(GeneratedData.generateUUID()); //rule uuid
        objects.add((String) context.get("draftStateReferenceUUID"));
        objects.add((String) context.get("categoryReferenceUUID"));
        objects.add(getCreator());
        objects.add(GeneratedData.getTimestamp());
        objects.add((String) context.get("format"));
        objects.add(GeneratedData.generateUUID()); //base version + predecessor (currently only used in snapshot)
        if ("xls".equalsIgnoreCase((String) context.get("format"))) {
            objects.add(((File) context.get("file")).getName());
        }
        return objects.toArray(new Object[]{});
    }


    private String getCreator() {
        if (options.getCreator() != null) {
            return options.getCreator();
        }
        return DEFAULT_CREATOR;
    }

    public void run(Configuration options) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date startd = new Date();
            DateTime start = new DateTime(startd);
            this.options = options;
//            BASE_DIR = options.getBaseDir();
            logger.debug("Running BRMS Import Generator (started " + fmt.format(startd) + "):");

            logger.debug("Scanning directories...");
            Map<String, PackageFile> details = PackageFile.buildPackages(options);

            logger.debug("Generating 'Guvnor import data'...");
            setPackages(details);
            String guvnorImport = generateImportFile();
            File guvnorImportFile = getFile(options.getOutputFile());
            logger.debug("Writing 'Guvnor import data to disk' (" + guvnorImportFile.getAbsolutePath() + ")");
            IOUtils.write(guvnorImport.getBytes(), new FileOutputStream(guvnorImportFile));

            if (options.getKagentChangeSetFile() != null) {
                logger.debug("Generating 'Knowledge agent changeset' data...");
                String kagentChangeSet = generateKnowledgeAgentInitFile(details);
                File kagentChangeSetFile = getFile(options.getKagentChangeSetFile());
                logger.debug("Writing 'Knowledge agent changeset' to disk (" + kagentChangeSetFile.getAbsolutePath() + ")");
                IOUtils.write(kagentChangeSet.getBytes(), new FileOutputStream(kagentChangeSetFile));
            }

            DateTime end = new DateTime(System.currentTimeMillis());
            int m = Minutes.minutesBetween(start, end).getMinutes();
            int s = Seconds.secondsBetween(start, end).getSeconds() - (m * 60);
            logger.debug("Finished in (" + m + "m" + s + "s)");
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private File getFile(String fileLoc) {
        if (fileLoc.startsWith("/") || fileLoc.startsWith("~")) {
            return new File(fileLoc);
        } else {
            return new File(BASE_DIR, fileLoc);
        }
    }

}
