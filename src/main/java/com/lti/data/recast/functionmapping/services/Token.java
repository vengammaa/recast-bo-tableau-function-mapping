package com.lti.data.recast.functionmapping.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Token {

	TK_MINUS("-"), TK_PLUS("\\+"), TK_MUL("\\*"), TK_DIV("/"), TK_NOT("~"), TK_AND("And"), TK_OR("Or"), TK_LESS("<"),
	TK_LEG("<="), TK_GT(">"), TK_GEQ(">="), TK_EQ("=="), TK_ASSIGN("="), TK_OPEN("\\("), TK_CLOSE("\\)"), TK_SEMI(";"),
	TK_COMMA(","), TK_KEY_DEFINE("define"), TK_KEY_AS("as"), TK_IS_NULL("IsNull"), TK_KEY_IS("is"),
	TK_KEY_ELSE_IF("ElseIf"), TK_KEY_IF("If"), TK_KEY_THEN("Then"), TK_KEY_ELSE("Else"), TK_KEY_ENDIF("endif"),
	OPEN_BRACKET("\\{"), CLOSE_BRACKET("\\}"), SQUR_OPEN_BRACKET("\\["), SQUR_CLOSE_BRACKET("\\]"), DIFFERENT("<>"),
	TK_NOFILTER("NoFilter"),

	STRING("\"[^\"]*\""), NUMBER("(\\d*)\\.\\d+"), INTEGER("\\d+"), IDENTIFIER("\\w+");

	private final Pattern pattern;

	Token(String regex) {
		pattern = Pattern.compile("^" + regex);
	}

	int endOfMatch(String s) {
		Matcher m = pattern.matcher(s);

		if (m.find()) {
			return m.end();
		}
		return -1;
	}
}
