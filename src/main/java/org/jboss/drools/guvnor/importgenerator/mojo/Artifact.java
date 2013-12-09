package org.jboss.drools.guvnor.importgenerator.mojo;

public class Artifact {
  private String groupId;
  private String artifactId;
  private String version;
  private String type;
  public String toString(){
    return this.getClass().getName()+"["+getGroupId()+":"+getArtifactId()+":"+getVersion()+":"+getType()+"]";
  }
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  public String getArtifactId() {
    return artifactId;
  }
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  
  
}
