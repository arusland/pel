package io.arusland.pel;

import io.arusland.pel.LexemType;

public class Lexem {
	private final String value;
	private final String name;
	private final LexemType type;

	public Lexem(LexemType type, String value) {
		this(type, value, "");
	}

	public Lexem(LexemType type, String value, String name) {
		this.type = type;
		this.value = value;
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public LexemType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String expressionValue() {
		if (name == null || name.isEmpty()) {
			return value;
		} else {
			return String.format("%s:%s", name, value);
		}
	}

	public boolean isOperator() {
		return getType() == LexemType.OPERATOR;
	}

	public boolean equalsValue(String value) {
		return getValue().equals(value);
	}

	@Override
	public String toString() {
		if (name == null || name.isEmpty()) {
			return String.format("%s - %s", type.toString(), value);
		} else {
			return String.format("%s - %s:%s", type.toString(), name, value);
		}
	}
}