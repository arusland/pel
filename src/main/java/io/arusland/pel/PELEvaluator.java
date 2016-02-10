package io.arusland.pel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Profile Expression Language Engine
 * 
 * Parse and evaluate expressions like
 * "!!(role:authorized && !!(platform:android)) || role:admins ||!!config:allowedit"
 * 
 * @author arusland
 *
 */
public class PELEvaluator {
	private final static Pattern NOTALLOWED_SYMBOLS = Pattern.compile("[^\\w:&|! \\(\\)]");
	private final List<Lexem> lexems;

	private PELEvaluator(List<Lexem> lexems) {
		this.lexems = lexems;
	}

	/**
	 * Generates java expression.
	 * 
	 * @return Java expression
	 */
	public String generateExpression(final Replacer replacer) {
		StringBuilder result = new StringBuilder();
		Lexem prevLexem = null;

		for (Lexem lexem : lexems) {
			if (prevLexem != null) {
				if (lexem.isOperator() && !lexem.equalsValue("!")
						|| prevLexem.isOperator() && !prevLexem.equalsValue("!")) {
					result.append(' ');
				}
			}

			if (lexem.getType() == LexemType.SYMBOL) {
				result.append(replacer.replace(lexem));
			} else {
				result.append(lexem.expressionValue());
			}

			prevLexem = lexem;
		}

		return result.toString();
	}

	public static PELEvaluator parse(String input) {
		List<Lexem> lexems = parseLexems(input);

		/*for (int i = 0; i < lexems.size(); i++) {
			System.out.println(lexems.get(i));
		}*/

		return new PELEvaluator(lexems);
	}

	/**
	 * Returns lexems by input expression.
	 * 
	 * @return Lexems.
	 */
	private static List<Lexem> parseLexems(String input) {
		List<String> tokens = parseTokens(input);
		List<Lexem> lexems = new ArrayList<>();
		boolean firstPartOfSymbolFound = false;
		boolean colonFound = false;
		boolean firstPartOfOperatorFound = false;
		int openBracketFoundCounter = 0;
		String lastToken = "";

		for (String token : tokens) {
			if (Character.isLetter(token.charAt(0))) {
				if (firstPartOfOperatorFound) {
					throw new IllegalStateException("Second part of operator not found for: " + lastToken);
				}

				if (firstPartOfSymbolFound) {
					if (!colonFound) {
						throw new IllegalStateException("Colon not found before: " + token);
					}

					lexems.add(new Lexem(LexemType.SYMBOL, token, lastToken));

					firstPartOfSymbolFound = false;
					colonFound = false;

				} else {
					firstPartOfSymbolFound = true;
					lastToken = token;
					colonFound = false;
				}
			} else if (":".equals(token)) {
				if (!firstPartOfSymbolFound) {
					throw new IllegalStateException("Not symbol found before colon");
				}

				if (colonFound) {
					throw new IllegalStateException("Colon found twice");
				}

				colonFound = true;
			} else {
				if (firstPartOfSymbolFound) {
					throw new IllegalStateException("Invalid symbol without colon: " + lastToken);
				}

				if (firstPartOfOperatorFound) {
					if (")!(".contains(token) || !token.equals(lastToken)) {
						throw new IllegalStateException("Invalid operator found: " + lastToken);
					}

					lexems.add(new Lexem(LexemType.OPERATOR, lastToken + token));

					firstPartOfOperatorFound = false;
				} else {
					if (token.equals("!")) {
						lexems.add(new Lexem(LexemType.OPERATOR, token));
					} else if (token.equals("(")) {
						openBracketFoundCounter++;

						lexems.add(new Lexem(LexemType.OPEN_BRACKET, token));
					} else if (token.equals(")")) {

						if (openBracketFoundCounter <= 0) {
							throw new IllegalStateException("Cannot find open bracket");
						}

						openBracketFoundCounter--;

						lexems.add(new Lexem(LexemType.CLOSE_BRACKET, token));
					} else {
						firstPartOfOperatorFound = true;
						lastToken = token;
					}
				}
			}
		}

		if (openBracketFoundCounter > 0) {
			throw new IllegalStateException("Cannot find close bracket");
		}

		return validateLexems(lexems);
	}

	/**
	 * Validates lexem list.
	 * 
	 */
	private static List<Lexem> validateLexems(List<Lexem> lexems) {
		Lexem prevLexem = null;

		for (Lexem lexem : lexems) {
			if (lexem.isOperator()) {
				if (prevLexem != null) {
					if (prevLexem.isOperator() && !prevLexem.equalsValue("!") && !lexem.equalsValue("!")) {
						throw new IllegalStateException("Invalid operator using: " + lexem.getValue());
					}
				}
			}

			prevLexem = lexem;
		}

		return lexems;
	}

	/**
	 * Returns token list by input expression.
	 * 
	 * @return Token list.
	 */
	private static List<String> parseTokens(String input) {
		if (!isValidInput(input)) {
			throw new IllegalStateException("Exptression is invalid: " + input);
		}

		boolean tokenStarted = false;
		List<String> tokens = new ArrayList<>();
		StringBuilder currentToken = new StringBuilder();

		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);

			if (Character.isLetterOrDigit(ch)) {
				if (!tokenStarted) {
					tokenStarted = true;
				}
				currentToken.append(ch);
			} else {
				if (tokenStarted) {
					tokenStarted = false;
					tokens.add(currentToken.toString());
					currentToken.setLength(0);
				}

				if (ch != ' ') {
					tokens.add(String.valueOf(ch));
				}
			}
		}

		if (tokenStarted) {
			tokens.add(currentToken.toString());
		}

		return tokens;
	}

	private static boolean isValidInput(String input) {
		input = input != null ? input.trim() : "";

		return input.length() > 0 && !NOTALLOWED_SYMBOLS.matcher(input).matches();
	}

	public static class Lexem {
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

	public enum LexemType {
		SYMBOL,

		OPERATOR,

		OPEN_BRACKET,

		CLOSE_BRACKET
	}

	public interface Replacer {
		String replace(Lexem lexem);
	}
}
