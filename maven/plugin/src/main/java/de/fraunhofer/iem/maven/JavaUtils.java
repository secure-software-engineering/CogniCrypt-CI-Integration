package de.fraunhofer.iem.maven;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

class JavaUtils {

	static Optional<Integer> javaVersion = Optional.empty();

	public static int getJavaVersion() {
		if (!javaVersion.isPresent()){
			String versionString = System.getProperty("java.version");
			if(versionString.startsWith("1.")) {
				versionString = versionString.substring(2, 3);
			} else {
				int dot = versionString.indexOf(".");
				if(dot != -1) {
					versionString = versionString.substring(0, dot);
				}
			}
			int version = Integer.parseInt(versionString);
			javaVersion = Optional.ofNullable(version);
		}
		return javaVersion.get();
	}

	public static boolean isModularProject(File classPath) {
		Path moduleClassPath = Paths.get(classPath.getAbsolutePath(), "module-info.class");
		return moduleClassPath.toFile().exists();
	}

	public static File getJavaRuntimePath(){
		Path subPath;
		if(JavaUtils.getJavaVersion() < 9){
			subPath = Paths.get("lib", "rt.jar");
		} else{
			subPath = Paths.get("lib");
		}
		String javaHome = System.getProperty("java.home");
		return new File(javaHome, subPath.toString());
	}

}
