package com.lti.data.recast.functionmapping.services;

import java.util.HashSet;
import java.util.Set;

public class Lexer {
	private StringBuilder input = new StringBuilder();
	private Token token;
	private String lexema;
	private boolean exausthed = false;
	private String errorMessage = "";
	private Set<Character> blankChars = new HashSet<Character>();
/**
 * Appending the input data 
 * @param sapBoFunction
 */
	public Lexer(String sapBoFunction) {

		input.append(sapBoFunction);

		blankChars.add('\r');
		blankChars.add('\n');
		blankChars.add((char) 8);
		blankChars.add((char) 9);
		blankChars.add((char) 11);
		blankChars.add((char) 12);
		blankChars.add((char) 32);

		moveAhead();
	}
	/**
	 * Continue with the input text up to last token
	 */

	public void moveAhead() {
		if (exausthed) {
			return;
		}

		if (input.length() == 0) {
			exausthed = true;
			return;
		}

		ignoreWhiteSpaces();

		if (findNextToken()) {
			return;
		}

		exausthed = true;

		if (input.length() > 0) {
			errorMessage = "Unexpected symbol: '" + input.charAt(0) + "'";
		}
	}
	/**
	 * Ignoring the white spaces in the input
	 */
	private void ignoreWhiteSpaces() {
		int charsToDelete = 0;

		while (blankChars.contains(input.charAt(charsToDelete))) {
			charsToDelete++;
		}

		if (charsToDelete > 0) {
			input.delete(0, charsToDelete);
		}
	}
	/**
	 * Method to find the next token in the input
	 * @return
	 */
	private boolean findNextToken() {
		for (Token t : Token.values()) {
			int end = t.endOfMatch(input.toString());

			if (end != -1) {
				token = t;
				lexema = input.substring(0, end);
				input.delete(0, end);
				return true;
			}
		}

		return false;
	}

	public Token currentToken() {
		return token;
	}

	public String currentLexema() {
		return lexema;
	}

	public boolean isSuccessful() {
		return errorMessage.isEmpty();
	}

	public String errorMessage() {
		return errorMessage;
	}

	public boolean isExausthed() {
		return exausthed;
	}
}
