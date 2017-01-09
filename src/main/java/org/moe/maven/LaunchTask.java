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
 * @goal launch
 * @execute phase="compile"
 */
public class LaunchTask extends BuildGradleTask {

    public static final String DEVICES_KEY = "-Pmoe.launcher.devices=";

    public static final String SIMULATORS_KEY = "-Pmoe.launcher.simulators=";

    public static final String OPTIONS_KEY = "-Pmoe.launcher.options=";

    /**
     * @parameter expression="${moe.devices}"
     */
    protected String[] deviceIds;

    /**
     * @parameter expression="${moe.simulators}"
     */
    protected String[] simulatorIds;

    /**
     * @parameter expression="${moe.options}"
     */
    protected String[] options;
    
    /**
     * @parameter expression="${moe.noinstall.ontarget}"
     */
    private String noInstallOnTarget;

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

    protected void addLaunchArguments() {
        Vector<String> newArgs = new Vector<String>();

        if (args != null && args.length > 0) {
            for (String arg : args) {
                newArgs.add(arg);
            }
        }

        if (deviceIds != null && deviceIds.length > 0) {
            StringBuilder devices = new StringBuilder();
            devices.append(DEVICES_KEY);
            for (String deviceId : deviceIds) {
                devices.append(deviceId);
                devices.append(",");
            }
            devices.deleteCharAt(devices.length() - 1);
            String deviceIdsParam = devices.toString();
            newArgs.add(deviceIdsParam);
            getLog().info("Device ids: " + deviceIdsParam);
        }

        if (simulatorIds != null && simulatorIds.length > 0) {
            StringBuilder simulators = new StringBuilder();
            simulators.append(SIMULATORS_KEY);
            for (String simulatorId : simulatorIds) {
                simulators.append(simulatorId);
                simulators.append(",");
            }
            simulators.deleteCharAt(simulators.length() - 1);
            String simulatorIdsParam = simulators.toString();
            newArgs.add(simulatorIdsParam);
            getLog().info("Simulator ids: " + simulatorIdsParam);
        }
        
        OptionsBuilder optionsBuilder = null;
        
        boolean isNoInstallOnTarget =
        		noInstallOnTarget == null || noInstallOnTarget.isEmpty() ? false : Boolean.parseBoolean(noInstallOnTarget);
        
        if (isNoInstallOnTarget) {
        	optionsBuilder = new OptionsBuilder();
        	optionsBuilder.push("no-install-on-target");
        }

        if (options != null && options.length > 0) {
        	if (optionsBuilder == null) {
        		optionsBuilder = new OptionsBuilder();
        	}
            for (String option : options) {
                optionsBuilder.push(option);
            }
        }
        
        if (optionsBuilder != null) {
        	String optionsParam = optionsBuilder.toString();
            newArgs.add(optionsParam);
            getLog().info("Options: " + optionsParam);
        }

        args = new String[newArgs.size()];
        newArgs.toArray(args);
    }

    protected boolean isPrintTask() {
        return true;
    }

}
