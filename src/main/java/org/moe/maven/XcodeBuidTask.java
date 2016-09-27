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

import java.util.Vector;

/**
 * @goal xcodebuild
 * @execute phase="compile"
 */
public class XcodeBuidTask extends BuildGradleTask {

    /**
     * @parameter expression="${moe.configuration}"
     */
    private String configuration;

    /**
     * @parameter expression="${moe.remotebuild}"
     */
    private String remotebuild;

    /**
     * @parameter expression="${moe.remotebuild.host}"
     */
    private String host;

    /**
     * @parameter expression="${moe.remotebuild.port}"
     */
    private String port;

    /**
     * @parameter expression="${moe.remotebuild.user}"
     */
    private String user;

    /**
     * @parameter expression="${moe.remotebuild.knownhosts}"
     */
    private String knownhosts;

    /**
     * @parameter expression="${moe.remotebuild.identity}"
     */
    private String identity;

    /**
     * @parameter expression="${moe.remotebuild.keychain.pass}"
     */
    private String keychainPass;

    /**
     * @parameter expression="${moe.remotebuild.keychain.locktimeout}"
     */
    private String locktimeout;

    /**
     * @parameter expression="${moe.remotebuild.gradle.repositories}"
     */
    private String repositories;

    /**
     * @parameter expression="${moe.simulator.udid}"
     */
    private String simulatorUdid;
    
    /**
     * @parameter expression="${moe.gradle.console.info}"
     */
    private String infoMode;
    
    /**
     * @parameter expression="${moe.gradle.console.debug}"
     */
    private String debugMode;
    
    /**
     * @parameter expression="${moe.gradle.console.stacktrace}"
     */
    private String stacktraceMode;

    @Override
    protected String[] tasks() {
        return new String[] { "moeLaunch" };
    }

    @Override
    protected void addArguments() {
        addInjars();
        addLaunchArguments();
        super.addArguments();
    }

    private void addLaunchArguments() {
        Vector<String> newArgs = new Vector<String>();

        if (args != null && args.length > 0) {
            for (String arg : args) {
                newArgs.add(arg);
            }
        }
        
        boolean isInfo =
        		infoMode == null || infoMode.isEmpty() ? false : Boolean.parseBoolean(infoMode);
        if (isInfo) {
        	newArgs.add("--info");
        }
        
        boolean isDegug =
        		debugMode == null || debugMode.isEmpty() ? false : Boolean.parseBoolean(debugMode);
        if (isDegug) {
        	newArgs.add("--debug");
        }
        
        boolean isStrackrace =
        		stacktraceMode == null || stacktraceMode.isEmpty() ? false : Boolean.parseBoolean(stacktraceMode);
        if (isStrackrace) {
        	newArgs.add("--stacktrace");
        }

        if (simulatorUdid != null && !simulatorUdid.isEmpty()) {
            newArgs.add("-Pmoe.launcher.simulators=" + simulatorUdid);
        }

        OptionsBuilder optionsBuilder = new OptionsBuilder();

        optionsBuilder.push("no-launch");

        if (configuration != null && !configuration.isEmpty()) {
            optionsBuilder.push("config:" + configuration);
        }

        boolean isRemoteBuild =
                remotebuild == null || remotebuild.isEmpty() ? false : Boolean.parseBoolean(remotebuild);

        if (isRemoteBuild) {
            newArgs.add("-Pmoe.remotebuild.properties.ignore");

            if (host != null && !host.isEmpty()) {
                newArgs.add("-Pmoe.remotebuild.host=" + host);
            }

            if (port != null && !port.isEmpty()) {
                newArgs.add("-Pmoe.remotebuild.port=" + port);
            }

            if (user != null && !user.isEmpty()) {
                newArgs.add("-Pmoe.remotebuild.user=" + user);
            }

            if (knownhosts != null && !knownhosts.isEmpty()) {
                newArgs.add("-Pmoe.remotebuild.knownhosts=" + knownhosts);
            }

            if (identity != null && !identity.isEmpty()) {
                newArgs.add("-Pmoe.remotebuild.identity=" + identity);
            }

            if (keychainPass != null && !keychainPass.isEmpty()) {
                newArgs.add("-Pmoe.remotebuild.keychain.pass=" + keychainPass);
            }

            if (locktimeout != null && !locktimeout.isEmpty()) {
                newArgs.add("-Pmoe.remotebuild.keychain.locktimeout=" + locktimeout);
            }

            if (repositories != null && !repositories.isEmpty()) {
                newArgs.add("-Pmoe.remotebuild.gradle.repositories=" + repositories);
            }
        }

        String optionsParam = optionsBuilder.toString();
        newArgs.add(optionsParam);

        args = new String[newArgs.size()];
        newArgs.toArray(args);
    }

    protected boolean isPrintTask() {
        return true;
    }

}
