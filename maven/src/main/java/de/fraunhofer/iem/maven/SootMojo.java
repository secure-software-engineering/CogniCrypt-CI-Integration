package de.fraunhofer.iem.maven;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import crypto.cryslhandler.CrySLModelReaderClassPath;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.dependency.fromDependencies.AbstractDependencyFilterMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.codehaus.plexus.util.StringUtils;
import soot.PackManager;
import soot.Transform;
import soot.Transformer;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SootMojo extends AbstractDependencyFilterMojo {

	private List<String> classFolders = Lists.newLinkedList();

	private Optional<File> targetDir = Optional.empty();

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;

	@Parameter(property = "mdep.localRepoProperty", defaultValue = "")
	protected String localRepoProperty;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(property = "soot.callGraph", defaultValue = "cha")
	private String callGraph;

	@Parameter(property = "soot.includeDependencies", defaultValue = "true")
	private boolean includeDependencies;

	@Parameter(property = "soot.excludedPackages", defaultValue = "java.*,javax.*,sun.*,com.sun.*,com.ibm.*,org.xml.*,org.w3c.*,apple.awt.*,com.apple.*")
	private String excludedPackages;

	@Component
	private RepositoryManager repositoryManager;

	public File getProjectTargetDir() {
		if (!targetDir.isPresent()){
			File td = new File(project.getBuild().getDirectory());
			targetDir = Optional.of(td);
		}
		return targetDir.get();
	}

	@Override
	public MavenProject getProject() {
		return this.project;
	}

	@Override
	protected ArtifactsFilter getMarkedArtifactFilter() {
		return null;
	}

	@Override
	protected void doExecute() throws MojoExecutionException, MojoFailureException {
		if (includeDependencies) {
			Set<Artifact> artifacts = getResolvedDependencies(false);
			for (Artifact a : artifacts) {
				String file = a.getFile().getPath();
				// substitute the property for the local repo path to make the classpath file
				// portable.
				if (StringUtils.isNotEmpty(localRepoProperty)) {
					File localBasedir = repositoryManager
							.getLocalRepositoryBasedir(session.getProjectBuildingRequest());

					file = StringUtils.replace(file, localBasedir.getAbsolutePath(), localRepoProperty);
				}

				CrySLModelReaderClassPath.addToClassPath(new File(file).toURI());
				classFolders.add(file);
			}
		}
		run(getProjectTargetDir());
	}

	public void run(File targetDir) {
		final File classFolder = new File(targetDir.getAbsolutePath() + File.separator + "classes");
		if(!classFolder.exists()) {
			getLog().info("No class folder found at " + classFolder + "");
			return;
		}

		new SootSetup(CreateSootSetupData()).run();
		analyse();
		getLog().info("Soot analysis done!");
	}

	protected abstract Transformer createAnalysisTransformer();

	private SootSetupData CreateSootSetupData() {
		List<String> excludeList = getExcludeList();
		List<String> appCp = buildApplicationClassPath();
		String sootCp = buildSootClassPath(classFolders, appCp);
		boolean modular = JavaUtils.isModularProject(new File(getProjectTargetDir(), "classes"));
		return new SootSetupData(callGraph, sootCp, appCp, modular, excludeList);
	}

	private void analyse() {
		PackManager.v().getPack("wjap").add(new Transform("wjap.ifds", createAnalysisTransformer()));
		PackManager.v().runPacks();
	}


	protected abstract List<String> getExcludeList();

	private List<String> buildApplicationClassPath() {
		final File classFolder = new File(getProjectTargetDir().getAbsolutePath(), "classes");
		return Lists.newArrayList(classFolder.getAbsolutePath());
	}

	private String buildSootClassPath(List<String> dependencies, List<String> applicationCp) {
		List<String> sootCp = Stream.of(dependencies, applicationCp)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		String javaPath = JavaUtils.getJavaRuntimePath().getAbsolutePath();
		sootCp.add(0, javaPath);
		List<String> distinctSootCp = sootCp.stream().distinct().collect(Collectors.toList());
		return Joiner.on(File.pathSeparator).join(distinctSootCp);
	}
}
