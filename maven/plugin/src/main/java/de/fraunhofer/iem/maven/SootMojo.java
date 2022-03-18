package de.fraunhofer.iem.maven;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import crypto.rules.CrySLRule;
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
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public abstract class SootMojo extends AbstractDependencyFilterMojo {

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

	@Override
	public MavenProject getProject() {
		return this.project;
	}

	@Override
	protected ArtifactsFilter getMarkedArtifactFilter() {
		return null;
	}

	protected abstract void analyse();

	protected Collection<Path> resolveDependencies() throws MojoExecutionException {
		if (!includeDependencies)
			return Collections.emptyList();
		var dependencies = new ArrayList<Path>();
		Set<Artifact> artifacts = getResolvedDependencies(false);
		for (Artifact artifact : artifacts) {
			var filePath = artifact.getFile().getPath();
			if (StringUtils.isNotEmpty(localRepoProperty)) {
				// substitute the property for the local repo path to make the classpath file portable.
				File localBasedir = repositoryManager.getLocalRepositoryBasedir(session.getProjectBuildingRequest());
				filePath = StringUtils.replace(filePath, localBasedir.getAbsolutePath(), localRepoProperty);
			}
			var dependency = Path.of(filePath);
			dependencies.add(dependency);
		}
		return dependencies;
	}

	protected Path getProjectTargetDir() {
		return Path.of(project.getBuild().getDirectory());
	}

	protected SootSetupData createSootSetupData(Path applicationPath, Collection<Path> dependencies, Collection<CrySLRule> rules) {
		List<String> excludeList = getExcludeList(rules);
		List<String> appCp = Lists.newArrayList(applicationPath.toAbsolutePath().toString());
		String sootCp = buildSootClassPath(applicationPath, dependencies);
		boolean modular = JavaUtils.isModularProject(applicationPath);
		return new SootSetupData(callGraph, sootCp, appCp, modular, excludeList);
	}


	private List<String> getExcludeList(Collection<CrySLRule> rules) {
		return  Lists.newArrayList(excludedPackages.split(","));
	}

	private String buildSootClassPath(Path applicationPath, Collection<Path> dependencies) {
		var sootCp = new ArrayList<String>();
		for (var dep: dependencies) {
			sootCp.add(dep.toAbsolutePath().toString());
		}
		sootCp.add(applicationPath.toAbsolutePath().toString());
		String javaPath = JavaUtils.getJavaRuntimePath().getAbsolutePath();
		sootCp.add(0, javaPath);
		List<String> distinctSootCp = sootCp.stream().distinct().collect(Collectors.toList());
		return Joiner.on(File.pathSeparator).join(distinctSootCp);
	}
}
