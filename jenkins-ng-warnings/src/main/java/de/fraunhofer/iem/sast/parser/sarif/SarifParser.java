package de.fraunhofer.iem.sast.parser.sarif;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

public class SarifParser extends IssueParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Map<String, Severity> errorMap = new HashMap<>();
	
	public SarifParser() {
		initialize();
	}
	
	private void initialize() {
		errorMap.put(SARIFConfig.CONSTRAINT_ERROR, Severity.ERROR);
		errorMap.put(SARIFConfig.NEVER_TYPE_OF_ERROR, Severity.WARNING_HIGH);
		errorMap.put(SARIFConfig.HARDCODED_ERROR, Severity.WARNING_HIGH);
		errorMap.put(SARIFConfig.FORBIDDEN_METHOD_ERROR, Severity.WARNING_NORMAL);
		errorMap.put(SARIFConfig.TYPE_STATE_ERROR, Severity.WARNING_NORMAL);
		errorMap.put(SARIFConfig.REQUIRED_PREDICATE_ERROR, Severity.WARNING_HIGH);
		errorMap.put(SARIFConfig.INCOMPLETE_OPERATION_ERROR, Severity.WARNING_LOW);
		errorMap.put(SARIFConfig.PREDICATE_CONTRADICTION_ERROR, Severity.WARNING_NORMAL);
	}

	@Override
	public Report parse(ReaderFactory readerFactory) throws ParsingException, ParsingCanceledException {
		System.out.println("Parsing file");
		return parseSarif(readerFactory.readString());
	}

	private String getFileName(JSONObject location) {
		return location.getJSONObject("physicalLocation").getJSONObject("fileLocation").getString("uri");
	}

	private int getLineNumber(JSONObject location) {
		return location.getJSONObject("physicalLocation").getJSONObject("region").getInt("startLine");
	}

	private Report parseSarif(String sarifJson) {
		Report report = new Report();
		JSONObject sarif_root = new JSONObject(sarifJson);
		JSONArray sarif_runs = sarif_root.getJSONArray("runs");
		for (int k = 0; k < sarif_runs.length(); k++) {
			JSONObject runs = sarif_runs.getJSONObject(k);
			JSONArray results = runs.getJSONArray("results");
			for (int j = 0; j < results.length(); j++) {
				JSONObject current_result = results.getJSONObject(j);
				String file_name = getFileName(current_result.getJSONArray("locations").getJSONObject(0));
				String line_number = String
						.valueOf(getLineNumber(current_result.getJSONArray("locations").getJSONObject(0)));
				String error = current_result.getJSONObject("message").getString("richText");
				String error_desc = current_result.getJSONObject("message").getString("text");
				String error_type = current_result.getString("ruleId").split("-")[0];
				if (!error_type.equals(SARIFConfig.IMPRECISE_VALUE_EXTRACTION_ERROR)) {
					IssueBuilder builder = new IssueBuilder();
					builder.setType(error_type);
					builder.setMessage(error);
					builder.setSeverity(errorMap.get(error_type));
					builder.setDescription(error_desc);
					builder.setLineStart(line_number);
					builder.setFileName(file_name);
					report.add(builder.build());
				}
			}
		}
		return report;
	}

}
