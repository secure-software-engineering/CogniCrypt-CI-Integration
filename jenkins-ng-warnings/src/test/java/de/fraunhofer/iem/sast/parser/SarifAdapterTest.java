package de.fraunhofer.iem.sast.parser;

import de.fraunhofer.iem.sast.parser.sarif.SarifParser;
import edu.hm.hafner.analysis.AbstractParserTest;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.assertj.SoftAssertions;

public class SarifAdapterTest extends AbstractParserTest {
	SarifAdapterTest() {
        super("CogniCrypt-SARIF-Format.txt");
    }

    @Override
    protected void assertThatIssuesArePresent(final Report report, final SoftAssertions softly) {
        softly.assertThat(report).hasSize(6);
        softly.assertThat(report.get(0))
                .hasMessage("ConstraintError violating CrySL rule for javax.crypto.Cipher.")
                .hasDescription("First parameter (with value \"AES/ECB/PKCS5Padding\") should be any of AES/{CBC, GCM, PCBC, CTR, CTS, CFB, OFB}.")
                .hasFileName("example/ConstraintErrorExample.java")
                .hasType("ConstraintError")
                .hasLineStart(15)
                .hasSeverity(Severity.ERROR);
        softly.assertThat(report.get(1))
		        .hasMessage("IncompleteOperationError violating CrySL rule for java.security.Signature.")
		        .hasDescription("Operation on object of type java.security.Signature object not completed. Expected call to <java.security.Signature: void update(byte[],int,int)>, <java.security.Signature: void update(byte)>, sign, <java.security.Signature: void update(java.nio.ByteBuffer)>, <java.security.Signature: void update(byte[])>.")
		        .hasFileName("example/IncompleOperationErrorExample.java")
		        .hasType("IncompleteOperationError")
		        .hasLineStart(22)
		        .hasSeverity(Severity.WARNING_LOW);
        softly.assertThat(report.get(2))
		        .hasMessage("IncompleteOperationError violating CrySL rule for java.security.Signature.")
		        .hasDescription("Operation on object of type java.security.Signature object not completed. Expected call to sign, update.")
		        .hasFileName("example/IncompleOperationErrorExample.java")
		        .hasType("IncompleteOperationError")
		        .hasLineStart(31)
		        .hasSeverity(Severity.WARNING_LOW);
        softly.assertThat(report.get(3))
		        .hasMessage("RequiredPredicateError violating CrySL rule for javax.crypto.Cipher.")
		        .hasDescription("Second parameter was not properly generated as generated Key.")
		        .hasFileName("example/PredicateMissingExample.java")
		        .hasType("RequiredPredicateError")
		        .hasLineStart(26)
		        .hasSeverity(Severity.WARNING_HIGH);
        softly.assertThat(report.get(4))
		        .hasMessage("ConstraintError violating CrySL rule for javax.crypto.KeyGenerator.")
		        .hasDescription("First parameter (with value 46) should be any of {128, 192, 256}.")
		        .hasFileName("example/PredicateMissingExample.java")
		        .hasType("ConstraintError")
		        .hasLineStart(21)
		        .hasSeverity(Severity.ERROR);
        softly.assertThat(report.get(5))
		        .hasMessage("TypestateError violating CrySL rule for java.security.Signature.")
		        .hasDescription("Unexpected call to method sign on object of type java.security.Signature. Expect a call to one of the following methods initSign,update.")
		        .hasFileName("example/TypestateErrorExample.java")
		        .hasType("TypestateError")
		        .hasLineStart(24)
		        .hasSeverity(Severity.WARNING_NORMAL);
    }

    @Override
    protected SarifParser createParser() {
        return new SarifParser();
    }
}