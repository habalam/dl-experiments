import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import org.nd4j.linalg.io.ClassPathResource;

public class CsvFileStringTransformer {

	private static final List<Integer> COLUMNS_TO_TRANSFORM;

	static {
		List<Integer> columnsToTransform = new ArrayList<>();
		columnsToTransform.add(0);
		columnsToTransform.add(1);
		COLUMNS_TO_TRANSFORM = Collections.unmodifiableList(columnsToTransform);
	}

	public static void main(String[] args) throws IOException {
		CSVReader csvReader = new CSVReader(new InputStreamReader(
			new FileInputStream(new ClassPathResource("/input/generated_strings_data.csv").getFile())));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("generated_strings_data_transformed.csv")));
		while (true) {
			String[] line = csvReader.readNext();
			if (line == null) {
				break;
			}
			writer.write(processLine(line));
			writer.newLine();
		}
		writer.close();
	}

	private static String processLine(String[] line) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < line.length; i++) {
			if (COLUMNS_TO_TRANSFORM.contains(i)) {
				transformStringToNumber(sb, line[i]);
			}
			else {
				sb.append(line[i]);
			}
//			if (i != (line.length - 1)) {
//				sb.append(',');
//			}
		}
		return sb.toString();
	}

	private static void transformStringToNumber(StringBuilder sb, String word) {
		int stringLength = word.length();
		for (char character: word.toCharArray()) {
			sb.append((int) character).append(",");
		}
		for (int i = stringLength; i < 100; i++) {
			sb.append("0,");
		}
	}

//	private static String transformStringToNumber(String value) {
//		return value.chars().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
//	}
}
