/*
Copyright (C) 2016 Migeran

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.moe.maven;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

public abstract class GradleTask extends AbstractMojo {

    public static String DEFAULT_GRADLE_VERSION = "2.14.1";
    public static final String DISTRIBUTION_URL_KEY = "distributionUrl";

    private volatile boolean completed;
    private GradleConnectionException exception = null;
    private ProjectConnection connection = null;

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @since 1.0-alpha-1
     */
    protected MavenProject project;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression=
     * "${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    protected ArtifactResolver artifactResolver;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    @SuppressWarnings("rawtypes") protected List remoteRepositories;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * @parameter expression="${project.basedir}"
     */
    protected File projectFile;

    /**
     * @parameter
     */
    protected String[] args;

    /**
     * @parameter
     */
    protected String[] jvmArgs;

    /**
     * @parameter
     */
    protected String gradleVersion;

    /**
     * @parameter
     */
    protected File javaHome;

    protected File getProjectFile() {
        return projectFile;
    }

    private BuildLauncher launcher;

    protected abstract String[] tasks();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {

            GradleConnector gradleConnector = GradleConnector.newConnector();

            String useGradleVersion = DEFAULT_GRADLE_VERSION;

            if (gradleVersion != null && !gradleVersion.isEmpty()) {
                useGradleVersion = gradleVersion;
            } else {
                useGradleVersion = getGradleVersion();
                useGradleVersion = useGradleVersion == null ? DEFAULT_GRADLE_VERSION : useGradleVersion;
            }

            gradleConnector.useGradleVersion(useGradleVersion).forProjectDirectory(getProjectFile());

            connection = gradleConnector.connect();

            launcher = connection.newBuild();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (!isPrintTask()) {
                launcher.setStandardOutput(baos);
                launcher.setStandardError(baos);
            } else {
                final ConsolePrinter console = new ConsolePrinter();
                launcher.setStandardOutput(console);
                launcher.setStandardError(console);
            }

            launcher.forTasks(tasks());

            addJVMArguments();

            addArguments();

            if (javaHome != null) {
                launcher.setJavaHome(javaHome);
            }

            launcher.run(new ResultHandler<Void>() {

                @Override
                public void onComplete(Void arg0) {
                    completed = true;
                    if (!isPrintTask()) {
                        readOutput(baos);
                    }
                }

                @Override
                public void onFailure(GradleConnectionException exc) {
                    exception = exc;
                    completed = true;
                    //Print error
                    if (!isPrintTask()) {
                        printErrorOutputToLog(baos);
                    }
                }
            });

            synchronized (this) {
                while (!completed) {
                    try {
                        this.wait(50L);
                    } catch (InterruptedException e) {
                        getLog().error("Interrupt exception");
                    }
                }
            }

            if (exception != null) {
                StringBuilder msg = new StringBuilder();
                Throwable throwable = exception;
                while (throwable != null) {
                    if (throwable.getMessage() != null) {
                        msg.append(throwable.getMessage()).append("\n");
                    }
                    throwable = throwable.getCause();
                }
                throw new MojoFailureException(msg.toString());
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    protected void readOutput(ByteArrayOutputStream baos) {
        try {
            getLog().info("Gradle: ");
            getLog().info(baos.toString("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            getLog().error(e);
        }
    }

    protected void printErrorOutputToLog(ByteArrayOutputStream baos) {
        try {
            getLog().error("Gradle error: ");
            getLog().error(baos.toString("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            getLog().error(e);
        }
    }

    protected void addArguments() {
        if (args != null && args.length > 0) {
            launcher.withArguments(args);
        }
    }

    protected void addJVMArguments() {
        if (jvmArgs != null && jvmArgs.length > 0) {
            launcher.setJvmArguments(jvmArgs);
        }
    }

    protected String getGradleVersion() {
        File projectfile = project.getBasedir();
        File wrapperPropertiesFile = null;
        wrapperPropertiesFile = new File(projectfile, "/gradle/wrapper/gradle-wrapper.properties");
        if (!wrapperPropertiesFile.exists()) {
            wrapperPropertiesFile = new File(projectfile.getParentFile(), "/gradle/wrapper/gradle-wrapper.properties");
        }
        if (!wrapperPropertiesFile.exists()) {
            getLog().error("Unable find gradle wrapper");
            return null;
        }

        FileInputStream inStream = null;
        Properties property;
        try {
            inStream = new FileInputStream(wrapperPropertiesFile);
            property = new Properties();
            property.load(inStream);
        } catch (IOException e) {
            getLog().error("Unable read gradle wrapper", e);
            return null;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ignore) {

                }
            }
        }

        String distUrl = property.getProperty(DISTRIBUTION_URL_KEY);
        if (distUrl == null) {
            getLog().error("Unable read property");
            return null;
        }

        distUrl = distUrl.substring(distUrl.lastIndexOf("/"), distUrl.length());
        String versionString = distUrl.substring(distUrl.indexOf("-") + 1, distUrl.lastIndexOf("-"));

        String versionNumbers[] = versionString.split(".");
        for (String number : versionNumbers) {
            try {
                Integer.valueOf(number);
            } catch (NumberFormatException e) {
                getLog().error("Unable parse gradle version: " + versionString);
                return null;
            }
        }
        getLog().info("Gradle version in wrapper: " + versionString);
        return versionString;
    }

    protected boolean isPrintTask() {
        return false;
    }

    private class ConsolePrinter extends OutputStream {

        private String line = "";

        @Override
        public void write(int b) throws IOException {
            byte[] bytes = new byte[1];
            bytes[0] = (byte)(b & 0xff);
            line = line + new String(bytes);

            if (line.endsWith("\n")) {
                line = line.substring(0, line.length() - 1);
                getLog().info(line);
                line = "";
            }
        }
    }
}
