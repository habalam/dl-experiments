package datageneration.string;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringComparisonDataCreator {

	private static final Logger logger = LoggerFactory.getLogger(StringComparisonDataCreator.class);

	public static void main(String[] args) throws IOException, URISyntaxException {
		try (Stream<String> linesStream = Files.lines(Paths.get(ClassLoader.getSystemResource("input/export_raw_data.csv").toURI()))) {
			AtomicInteger linesProcessed = new AtomicInteger();
			StringBuilder sb = new StringBuilder();
			BufferedWriter bf = new BufferedWriter(new FileWriter(new File("generated_strings_data.csv")));
			List<String> usedStrings = new ArrayList<>();
			linesStream.forEach(line -> {
				if (line.length() > 100) {
					line = line.substring(0, 100);
				}
				appendString(sb, line.replaceAll("\"", "\\\\\"")).append(',');
				if (linesProcessed.get() % 3 == 0) {
					if (!usedStrings.isEmpty()) {
						appendString(sb, usedStrings.get(new Random().nextInt(usedStrings.size() - 1))).append(",0");
					}
					else {
						appendString(sb, RandomStringUtils.randomAlphabetic(10));
					}
				}
				else {
					String stringVariant = generateStringVariant(line);
					appendString(sb, stringVariant.replaceAll("\"", "\\\\\"")).append(",1");
				}
				
				try {
					bf.write(sb.toString());
					bf.newLine();
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				logger.info(sb.toString());
				//clear SB
				sb.setLength(0);
				usedStrings.add(line);
				linesProcessed.getAndIncrement();
			});
			bf.flush();
			bf.close();
		}
	}

	private static StringBuilder appendString(StringBuilder stringBuilder, String sentence) {
		return  stringBuilder.append("\"").append(sentence).append("\"");
	}

	private static String generateStringVariant(String sentence) {
		StringChangeType changeType = getChangeType();
		switch (changeType) {
			case ADD_OR_REMOVE_CHARACTERS:
				return addOrRemoveCharacters(sentence);
			case TO_UPPER_OR_LOWER_CASE:
				return toUpperOrLowerCase(sentence);
			case TO_SHORTCUT:
				return toShortcut(sentence);
			case SWITCH_WORDS:
				return switchWords(sentence);
			case STRIP_DIACRITICS:
				return stripDiacritics(sentence);
			default:
				return null;
		}
	}

	private static String stripDiacritics(String sentence) {
		if (sentence == null) {
			return null;
		}
		return Normalizer.normalize(sentence, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

	private static String toShortcut(String sentence) {
		List<String> sentenceWords = Splitter.on(' ').splitToList(sentence);
		String shortcut = sentenceWords.stream().map(word -> {
			if (!word.isEmpty()) {
				return String.valueOf(word.charAt(0));
			}
			return word;
		}).collect(Collectors.joining(" "));
		return shortcut;
	}

	private static String switchWords(String sentence) {
		List<String> sentenceWords = Splitter.on(' ').splitToList(sentence).stream().collect(Collectors.toList());
		Collections.shuffle(sentenceWords);
		String sentenceWithSwitchedWords = sentenceWords.stream().collect(Collectors.joining(" "));
		return sentenceWithSwitchedWords;
	}

	private static String toUpperOrLowerCase(String sentence) {
		if (randomBoolean()) {
			if (randomBoolean()) {
				return changeCasingOfRandomCharactersInString(sentence, CasingType.UPPER_CASE);
			}
			else {
				return sentence.toUpperCase();
			}
		}
		else {
			if (randomBoolean()) {
				return changeCasingOfRandomCharactersInString(sentence, CasingType.LOWER_CASE);
			}
			else {
				return sentence.toLowerCase();
			}
		}
	}

	private static String changeCasingOfRandomCharactersInString(String sentence, CasingType casingType) {
		int charsToChangeCount = randomIntegerTo(sentence.length() / 2);
		for (Integer index : createListOfIndexesToChange(sentence, charsToChangeCount)) {
			sentence = changeCaseOfCharacterInString(sentence, casingType, index);
		}
		return sentence;
	}

	private static String changeCaseOfCharacterInString(String sentence, CasingType casingType, Integer index) {
		char changedChar = 0;
		switch (casingType) {
			case LOWER_CASE:
				changedChar = Character.toLowerCase(sentence.charAt(index));
				break;
			case UPPER_CASE:
				changedChar = Character.toUpperCase(sentence.charAt(index));
				break;
		}
		return changeStringCharWith(sentence, changedChar, index);
	}

	private static List<Integer> createListOfIndexesToChange(String sentence, int charsToChangeCount) {
		List<Integer> indexesToChange = new ArrayList<>();
		for (int i = 0; i < charsToChangeCount; i++) {
			int randomCharIndex = new Random().nextInt(sentence.length() - 1);
			if (indexesToChange.contains(randomCharIndex)) {
				indexesToChange.add(randomCharIndex + 1);
			}
			else {
				indexesToChange.add(randomCharIndex);
			}
		}
		return indexesToChange;
	}

	private static String addOrRemoveCharacters(String sentence) {
		int originalSentenceLength = sentence.length();
		int charsToChangeCount = randomIntegerTo(originalSentenceLength / 4);

		if (randomBoolean()) {
			for (int i = 0; i < charsToChangeCount; i++) {
				int charIndexToChange = randomIntegerTo(sentence.length() - 1);
				sentence = sentence.substring(0, charIndexToChange) + RandomStringUtils.randomAlphabetic(1) +
					sentence.substring(charIndexToChange + 1);
			}
		}
		else {
			for (int i = 0; i < charsToChangeCount; i++) {
				int charIndexToChange = randomIntegerTo(sentence.length() - 1);
				sentence = sentence.substring(0, charIndexToChange) + sentence.substring(charIndexToChange + 1);
			}
		}

		return sentence;
	}

	private static String changeStringCharWith(String sentence, char insertChar, int charIndexToChange)
	{
		return sentence.substring(0, charIndexToChange) + insertChar +
			sentence.substring(charIndexToChange + 1);
	}

	private static StringChangeType getChangeType() {
		return StringChangeType.getByValue(randomIntegerTo(StringChangeType.values().length));
	}

	private static int randomIntegerTo(int maxValue) {
		return (int) Math.ceil(Math.random() * maxValue);
	}

	private static boolean randomBoolean() {
		return new Random().nextBoolean();
	}
}
