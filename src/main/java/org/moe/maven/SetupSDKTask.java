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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @goal setupSDK
 */
public class SetupSDKTask extends GradleTask {

    public static final String MOE_SDK_CORE_JAR = "moe.sdk.coreJar";
    public static final String MOE_SDK_PLATFORM_JAR = "moe.sdk.platformJar";
    public static final String MOE_SDK_JUNIT_JAR = "moe.sdk.junitJar";

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression=
     * "${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    protected ArtifactFactory factory;

    /**
     * @parameter
     */
    protected String moeSdkLocalbuild;

    private String coreJarPath;

    private String platformJarPath;

    private String junitJarPath;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        super.execute();

        if (project != null) {

            Artifact moe_ios = factory.createArtifact("moe.sdk", MOE_SDK_PLATFORM_JAR, "1.0", "system", "jar");
            moe_ios.setFile(new File(platformJarPath));

            try {
                artifactResolver.resolve(moe_ios, remoteRepositories, localRepository);
            } catch (ArtifactResolutionException e) {
                getLog().error(e);
            } catch (ArtifactNotFoundException e) {
                getLog().error(e);
            }

            Artifact moe_core = factory.createArtifact("moe.sdk", MOE_SDK_CORE_JAR, "1.0", "system", "jar");
            moe_core.setFile(new File(coreJarPath));

            try {
                artifactResolver.resolve(moe_core, remoteRepositories, localRepository);
            } catch (ArtifactResolutionException e) {
                getLog().error(e);
            } catch (ArtifactNotFoundException e) {
                getLog().error(e);
            }

            Artifact moe_junit = factory.createArtifact("moe.sdk", MOE_SDK_JUNIT_JAR, "1.0", "system", "jar");
            File junitJar = new File(junitJarPath);
            moe_junit.setFile(junitJar);
            MOESdk.setJunitJar(junitJar);

            try {
                artifactResolver.resolve(moe_junit, remoteRepositories, localRepository);
            } catch (ArtifactResolutionException e) {
                getLog().error(e);
            } catch (ArtifactNotFoundException e) {
                getLog().error(e);
            }

            Set<Artifact> projectDependencies = new HashSet<Artifact>();
            projectDependencies.addAll(project.getDependencyArtifacts());

            projectDependencies.add(moe_ios);
            projectDependencies.add(moe_core);
            projectDependencies.add(moe_junit);

            project.setDependencyArtifacts(projectDependencies);

        } else {
            getLog().error("PROJECT NULL");
        }
    }

    @Override
    protected String[] tasks() {
        return new String[] { "moeSDKProperties" };
    }

    @Override
    protected void readOutput(ByteArrayOutputStream baos) {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));
        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(MOE_SDK_PLATFORM_JAR)) {
                    platformJarPath = getValue(line);
                } else if (line.startsWith(MOE_SDK_CORE_JAR)) {
                    coreJarPath = getValue(line);
                } else if (line.startsWith(MOE_SDK_JUNIT_JAR)) {
                    junitJarPath = getValue(line);
                }
            }
        } catch (IOException e) {
            getLog().error("Unable read SDK properties", e);
            ;
        }
    }

    private String getValue(String line) {
        String[] keyValue = line.split("=");
        if (keyValue.length > 1) {
            return keyValue[1];
        }
        return "";
    }

}
