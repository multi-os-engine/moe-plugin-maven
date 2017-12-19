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
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public abstract class BuildGradleTask extends GradleTask {

    public static final String MOE_PROGUARD_INJARS_KEY = "-Pmoe.proguard.injars=";

    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    protected File outputDirectory;
    
    /**
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    protected File testOutputDirectory;

    @Parameter(defaultValue = "${session}", readonly = true, required = true) private MavenSession session;

    /**
     * @parameter expression=
     * "${component.org.apache.maven.shared.dependency.graph.DependencyGraphBuilder}"
     * @required
     * @readonly
     */
    private DependencyGraphBuilder dependencyGraphBuilder;
    
    /**
     * @parameter expression="${moe.gradle.log.level}"
     */
    protected String gradleLogLevel;
    
    /**
     * @parameter expression="${moe.gradle.stacktrace.level}"
     */
    protected String gradleStacktraceLevel;

    @Override
    protected void addArguments() {
        addInjars();
        super.addArguments();
    }

    protected void addInjars() {
        Vector<String> newArgs = new Vector<String>();

        if (args != null && args.length > 0) {
            for (String arg : args) {
                newArgs.add(arg);
            }
        }
        
        if (gradleLogLevel != null) {
        	newArgs.add(gradleLogLevel);
        }
        
        if (gradleStacktraceLevel != null) {
        	newArgs.add(gradleStacktraceLevel);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MOE_PROGUARD_INJARS_KEY);
        stringBuilder.append(outputDirectory.getAbsolutePath());
        addTestOutputDirectory(stringBuilder);

        List<File> dependencies = getDependencies();

        if (dependencies != null && dependencies.size() > 0) {
            stringBuilder.append(File.pathSeparator);

            for (File file : dependencies) {
                stringBuilder.append(file.getAbsolutePath());
                stringBuilder.append(File.pathSeparator);
            }

            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        
        addSDKJars(stringBuilder);

        String injarsParam = stringBuilder.toString();
        newArgs.add(injarsParam);
        getLog().info("Injars: " + injarsParam);

        args = new String[newArgs.size()];
        newArgs.toArray(args);
    }

	protected List<File> getDependencies() {
        List<File> dependenciesList = null;
        if (project != null) {
            dependenciesList = new ArrayList<File>();

            Set<Artifact> artifacts = getAllDependencies();

            for (Artifact artifact : artifacts) {
                if (artifact.getArtifactId().equals(SetupSDKTask.MOE_SDK_PLATFORM_JAR) || artifact.getArtifactId()
                        .equals(SetupSDKTask.MOE_SDK_CORE_JAR) || artifact.getArtifactId()
                        .equals(SetupSDKTask.MOE_SDK_JUNIT_JAR) || artifact.getArtifactId()
                        .equals(SetupSDKTask.MOE_SDK_JAVA8SUPPORT_JAR)) {

                    continue;
                }
                if (!artifact.isResolved()) {

                    try {
                        artifactResolver.resolve(artifact, remoteRepositories, localRepository);
                    } catch (ArtifactResolutionException e) {
                        getLog().error(e);
                    } catch (ArtifactNotFoundException e) {
                        getLog().error(e);
                    }
                }

                File artifactFile = artifact.getFile();
                if (artifactFile != null) {
                    dependenciesList.add(artifactFile);
                }
            }
        } else {
            getLog().info("Project null error");
        }

        return dependenciesList;
    }

    private Set<Artifact> getAllDependencies() {
        Set<Artifact> dependencies = null;
        try {
            DependencyNode rootNode = dependencyGraphBuilder.buildDependencyGraph(project, null);
            dependencies = getAllDescendants(rootNode);
        } catch (DependencyGraphBuilderException e) {
            getLog().error("Unable resolve dependencies", e);
        }
        return dependencies;
    }

    private Set<Artifact> getAllDescendants(DependencyNode node) {
        Set<Artifact> children = null;
        if (node.getChildren() != null) {
            children = new HashSet<Artifact>();
            for (DependencyNode depNode : node.getChildren()) {
                children.add(depNode.getArtifact());
                Set<Artifact> subNodes = getAllDescendants(depNode);
                if (subNodes != null) {
                    children.addAll(subNodes);
                }
            }
        }
        return children;
    }
    
    protected void addSDKJars(StringBuilder stringBuilder) {
    	
    }
    
    protected void addTestOutputDirectory(StringBuilder stringBuilder) {
		// TODO Auto-generated method stub
		
	}

}
