package datageneration.string;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum StringChangeType {
	ADD_OR_REMOVE_CHARACTERS(1),
	TO_UPPER_OR_LOWER_CASE(2),
	TO_SHORTCUT(3),
	SWITCH_WORDS(4),
	STRIP_DIACRITICS(5);

	private static final Map<Integer, StringChangeType> CHANGE_TYPE_BY_VALUE;

	static {
		Map<Integer, StringChangeType> changeTypeByValue = new HashMap<>();
		Arrays.stream(StringChangeType.values())
			.forEach(stringChangeType -> changeTypeByValue.put(stringChangeType.value, stringChangeType));
		CHANGE_TYPE_BY_VALUE = Collections.unmodifiableMap(changeTypeByValue);
	}

	private final int value;

	StringChangeType(int value) {
		this.value = value;
	}

	public static StringChangeType getByValue(int value) {
		return CHANGE_TYPE_BY_VALUE.get(value);
	}
}
