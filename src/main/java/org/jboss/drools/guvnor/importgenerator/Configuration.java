package org.jboss.drools.guvnor.importgenerator;

public interface Configuration{
  public String getDebug();
  public String getDebugExtra();
  public String getPath();
//  public String getPackageStart();
  public String getPackageExclude();
  public String getRecursive();
  public String getCreator();
  public String getFileExtensions();
  public String getOutputFile();
  public String getSnapshotName();
  public String getFunctionFileName();
  public String getKagentChangeSetServer();
  public String getKagentChangeSetFile();
//  public String getBaseDir();
  public String getModelFile();
}
