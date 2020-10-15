package de.fraunhofer.iem.maven;

import java.util.Collections;
import java.util.List;

class SootSetupData {

	private String callgraph;

	private String sootClassPath;

	private boolean useJavaModules;

	private List<String> applicationClassPath;

	private List<String> excludeList;

	public SootSetupData(
			String callgraph,
			String sootClassPath,
			List<String> applicationClassPath,
			boolean useJavaModules,
			List<String> excludeList) {
		this.callgraph = callgraph;
		this.sootClassPath = sootClassPath;
		this.applicationClassPath = applicationClassPath;
		this.excludeList = excludeList;
		this.useJavaModules = useJavaModules;
	}

	public SootSetupData(String callgraph, String sootClassPath, List<String> applicationClassPath) {
		this(callgraph, sootClassPath, applicationClassPath, false, Collections.EMPTY_LIST);
	}

	public String getCallgraph() {
		return callgraph;
	}

	public String getSootClassPath() {
		return sootClassPath;
	}

	public boolean isUseJavaModules() {
		return useJavaModules;
	}

	public List<String> getApplicationClassPath() {
		return applicationClassPath;
	}

	public List<String> getExcludeList() {
		return excludeList;
	}
}
