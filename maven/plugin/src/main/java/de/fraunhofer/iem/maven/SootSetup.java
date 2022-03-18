package de.fraunhofer.iem.maven;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.NullArgumentException;


import soot.EntryPoints;
import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.options.Options;


class SootSetup {

	private final SootSetupData setupData;

	public SootSetup(SootSetupData setupData) {
		if (setupData == null) {
			throw new NullArgumentException("setupData");
		}
		this.setupData = setupData;
	}

	public void run(){
		setupSootScene();
	}

	private void setupSootScene() {
		G.v().reset();
		Options.v().set_whole_program(true);

		switch (setupData.getCallgraph()) {
			case "cha":
				Options.v().setPhaseOption("cg.cha", "on");
				break;
			case "spark-library":
				Options.v().setPhaseOption("cg.spark", "on");
				Options.v().setPhaseOption("cg", "library:any-subtype");
				break;
			case "spark":
				Options.v().setPhaseOption("cg.spark", "on");
				break;
		}
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_keep_line_number(true);

		int javaVersion = JavaUtils.getJavaVersion();

		// JAVA 8
		if(javaVersion < 9) {
			Options.v().set_prepend_classpath(true);
			Options.v().set_soot_classpath(setupData.getSootClassPath());
		} else {
			if (setupData.isUseJavaModules()){
				// JAVA VERSION >= 9 && IS A MODULEPATH PROJECT
				Options.v().set_prepend_classpath(true);
				Options.v().set_soot_modulepath("VIRTUAL_FS_FOR_JDK" + File.pathSeparator + setupData.getSootClassPath());
			} else {
				// JAVA VERSION >= 9 && IS A CLASSPATH PROJECT
				Options.v().set_soot_classpath("VIRTUAL_FS_FOR_JDK" + File.pathSeparator + setupData.getSootClassPath());
			}
		}

		Options.v().set_process_dir(setupData.getApplicationClassPath());
		Options.v().set_include(getIncludeList());
		Options.v().set_exclude(setupData.getExcludeList());
		Options.v().set_full_resolver(true);

		Scene.v().loadNecessaryClasses();
		Scene.v().setEntryPoints(getEntryPoints());
	}


	private List<SootMethod> getEntryPoints() {
		List<SootMethod> entryPoints = Lists.newArrayList();
		entryPoints.addAll(EntryPoints.v().application());
		entryPoints.addAll(EntryPoints.v().methodsOfApplicationClasses());
		return entryPoints;
	}

	private List<String> getIncludeList() {
		List<String> includeList = new LinkedList<>();
		includeList.add("java.lang.AbstractStringBuilder");
		includeList.add("java.lang.Boolean");
		includeList.add("java.lang.Byte");
		includeList.add("java.lang.Class");
		includeList.add("java.lang.Integer");
		includeList.add("java.lang.Long");
		includeList.add("java.lang.Object");
		includeList.add("java.lang.String");
		includeList.add("java.lang.StringCoding");
		includeList.add("java.lang.StringIndexOutOfBoundsException");
		return includeList;
	}
}
