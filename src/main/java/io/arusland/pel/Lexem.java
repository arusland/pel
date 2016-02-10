package io.arusland.pel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import io.arusland.pel.LexemType;

public class Lexem {
	private final String value;
	private final String name;
	private final LexemType type;

	public Lexem(LexemType type, String value) {
		this(type, value, StringUtils.EMPTY);
	}

	public Lexem(LexemType type, String value, String name) {
		this.type = Validate.notNull(type, "type");
		this.value = Validate.notBlank(value, "value");
		this.name = Validate.notNull(name, "name");
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
		if (StringUtils.isBlank(name)) {
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
		if (StringUtils.isBlank(name)) {
			return String.format("%s - %s", type.toString(), value);
		} else {
			return String.format("%s - %s:%s", type.toString(), name, value);
		}
	}
}