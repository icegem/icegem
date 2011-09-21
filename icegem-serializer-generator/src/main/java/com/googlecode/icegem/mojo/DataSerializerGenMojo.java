/*
 * Icegem, Extensions library for VMWare vFabric GemFire
 * 
 * Copyright (c) 2010-2011, Grid Dynamics Consulting Services Inc. or third-party  
 * contributors as indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  
 * 
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License v3, as published by the Free Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * You should have received a copy of the GNU Lesser General Public License v3
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.googlecode.icegem.mojo;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.googlecode.icegem.serialization.AutoSerializable;
import com.googlecode.icegem.serialization.HierarchyRegistry;

/**
 * Goal which gen DataSerializers for @AutoSerializable objects
 * 
 * @goal generate
 * @phase process-classes
 * @requiresDependencyResolution
 * @requiresProject
 */
public class DataSerializerGenMojo extends AbstractMojo {
    private final static Logger logger = Logger.getLogger(DataSerializerGenMojo.class.getName()); // todo:
                                                                                                  // java.util.logging
                                                                                                  // or other?
    /**
     * Location of the output dir for DataSerializer.
     * 
     * @parameter default-value="target/classes"
     * @required
     */
    private String outputDirectory;

    /**
     * @parameter default-value="target/classes"
     * @required
     */
    private String classLocation;

    /**
     * Project classpath.
     * 
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> projectClasspathElements;

    /**
     * The source directories containing the sources .
     * 
     * @parameter default-value="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> compileSourceRoots;

    /**
     * path to packages where data model exists
     * 
     * @parameter
     * @required
     */
    private List<String> scanPackages;

    /**
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    public void execute() throws MojoExecutionException {
        ClassLoader mojoLoader = null;
        try {
            URL[] urls = new URL[project.getCompileClasspathElements().size()];
            for (int i = 0; i < project.getCompileClasspathElements().size(); i++) {
                urls[i] = new File(projectClasspathElements.get(i)).toURI().toURL();
            }
            mojoLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Class<?>> classesFromPackages = new ArrayList<Class<?>>();
        List<Class<?>> registeredClasses = new ArrayList<Class<?>>();

        for (String pack : scanPackages) {
            String packPath = project.getBasedir() + "/" + classLocation + "/" + pack.replaceAll("\\.", "/");
            File currentDir = new File(packPath);
            if (!currentDir.exists()) {
                logger.log(Level.WARNING, packPath + " doesn't exist");
                continue;
            }
            File[] files = currentDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".class");
                }
            });
            for (File f : files) {
                try {
                    String className = pack + "." + f.getName().replaceFirst(".class", "");
                    Class<?> clazz = mojoLoader.loadClass(className);
                    if (clazz.getAnnotation(AutoSerializable.class) != null) {
                        classesFromPackages.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        classesFromPackages.addAll(registeredClasses);
        logger.info("to register: " + classesFromPackages);

        try {
            HierarchyRegistry.registerAll(mojoLoader, classesFromPackages, project.getBasedir() + "/" + outputDirectory); // todo: replaced
                                                                                             // classLoader with cl
        } catch (Exception e) {
            final String msg = "Some class from list " + classesFromPackages + " is nor serializable. Cause: "
                    + e.getMessage();
            throw new RuntimeException(msg, e);
        }
    }
}
