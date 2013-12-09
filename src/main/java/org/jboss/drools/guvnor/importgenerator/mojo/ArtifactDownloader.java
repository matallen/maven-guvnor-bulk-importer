package org.jboss.drools.guvnor.importgenerator.mojo;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;

public class ArtifactDownloader {
  protected ArtifactFactory factory;
  protected ArtifactResolver artifactResolver;
  protected List remoteRepositories;
  protected ArtifactRepository localRepository;
  protected boolean debug;
  
  public ArtifactDownloader(ArtifactFactory factory, ArtifactResolver artifactResolver, List remoteRepositories, ArtifactRepository localRepository){
    this.factory=factory;
    this.artifactResolver=artifactResolver;
    this.remoteRepositories=remoteRepositories;
    this.localRepository=localRepository;
  }
  
  public void setDebug(boolean debug){
    this.debug=debug;
  }
  
  public File resolve(Artifact artifact) throws ArtifactResolutionException, ArtifactNotFoundException {
//    try {
      org.apache.maven.artifact.Artifact pomArtifact = factory.createArtifact(
          artifact.getGroupId(), 
          artifact.getArtifactId(), 
          artifact.getVersion(), 
          "", 
          artifact.getType()==null || artifact.getType().equals("")?"jar":artifact.getType() );
      artifactResolver.resolve(pomArtifact, this.remoteRepositories, this.localRepository);
      
      if (debug) System.out.println("Resolved Artifact GAV to ["+pomArtifact.getFile()+"]");
      return pomArtifact.getFile();
      
//    } catch (ArtifactResolutionException e) {
//      getLog().error("can't resolve parent pom", e);
//    } catch (ArtifactNotFoundException e) {
//      getLog().error("can't resolve parent pom", e);
//    }
//    throw new RuntimeException("What is this?");
}
}
