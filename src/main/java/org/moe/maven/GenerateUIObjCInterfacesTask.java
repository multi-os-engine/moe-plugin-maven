package org.moe.maven;

/**
 * @goal generateUIObjCInterfaces
 * @execute phase="compile"
 */
public class GenerateUIObjCInterfacesTask extends BuildGradleTask {

	@Override
	protected String[] tasks() {
		return new String[] { "moeGenerateUIObjCInterfaces" };
	}

}
