package org.jboss.drools.guvnor.importgenerator.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jboss.drools.guvnor.importgenerator.Configuration;
import org.jboss.drools.guvnor.importgenerator.ImportFileGenerator;

/**
 * @goal generate
 */
public class ImportFileGeneratorMojo extends AbstractMojo implements Configuration{
  /** @parameter expression="${generate.debug}" */
  private String debug="true";
  /** @parameter expression="${generate.debugExtra}" */
  private String debugExtra="true";
  /** @parameter expression="${generate.path}" */
  private String path="rules";
  /** @parameter expression="${generate.packageExclude}" */
  private String packageExclude="[0-9|.]*[.|-]+[SNAPSHOT]+[.|-]*[09|.]*";
  /** @parameter expression="${generate.recursive}" */
  private String recursive="true";
  /** @parameter expression="${generate.creator}" */
  private String creator="admin";
  /** @parameter expression="${generate.fileExtensions}" */
  private String fileExtensions="drl,xls,bpmn";
  /** @parameter expression="${generate.outputFile}" */
  private String outputFile="generated.xml";
  /** @parameter expression="${generate.snapshotName}" */
  private String snapshotName="1.0.0-SNAPSHOT";
  /** @parameter expression="${generate.functionFileName}" */
  private String functionFileName="functions.txt";
  /** @parameter expression="${generate.kagentChangeSetServer}" */
  private String kagentChangeSetServer="http://localhost:8080/org.drools.guvnor.Guvnor/package/";
  /** @parameter expression="${generate.kagentChangeSetFile}" */
  private String kagentChangeSetFile="kagent-changeset.xml";
  /** @parameter expression="${generate.models}" */
  private Artifact[] models;
  private List<File> modelFiles=new ArrayList<File>();
  
  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;
  
   /** @parameter expression=
   *  "${component.org.apache.maven.artifact.factory.ArtifactFactory}"
   * @required
   * @readonly
   */
  protected ArtifactFactory factory;
   /** @parameter expression=
   *  "${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
   * @required
   * @readonly
   */
  protected ArtifactResolver artifactResolver;
   /** @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected List remoteRepositories;
   /** @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected ArtifactRepository localRepository;
  
  public static void main(String[] args) throws Throwable{
    ImportFileGeneratorMojo mojo=new ImportFileGeneratorMojo();
    mojo.debug="true";
    mojo.creator="admin";
    mojo.modelFiles=new ArrayList<File>();
    mojo.modelFiles.add(new File("/home/mallen/.m2/repository/commons-io/commons-io/1.4/commons-io-1.4.jar"));
    mojo.fileExtensions="drl,xls,bpnm2";
    mojo.outputFile="target/guvnor-import.xml";
    mojo.packageExclude="[0-9|.]*[.|-]+[SNAPSHOT]+[.|-]*[09|.]*";
    mojo.snapshotName="1.0.0-SNAPSHOT";
    mojo.path=System.getProperty("user.dir")+"/rules1";
//    mojo.functionFileName=null;
    new ImportFileGenerator().run(mojo);
  }
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    addProjectDependenciesToClasspath(project);
    
    System.out.println("models="+models);
    System.out.println("models.count="+models.length);
    for(Artifact a:models){
      System.out.println("model="+a);
    }
    
    ArtifactDownloader downloader=new ArtifactDownloader(factory, artifactResolver, remoteRepositories, localRepository);
    downloader.setDebug(getDebug());
    for(Artifact artifact:models){
      try{
        File sourceFile=downloader.resolve(artifact);
        File targetModelFolder=new File(project.getBuild().getDirectory(), "models");
        targetModelFolder.mkdirs();
        
        File targetFile=new File(targetModelFolder, sourceFile.getName());
        IOUtils.copy(new FileInputStream(sourceFile), new FileOutputStream(targetFile));
        modelFiles.add(targetFile);
      }catch(IOException e){
        getLog().error("Unable to obtain the model ["+artifact+"]", e);
      }catch (ArtifactResolutionException e) {
        getLog().error("Unable to obtain the model ["+artifact+"]", e);
      }catch (ArtifactNotFoundException e) {
        getLog().error("Unable to obtain the model ["+artifact+"]", e);
      }
    }
    
    new ImportFileGenerator().run(this);
  }
  
  
  private void addProjectDependenciesToClasspath(MavenProject project) {
    try {
      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
      ClassLoader newClassLoader = new ProjectClasspath().getClassLoader(project, oldClassLoader, getLog());
      Thread.currentThread().setContextClassLoader(newClassLoader);
    } catch (DependencyResolutionRequiredException e) {
      getLog().info("Skipping addition of project artifacts, there appears to be a dependecy resolution problem", e);
    }
  }

  public boolean getDebug() {return debug!=null && debug.trim().toLowerCase().equals("true");}
  public boolean getDebugExtra() {return debugExtra!=null && debugExtra.trim().toLowerCase().equals("true");}
  public String getPath() {return path;}
  public String getPackageExclude() {return packageExclude;}
  public String getRecursive() {return recursive;}
  public String getCreator() {return creator;}
  public String getFileExtensions() {return fileExtensions;}
  public String getOutputFile() {return outputFile;}
  public String getSnapshotName() {return snapshotName;}
  public String getFunctionFileName() {return functionFileName;}
  public String getKagentChangeSetServer() {return kagentChangeSetServer;}
  public String getKagentChangeSetFile() {return kagentChangeSetFile;}
  public List<File> getModelFiles() {return modelFiles;}
}
