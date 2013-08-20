package org.jboss.drools.guvnor.importgenerator.mojo;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
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
  /** @parameter expression="${generate.modelFile}" */
  private String modelFile=null; // TODO: improve so you can provide mvn url
  
  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    addProjectDependenciesToClasspath(project);
    new ImportFileGenerator().run(this);
  }
  
  
  private void addProjectDependenciesToClasspath(MavenProject project) {
    try {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newClassLoader = new ProjectClasspath().getClassLoader(project, oldClassLoader, getLog());
        Thread.currentThread().setContextClassLoader(newClassLoader);
    } catch (DependencyResolutionRequiredException e) {
        getLog().info("Skipping addition of project artifacts, there appears to be a dependecy resolution problem",e);
    }
}

  public String getDebug() {return debug;}
  public String getDebugExtra() {return debugExtra;}
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
  public String getModelFile() {return modelFile;}
}
