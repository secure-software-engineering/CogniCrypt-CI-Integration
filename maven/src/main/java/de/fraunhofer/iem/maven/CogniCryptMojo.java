package de.fraunhofer.iem.maven;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transformer;
import soot.Unit;

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
	protected void doExecute() throws MojoExecutionException, MojoFailureException {
		validateParameters();
		super.doExecute();
	}

	@Override
	protected Transformer createAnalysisTransformer() {
		return new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				BoomerangPretransformer.v().reset();
				BoomerangPretransformer.v().apply();
				final CrySLResultsReporter reporter = new CrySLResultsReporter();
				ErrorMarkerListener fileReporter;

				System.out.println("Fetching CogniCrypt Rules.");
				List<CrySLRule> rules;
				try {
					rules = getRules();
				} catch (Exception e) {
					System.out.println("Failed fetching rules: " + e.getMessage());
					return;
				}

				if(outputFormat.equalsIgnoreCase("standard")) {
					fileReporter = new CommandLineReporter(getReportFolder().getAbsolutePath(), rules);
				} else if(outputFormat.equalsIgnoreCase("sarif")) {
					MavenProject project = getProject();
					fileReporter = new SARIFReporter(getReportFolder().getAbsolutePath(), rules,
							new SourceCodeLocater(project.hasParent() ?
									project.getParent().getBasedir() :
									project.getBasedir()));
				} else {
					throw new RuntimeException("Illegal state");
				}
				reporter.addReportListener(fileReporter);

				System.out.println("Creating ICFG!");
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

				System.out.println("Starting CogniCrypt Analysis!");
				scanner.scan(rules);
			}
		};
	}

	@Override
	protected List<String> getExcludeList() {
		List<String> exList = new LinkedList<String>();
		List<CrySLRule> rules = null;
		try {
			rules = getRules();
		} catch (CryptoAnalysisException e) {
			e.printStackTrace();
		}
		for(CrySLRule r : rules) {
			exList.add(r.getClassName());
		}
		return exList;
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




	private File getReportFolder() {
		File reportsFolder = new File(reportsFolderParameter);
		if (!reportsFolder.isAbsolute()) {
			reportsFolder = new File(getProjectTargetDir(), reportsFolderParameter);
		}
		if (!reportsFolder.exists()) {
			reportsFolder.mkdirs();
		}
		return reportsFolder;
	}
}
