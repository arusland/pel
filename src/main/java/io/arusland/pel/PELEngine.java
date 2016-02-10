package io.arusland.pel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Profile Expression Language Engine
 *
 * @author arusland
 *
 */
public class PELEngine {
	private final static Pattern NOTALLOWED_SYMBOLS = Pattern.compile("[^\\w:&|! \\(\\)]");
	private final List<Lexem> lexems;

	private PELEngine(List<Lexem> lexems) {
		this.lexems = Validate.notNull(lexems);
	}

	/**
	 * Generates java expression.
	 * 
	 * @return Java expression
	 */
	public String generateExpression(final Replacer replacer) {
		Validate.notNull(replacer);
		
		final StringBuilder result = new StringBuilder();
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

	public static PELEngine parse(String input) {
		List<Lexem> lexems = parseLexems(input);

		/*
		 * for (int i = 0; i < lexems.size(); i++) {
		 * System.out.println(lexems.get(i)); }
		 */

		return new PELEngine(lexems);
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
		if (!lexems.isEmpty()) {
			Lexem prevLexem = null;

			for (Lexem lexem : lexems) {
				if (lexem.isOperator()) {
					if (prevLexem != null) {
						if (prevLexem.isOperator()) {
							if (!lexem.equalsValue("!")) {
								throw new IllegalStateException("Invalid operator using: " + lexem.getValue());
							}
						}
					} else if (!lexem.equalsValue("!")) {
						throw new IllegalStateException("Invalid operator using: " + lexem.getValue());
					}
				}

				prevLexem = lexem;
			}
			
			Lexem lastLexem = lexems.get(lexems.size() - 1);

			if (lastLexem.isOperator()) {
				throw new IllegalStateException("Operator cannot be last in expression: " + lastLexem.getValue());
			}
		}

		return lexems;
	}

	/**
	 * Returns token list by input expression.
	 * 
	 * @return Token list.
	 */
	private static List<String> parseTokens(String input) {
		validateInput(input);

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

	private static void validateInput(String input) {
		if (StringUtils.isBlank(input)) {
			throw new IllegalStateException("Expression cannot be empty");
		}

		if (NOTALLOWED_SYMBOLS.matcher(input).matches()) {
			throw new IllegalStateException("Expression is invalid: " + input);
		}
	}
}
