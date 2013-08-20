package org.jboss.drools.guvnor.importgenerator.mojo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Represents the classpath built from a maven project's dependencies.
 */
public class ProjectClasspath {

    /**
     * Provides a class loader that can be used to load classes from this
     * project classpath.
     * 
     * @param parent
     *            a classloader which should be used as the parent of the newly
     *            created classloader.
     * @param log
     *            object to which details of the found/loaded classpath elements
     *            can be logged.
     * 
     * @return a classloader that can be used to load any class that is
     *         contained in the set of artifacts that this project classpath is
     *         based on.
     * @throws DependencyResolutionRequiredException
     *             if maven encounters a problem resolving project dependencies
     */
    public ClassLoader getClassLoader(MavenProject project, final ClassLoader parent, Log log) throws DependencyResolutionRequiredException {

        @SuppressWarnings("unchecked")
        List<String> classpathElements = project.getCompileClasspathElements();

        final List<URL> classpathUrls = new ArrayList<URL>(classpathElements.size());

        for (String classpathElement : classpathElements) {

            try {
                log.debug("Adding project artifact to classpath: " + classpathElement);
                classpathUrls.add(new File(classpathElement).toURI().toURL());
            } catch (MalformedURLException e) {
                log.debug("Unable to use classpath entry as it could not be understood as a valid URL: " + classpathElement, e);
            }

        }

        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
          public ClassLoader run() {
            return new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]), parent);
          }});

    }

}
