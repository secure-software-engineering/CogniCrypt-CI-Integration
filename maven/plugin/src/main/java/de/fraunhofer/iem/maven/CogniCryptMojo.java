package de.fraunhofer.iem.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import crypto.cryslhandler.CrySLModelReaderClassPath;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import boomerang.callgraph.BoomerangICFG;
import boomerang.callgraph.ObservableDynamicICFG;
import boomerang.callgraph.ObservableICFG;
import boomerang.callgraph.ObservableStaticICFG;
import boomerang.preanalysis.BoomerangPretransformer;
import crypto.analysis.CrySLResultsReporter;
import crypto.analysis.CryptoScanner;
import crypto.exceptions.CryptoAnalysisException;
import crypto.reporting.CommandLineReporter;
import crypto.reporting.ErrorMarkerListener;
import crypto.reporting.SARIFReporter;
import crypto.reporting.SourceCodeLocater;
import crypto.rules.CrySLRule;
import crypto.rules.CrySLRuleReader;
import soot.*;

@Mojo(name = "cognicrypt", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class CogniCryptMojo extends SootMojo {

	@Parameter(property = "cognicrypt.rulesDirectory")
	private String rulesDirectory;

	@Parameter(property = "cognicrypt.reportsDirectory", defaultValue = "cognicrypt-reports")
	private String reportsFolderParameter;
	
	@Parameter(property = "cognicrypt.outputFormat", defaultValue = "standard")
	private String outputFormat;
	
	@Parameter(property = "cognicrypt.dynamic-cg", defaultValue = "true")
	private boolean dynamicCg;


	@Override
	protected void doExecute() throws MojoExecutionException {
		validateParameters();

		var targetDir = getProjectTargetDir();
		var classFolder = targetDir.resolve("classes");
		if(!classFolder.toFile().exists()) {
			getLog().debug("No class folder for project found at " + classFolder + "");
			getLog().info("CogniCrypt found nothing to analyze!");
			return;
		}

		getLog().debug("Resolving project dependencies...");
		var dependencies = resolveDependencies();
		getLog().debug("Resolved " + dependencies.size() + " dependencies");

		getLog().debug("Initializing Soot...");
		registerDependencies(dependencies);
		List<CrySLRule> rules;
		try {
			getLog().info("Fetching CogniCrypt Rules.");
			rules = getRules();
		} catch (CryptoAnalysisException e) {
			getLog().error("Failed fetching rules: " + e.getMessage(), e);
			return;
		}
		getLog().debug("Initialized CogniCrypt.");

		getLog().debug("Initializing Soot...");
		var setupData = createSootSetupData(classFolder, dependencies, rules);
		new SootSetup(setupData).run();
		getLog().debug("Initialized Soot.");

		getLog().info("Running CogniCrypt...");
		analyse(rules);
		getLog().info("CogniCrypt analysis done!");

	}

	private void analyse(List<CrySLRule> rules) {
		PackManager.v().getPack("wjap").add(new Transform("wjap.ifds", createAnalysisTransformer(rules)));
		PackManager.v().runPacks();
	}

	private void registerDependencies(Collection<Path> dependencies){
		for (var dep: dependencies) {
			CrySLModelReaderClassPath.addToClassPath(dep.toUri());
		}
	}

	private Transformer createAnalysisTransformer(List<CrySLRule> rules) {
		return new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				BoomerangPretransformer.v().reset();
				BoomerangPretransformer.v().apply();
				final CrySLResultsReporter reporter = new CrySLResultsReporter();
				ErrorMarkerListener fileReporter;

				if(outputFormat.equalsIgnoreCase("standard")) {
					fileReporter = new CommandLineReporter(getReportFolder().toAbsolutePath().toString(), rules);
				} else if(outputFormat.equalsIgnoreCase("sarif")) {
					MavenProject project = getProject();
					fileReporter = new SARIFReporter(getReportFolder().toAbsolutePath().toString(), rules,
							new SourceCodeLocater(project.hasParent() ?
									project.getParent().getBasedir() :
									project.getBasedir()));
				} else {
					throw new IllegalStateException("Illegal output format specified");
				}
				reporter.addReportListener(fileReporter);

				getLog().debug("Creating ICFG!");
				final ObservableICFG<Unit, SootMethod> icfg;
				if(!dynamicCg) {
					 icfg = new ObservableStaticICFG(new BoomerangICFG(true));
				} else {
					 icfg = new ObservableDynamicICFG(false);
				} 
				CryptoScanner scanner = new CryptoScanner() {

					@Override
					public ObservableICFG<Unit, SootMethod> icfg() {
						return icfg;
					}

					@Override
					public CrySLResultsReporter getAnalysisListener() {
						return reporter;
					}
				};
				getLog().debug("Starting CogniCrypt Analysis!");
				scanner.scan(rules);
			}
		};
	}

	private void validateParameters() throws MojoExecutionException {
		if(!new File(rulesDirectory).exists() || !new File(rulesDirectory).isDirectory()) {
			throw new MojoExecutionException("Failed to locate the folder of the CrySL rules. " +
					"Specify -Dcognicrypt.rulesDirectory=<PATH-TO-CRYSL-RULES>.");
		}
		if(!Pattern.matches("(standard|sarif)", outputFormat)) {
			throw new MojoExecutionException("Incorrect output format specified. " +
					"Use -Dcognicrypt.outputFormat=[standard|sarif].");
		}
	}
	
	/**
	 * Receives the set of rules form a given directory.
	 */
	private List<CrySLRule> getRules() throws CryptoAnalysisException {
		return CrySLRuleReader.readFromDirectory(new File(rulesDirectory));
	}

	private Path getReportFolder() {
		var reportsFolder = Path.of(reportsFolderParameter);
		if (!reportsFolder.isAbsolute()) {
			reportsFolder = getProjectTargetDir().resolve(reportsFolderParameter);
		}
		var asFile = reportsFolder.toFile();
		if (!asFile.exists()) {
			asFile.mkdirs();
		}
		return reportsFolder;
	}
}
