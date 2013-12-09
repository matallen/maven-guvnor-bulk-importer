package org.jboss.drools.guvnor.importgenerator;

import java.io.File;
import java.util.List;

public interface Configuration{
  public boolean getDebug();
  public boolean getDebugExtra();
  public String getPath();
  public String getPackageExclude();
  public String getRecursive();
  public String getCreator();
  public String getFileExtensions();
  public String getOutputFile();
  public String getSnapshotName();
  public String getFunctionFileName();
  public String getKagentChangeSetServer();
  public String getKagentChangeSetFile();
//  public String getModelFile();
  public List<File> getModelFiles();
}
