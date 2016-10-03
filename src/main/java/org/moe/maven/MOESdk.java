package org.moe.maven;

import java.io.File;

public class MOESdk {
	
	private static File junitJar;
	
	public static void setJunitJar(File jar) {
		MOESdk.junitJar = jar;
	}
	
	public static File getJunitJar() {
		return MOESdk.junitJar;
	}

}
