package de.fraunhofer.iem.maven;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import de.fraunhofer.iem.sast.parser.sarif.SarifParser;
import edu.hm.hafner.analysis.IssueParser;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

public class CogniCrypt extends ReportScanningTool{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;   
	static final String ID = "cognicrypt";
    private static final String ICON_PREFIX = "/plugin/cognicrypt-jenkins/";

    /** Creates a new instance of {@link CogniCrypt}.
     */
    @DataBoundConstructor
    public CogniCrypt() {
        super();
        // empty constructor required for stapler
    }

	@Override
	public IssueParser createParser() {
		return new SarifParser();
	}

    /** Descriptor for this static analysis tool. */
    @Symbol("cognicrypt")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "CogniCrypt Parser";
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName()) {
                @Override
                public String getSmallIconUrl() {
                    return ICON_PREFIX + ID + "-24x24.png";
                }

                @Override
                public String getLargeIconUrl() {
                    return ICON_PREFIX + ID + "-48x48.png";
                }
            };
        }

        @Override
        public String getUrl() {
            return "https://www.eclipse.org/cognicrypt/";
        }
    }
}
