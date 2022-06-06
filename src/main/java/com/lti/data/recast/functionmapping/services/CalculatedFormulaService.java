package com.lti.data.recast.functionmapping.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lti.data.recast.functionmapping.model.CalculatedFormulaModel;
import com.lti.data.recast.functionmapping.model.FunctionModel;
import com.lti.data.recast.functionmapping.repository.FunctionRepository;

@Service

public class CalculatedFormulaService {
	LinkedHashMap<String, String> boToTabMap = new LinkedHashMap<String, String>();
	ArrayList<String> available = new ArrayList<String>();
	int count = 0;

	@Autowired
	FunctionRepository functionRepository;

	/**
	 * Method for converting sapBo function to Tableau function
	 * 
	 * @param request
	 * @param tableauResponse
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public List<CalculatedFormulaModel> boToTableauConversion(CalculatedFormulaModel request,
			List<CalculatedFormulaModel> tableauResponse) throws IOException {
		available = functionRepository.findAlltableau_Function();
		int count1 = 0;
		String input = request.getFormula();

		List<FunctionModel> p = functionRepository.findAll();
		HashMap<String, String> hashMap = new HashMap<>();
		for (FunctionModel model : p) {
			hashMap.put(model.getSapbo_Function(), model.getTableau_Function());
		}
		HashMap<String, String> stringReplacementMap = new HashMap<>();

		Lexer lexer = new Lexer(input);

		LinkedList<String> indentifierList = new LinkedList<>();
		LinkedList<String> TK_OPEN_List = new LinkedList<>();
		StringBuilder sb = null;
		StringBuilder orig = null;
		int lastStringPosition = 0;
		int count = 0;
		int ifCounter = 0;
		int elseIfCounter = 0;
		int openCounter = 0;
		boolean finalClosureDue = false;
		boolean isIFThen = false;
		String lastToken = "";
		String example = "";
		String data = null;
		StringBuilder sb1 = new StringBuilder();

		while (!lexer.isExausthed()) {
			count++;

			if ((lexer.currentLexema()).equalsIgnoreCase("=")) {
				if (ifCounter == 0) {
					if (sb != null) {
						data = endOfLineCleanUp(finalClosureDue, elseIfCounter, sb.toString());

						finalClosureDue = false;
						elseIfCounter = 0;
						lastStringPosition = 0;
						count = 0;
						openCounter = 0;
						isIFThen = false;
						lastToken = "";
						indentifierList.clear();
						boToTabMap.put(orig.toString(), data);
						sb1.append(data);
					}
					sb = new StringBuilder();
					sb.append(lexer.currentLexema());

					orig = new StringBuilder();
				} else {

					sb.append(lexer.currentLexema());
				}
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_SEMI")) {

				sb.append(hashMap.get(lexer.currentToken().name()));

			}

			else if (lexer.currentToken().name().equalsIgnoreCase("TK_PLUS")) {
				if (lastStringPosition + 1 == count) {
					sb.append(" ");
					sb.append(hashMap.get(lexer.currentToken().name()));
					sb.append(" ");
					lastStringPosition = count;
				} else {
					sb.append(lexer.currentLexema());
				}
			} else if (lexer.currentToken().name().equalsIgnoreCase("NUMBER")
					|| lexer.currentToken().name().equalsIgnoreCase("INTEGER")) {
				if (lastStringPosition + 1 == count) {
					lastStringPosition = count;
					int lastPlusPosition = sb.lastIndexOf("+");
					if (lastPlusPosition > 0 && sb.length() - lastPlusPosition <= 3) {
						sb.replace(lastPlusPosition, lastPlusPosition + 1, " + ");
					}
				}
				sb.append(lexer.currentLexema());
			} else if (lexer.currentToken().name().equalsIgnoreCase("EMPTY_STRING")) {
				sb.append(lexer.currentLexema());
			} else if (lexer.currentToken().name().equalsIgnoreCase("STRING")) {
				lastStringPosition = count;
				int lastPlusPosition = sb.lastIndexOf("+");
				if (lastPlusPosition > 0 && sb.length() - lastPlusPosition <= 3) {
					sb.replace(lastPlusPosition, lastPlusPosition + 1, " + ");
				}

				sb.append(stringReplacementMap.containsKey(lexer.currentLexema())
						? stringReplacementMap.get(lexer.currentLexema())
						: lexer.currentLexema());

			} else if (lexer.currentToken().name().equalsIgnoreCase("IDENTIFIER")) {
				indentifierList.addLast(lexer.currentLexema());

				if (lastToken.equalsIgnoreCase("SQUR_OPEN_BRACKET")) {

					sb.replace(sb.lastIndexOf("["), sb.length(), "" + "" + "[");
					openCounter++;
				}

				if (lastToken.equalsIgnoreCase("IDENTIFIER") || lastToken.equalsIgnoreCase("STRING")) {

					sb.append(hashMap.containsKey(lexer.currentLexema()) ? " " + hashMap.get(lexer.currentLexema())
							: " " + lexer.currentLexema());
				} else {

					sb.append(hashMap.containsKey(lexer.currentLexema().toUpperCase())
							? hashMap.get(lexer.currentLexema().toUpperCase())
							: lexer.currentLexema().toUpperCase());

				}

			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_OPEN")) {

				sb.append(lexer.currentLexema());
				openCounter++;
				TK_OPEN_List.addLast(lastToken);

			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_CLOSE")) {
				openCounter--;
				String functionName = TK_OPEN_List.getLast();
				TK_OPEN_List.removeLast();
				if (indentifierList.size() >= 1) {
					sb.append(hashMap.containsKey(lexer.currentToken().name() + "__" + indentifierList.getLast())
							? hashMap.get(lexer.currentToken().name() + "__" + indentifierList.getLast())
							: lexer.currentLexema());
					indentifierList.removeLast();
				} else {
					if (orig.toString().contains("EVEN") || orig.toString().contains("ODD")) {

					}
					sb.append(lexer.currentLexema());
				}
				if (functionName.equalsIgnoreCase("TK_NOFILTER")) {

					sb.replace(sb.lastIndexOf(")"), sb.length(), ", ALL(" + "" + ")");
				}
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_KEY_IF")) {
				ifCounter++;
				sb.append(lexer.currentLexema().trim());
				sb.append("_{" + ifCounter + "}");
				isIFThen = false;
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_KEY_THEN")) {
				sb.append(",TK_KEY_THEN");
				sb.append("_{" + ifCounter + "}");
				isIFThen = true;
				ifCounter--;
				if (ifCounter == 0 && !finalClosureDue)
					finalClosureDue = true;
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_KEY_ELSE")) {
				sb.append(",TK_KEY_ELSE");
				if (!isIFThen) {
					sb.append("_{" + ifCounter + "}");
					ifCounter--;
					if (ifCounter == 0 && !finalClosureDue)
						finalClosureDue = true;
					isIFThen = false;
				} else {
					sb.append("_{" + (ifCounter + 1) + "}");
				}
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_KEY_ELSE_IF")) {
				sb.append(",");
				if (isIFThen) {
					ifCounter++;
				}
				sb.append("If(");

				elseIfCounter++;
				finalClosureDue = true;
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_NOFILTER")) {
				sb.append(" CALCULATE");
			} else if (lexer.currentToken().name().equalsIgnoreCase("SQUR_OPEN_BRACKET")) {
				if (ifCounter > 0) {
					int lastIfPosition = sb.lastIndexOf("If");
					int lastOpenBracketPosition = sb.lastIndexOf("(");
					if (lastOpenBracketPosition > 0 && sb.length() - lastIfPosition <= 4) {

						sb.append(lexer.currentLexema().trim());
					} else if (sb.length() - lastIfPosition <= 3) {
						sb.append("(");
						openCounter++;
						sb.append(lexer.currentLexema().trim());
					} else {
						sb.append(lexer.currentLexema().trim());
					}
				} else {
					sb.append(lexer.currentLexema());
				}
			} else if (lexer.currentToken().name().equalsIgnoreCase("SQUR_CLOSE_BRACKET")) {
				sb.append(lexer.currentLexema());
				if (lastToken.equalsIgnoreCase("IDENTIFIER")) {

					openCounter--;
				}
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_IS_NULL")) {
				sb.append(" ISBLANK");
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_AND")) {
				sb.append(" && ");
			} else if (lexer.currentToken().name().equalsIgnoreCase("TK_OR")) {
				sb.append(" || ");
			} else {
				sb.append(lexer.currentLexema());
			}

			lastToken = lexer.currentToken().name();
			example = lexer.currentToken().toString();
			orig.append(lexer.currentLexema());
			lexer.moveAhead();
		}

		if (lexer.isSuccessful()) {
			data = endOfLineCleanUp(finalClosureDue, elseIfCounter, sb.toString());

			finalClosureDue = false;
			elseIfCounter = 0;
			lastStringPosition = 0;
			count = 0;
			ifCounter = 0;
			openCounter = 0;
			isIFThen = false;
			lastToken = "";

			boToTabMap.put(orig.toString(), data);
			sb1.append(data);

		}

		request.setCalculatedFormula(sb1.toString());
		if (count1 == 0) {
			tableauResponse.add(request);
			count1++;
		}
		return tableauResponse;
	}

	/*
	 * End of line set the closing bracket, replace IF, Then, Else token etc.
	 */
	private String endOfLineCleanUp(boolean finalClosureDue, int elseIfCounter, String str) {
		StringBuilder sb = new StringBuilder();
		sb.append(str);
		if (finalClosureDue) {

			for (int i = 0; i < elseIfCounter; i++) {
				sb.append(")");
			}
		}
		finalClosureDue = false;
		elseIfCounter = 0;
		String data = sb.toString();

		String key = (data.lastIndexOf("TK_KEY_ELSE_{") > data.lastIndexOf("TK_KEY_THEN_{") ? "TK_KEY_ELSE_{"
				: "TK_KEY_THEN_{");

		if (key.equalsIgnoreCase("TK_KEY_THEN_{") && data.lastIndexOf("TK_KEY_THEN_{") > 0) {
			int lastThen = data.lastIndexOf("TK_KEY_THEN_{");
			String post_fix = getPostFix(data, lastThen, key);
			data = replaceLast(data, "TK_KEY_THEN" + post_fix, "TK_END_THEN" + post_fix);
			data = iFThenElseEndBracketCalculation(data, "TK_END_THEN_{");
		}
		int total = countMatches(data, key);

		for (int i = 0; i < total; i++) {
			data = iFThenElseEndBracketCalculation(data, key);
		}
		if (data.lastIndexOf("TK_KEY_THEN_{") > 0) {
			total = countMatches(data, "TK_KEY_THEN_{");
			data = iFThenElseEndBracketCalculation(data, "TK_KEY_THEN_{");
		}
		if (!available.contains(data)) {
			int position = data.indexOf("(");

			if (position == -1) {
				data = "=INVALID FUNCTION NAME";
			}
			Pattern p1 = Pattern.compile("[A-Z\\s_a-z]+\\(");

			Matcher m1 = p1.matcher(data);

			while (m1.find()) {
				String exactdata = m1.group().substring(0, m1.group().indexOf("("));

				if (!available.contains(exactdata)) {
					data = "=INVALID FUNCTION NAME";
				}

			}
		}
		if (data.contains("=INVALID")) {
			return data;
		} else {
			if (data.contains("If(")) {
				int count = 0;
				Pattern p3 = Pattern.compile(",");
				Matcher m3 = p3.matcher(data);
				while (m3.find()) {
					count++;
				}
				if (!data.contains("IN(")) {
					if (count >= 2) {
						Pattern p = Pattern.compile("(?:^|\\W)If(?:$|\\W)");
						Matcher m = p.matcher(data);
						while (m.find()) {
							data = data.replace(m.group(), "IIF(");
						}
						Pattern p1 = Pattern.compile("\\)\\)+");
						Matcher m1 = p1.matcher(data);
						while (m1.find()) {
							data = data.replace(m1.group(), ")");
						}
						Pattern p2 = Pattern.compile("\\(\\(+");
						Matcher m2 = p2.matcher(data);
						while (m2.find()) {
							data = data.replace(m2.group(), "(");
						}

						int count1 = 0;
						Pattern p4 = Pattern.compile("\\(");
						Matcher m4 = p4.matcher(data);
						while (m4.find()) {
							count1++;
						}
						int count2 = 0;
						Pattern p5 = Pattern.compile("\\)");
						Matcher m5 = p5.matcher(data);
						while (m5.find()) {
							count2++;
						}
						if (count1 != count2) {
							int position = data.indexOf(")");
							int position1 = data.lastIndexOf(")");
							if (position != position1) {
								data = data.substring(0, position) + data.substring(position + 1, data.length());

							}
						}

					}

					else {

						data = data.substring(0, data.length() - 1).replace(",", " Then ").replace("(", " ")
								.replace(")", "") + " End ";
					}
				}
			}
			if (data.contains("WEEK(")) {
				int count = 0;
				int position = data.indexOf("WEEK(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String exactdata = data.substring(position + 5, position1);
				String exactdata1 = data.substring(0, position2);
				String data1[] = exactdata.split(",");
				String data2[] = exactdata1.split("\\(");

				for (int i = 0; i < data2.length; i++) {

					// System.out.println(data2[i]);
					if (available.contains(data2[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == data2.length) {
					// System.out.println("work");
					data = (data.substring(0, position + 5) + "\"week\"," + exactdata).replace("WEEK", "DATEPART")
							+ data.substring(position1, data.length());
					available.add(data);
				}
			}

			if (data.contains("TODATE(")) {
				int count = 0;
				int position = data.indexOf("TODATE(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String exactdata = data.substring(position + 7, position1);
				String exactdata1 = data.substring(0, position2);
				String data1[] = exactdata.split(",");
				String data2[] = exactdata1.split("\\(");
				exactdata = data1[1] + "," + data1[0];
				for (int i = 0; i < data2.length; i++) {

					// System.out.println(data2[i]);
					if (available.contains(data2[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == data2.length) {
					// System.out.println("work");
					data = (data.substring(0, position + 7) + exactdata).replace("TODATE", "DATEPARSE")
							+ data.substring(position1, data.length());
					available.add(data);
				}
			}

			if (data.contains("RELATIVEDATE(")) {
				int count = 0;
				int position = data.indexOf("RELATIVEDATE(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				int position3 = data.lastIndexOf(",");

				String exactdata = data.substring(position + 13, position3);

				String exactdata1 = data.substring(position + 13, position1);
				// System.out.println(exactdata1);
				String aa = data.substring(0, position2);
				String data1[] = aa.split("\\(");
				String data2[] = exactdata1.split(",");
				// System.out.println(data2.length);
				if (data2.length <= 2) {

					for (int i = 0; i < data1.length; i++) {

						if (available.contains(data1[i].replace("=", ""))) {
							count++;
						}
					}
					if (count == data1.length) {
						data = (data.substring(0, position + 13) + "\"month\","
								+ data.substring(position3 + 1, position1) + "," + exactdata
								+ data.substring(position1, data.length())).replace("RELATIVEDATE", "DATEADD");
						available.add(data);
					}
				} else {
					exactdata = "\"" + data2[2] + "\"," + data2[1] + "," + data2[0];
					data = data.substring(0, position + 13) + exactdata + data.substring(position1, data.length());

					data = data.replace("RELATIVEDATE", "DATEADD").replace("MONTHPERIOD", "month")
							.replace("WEEKPERIOD", "week").replace("QUARTERPERIOD", "quarter");
					available.add(data);
				}
			}

			if (data.contains("MONTHSBETWEEN(")) {
				int count = 0;
				int position = data.indexOf("MONTHSBETWEEN(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String exactdata = data.substring(position + 14, position1);
				// System.out.println(exactdata);
				String aa = data.substring(0, position2);
				System.out.println(aa);
				String data1[] = aa.split("\\(");
				System.out.println(data1.length);
				for (int i = 0; i < data1.length; i++) {
					System.out.println(data1[i]);
					if (available.contains(data1[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == data1.length) {
					data = (data.substring(0, position + 14) + "\"month\"," + exactdata
							+ data.substring(position1, data.length())).replace("MONTHSBETWEEN", "DATEDIFF");

					available.add(data);
				}
			}

			if (data.contains("MONTH(")) {
				int count = 0;
				int position = data.indexOf("MONTH(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String exactdata = data.substring(position + 6, position1);

				String aa = data.substring(0, position2);

				String data1[] = aa.split("\\(");

				for (int i = 0; i < data1.length; i++) {

					if (available.contains(data1[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == data1.length) {
					data = (data.substring(0, position + 6) + "\"month\"," + exactdata
							+ data.substring(position1, data.length())).replace("MONTH", "DATENAME");

					available.add(data);
				}
			}

			if (data.contains("MONTHNUMBEROFYEAR(")) {
				int count = 0;
				int position = data.indexOf("MONTHNUMBEROFYEAR(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String exactdata = data.substring(position + 18, position1);
				// System.out.println(exactdata);
				String aa = data.substring(0, position2);
				// System.out.println(aa);
				String data1[] = aa.split("\\(");
				System.out.println(data1.length);
				for (int i = 0; i < data1.length; i++) {
					// System.out.println(data1[i]);
					if (available.contains(data1[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == data1.length) {
					data = (data.substring(0, position + 18) + exactdata + data.substring(position1, data.length()))
							.replace("MONTHNUMBEROFYEAR", "MONTH");

					available.add(data);
				}
			}

			if (data.contains("LASTDAYOFMONTH(")) {
				int count = 0;
				int position = data.indexOf("LASTDAYOFMONTH");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");

				String exactdata = data.substring(position + 15, position1);
				String aa = data.substring(0, position2);
				String data1[] = aa.split("\\(");
				System.out.println(data1.length);
				for (int i = 0; i < data1.length; i++) {
					// System.out.println(data1[i]);
					if (available.contains(data1[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == data1.length) {
					data = data.substring(0, position + 15) + "\"day\",-1,DATEADD(\"month\",1,DATETRUNC(\"month\","
							+ exactdata + "))" + data.substring(position1, data.length());
					data = data.replace("LASTDAYOFMONTH", "DATEADD");
					available.add(data);
				}
			}

			if (data.contains("DAYNAME(")) {
				System.out.println("work");
				int position = data.indexOf("DAYNAME(");
				data = (data.substring(0, position + 8) + "\"weekday\"," + data.substring(position + 8, data.length()))
						.replace("DAYNAME", "DATENAME");
				available.add(data);
			}

			if (data.contains("DATESBETWEEN(")) {
				int position = data.indexOf("DATESBETWEEN(");
				int position1 = data.indexOf(")");
				String exactdata = data.substring(position + 13, position1);
				String data1[] = exactdata.split(",");

				exactdata = "\"" + data1[2] + "\"," + data1[0] + "," + data1[1];

				data = data.substring(0, position + 13) + exactdata + data.substring(position1, data.length());
				data = data.replace("DATESBETWEEN", "DATEIFF").replace("MONTHPERIOD", "month")
						.replace("DAYPERIOD", "day").replace("WEEKPERIOD", "week").replace("QUARTERPERIOD", "quarter");
				available.add(data);
			}

			if (data.contains("DAYSBETWEEN(")) {

				int count = 0;
				int position = data.indexOf("DAYSBETWEEN(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String aa = data.substring(0, position2);
				String exactdata = data.substring(position + 12, position1);
				String data1[] = aa.split("\\(");
				for (int i = 0; i < data1.length; i++) {

					if (available.contains(data1[i].replace("=", ""))) {
						count++;
					}

				}
				if (count == data1.length) {
					exactdata = "\"day\"," + exactdata;
					data = (data.substring(0, position + 12) + exactdata + data.substring(position1, data.length())
							+ "%30").replace("DAYSBETWEEN", "DATEDIFF");
					available.add(data);
				}
			}

			if (data.contains("AVG(")) {
				if (data.contains(",")) {
					data = "FUNCTION IS NOT AVAILABLE IN THE CURRENT TABLEAU VERSION";
				}
			}
			if (data.contains("FIND(")) {
				int position = data.indexOf("FIND(");
				int position1 = data.indexOf(")");
				String exactdata = data.substring(position + 7, position1);
				String data1[] = exactdata.split(",");

				if (StringUtils.isNumeric(data1[data1.length - 1]) && StringUtils.isNumeric(data1[data1.length - 2])) {
					data = "FUNCTION IS NOT AVAILABLE IN THE CURRENT TABLEAU VERSION";
				}
			}
			if (data.contains("LTRIM(")) {
				int position = data.indexOf("LTRIM(");
				int position1 = data.indexOf(")");
				String exactdata = data.substring(position + 6, position1);

				String data1[] = exactdata.split(",");
				if (data1.length > 1) {
					data = "FUNCTION IS NOT AVAILABLE IN THE CURRENT TABLEAU VERSION";
				}
			}

			if (data.contains("RTRIM(")) {
				int position = data.indexOf("RTRIM(");
				int position1 = data.indexOf(")");
				String exactdata = data.substring(position + 6, position1);

				String data1[] = exactdata.split(",");
				if (data1.length > 1) {
					data = "FUNCTION IS NOT AVAILABLE IN THE CURRENT TABLEAU VERSION";
				}
			}

			if (data.contains("PERCENTAGE")) {

				int position = data.lastIndexOf("PERCENTAGE");
				String exactdata = "";
				exactdata = data.toString().substring(0, position);

				String sample = "";
				char sample1 = 0;
				char sample2 = 0;
				if (position > 1) {
					sample = data.toString().substring(position + 10, data.toString().length() - 2);
					sample1 = data.charAt(data.toString().length() - 1);
					sample2 = data.charAt(data.toString().length() - 2);
					data = exactdata + sample + "/100" + sample1 + sample2;
				} else {
					sample = data.toString().substring(position + 10, data.toString().length() - 1);
					sample1 = data.charAt(data.toString().length() - 1);
					data = exactdata + sample + "/100" + sample1;
				}

			}

			if (data.contains("COUNT(")) {

				String data1[];
				int position = data.indexOf("COUNT(");
				int position1 = data.indexOf(")");

				String exactdata = data.substring(position + 6, position1);
				if (exactdata.contains(",")) {
					data1 = exactdata.split(",");
					if (data1.length > 1) {
						if (data1[1].equalsIgnoreCase("DISTINCT")) {
							data = data.replace("COUNT", "COUNTD");
							int pos = data.indexOf(",");
							data = data.substring(0, pos) + data.charAt(data.length() - 1);

						} else {
							data = "FUNCTION IS NOT AVAILABLE IN THE CURRENT TABLEAU VERSION";
						}
					}
				}
			}

			if (data.contains("FILL(")) {
				int position = data.indexOf("FILL(");
				int position1 = data.indexOf(")");

				String data2[];
				data2 = data.split(",");
				String doubleQuote = doubleQuoteValue(data);

				data = data.substring(0, position) + "REPLACE(SPACE(" + data2[1] + ",\" \"," + doubleQuote
						+ data.substring(position1, data.length());

			}
			if (data.contains("INITCAP(")) {

				int position = data.indexOf("INITCAP(");
				int position1 = data.indexOf(")");

				String exactdata = data.substring(position + 8, position1);
				data = data.charAt(0) + "UPPER(LEFT(" + exactdata + ",1))" + " + MID(" + exactdata + ",2)";

			}

			if (data.contains("WORDCAP")) {
				String doubleQuote = doubleQuoteValue(data);
				data = data.charAt(0) + "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE("
						+ "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE("
						+ "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE("
						+ "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE("
						+ "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE("
						+ "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE("
						+ "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE("
						+ "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(" + "REGEXP_REPLACE(REGEXP_REPLACE("
						+ doubleQuote + ",'(?<=^|\\s)a\','A') ,'(?<=^|\\s)b','B'),'(?<=^|\\s)c','C'),"
						+ "'(?<=^|\\s)d','D'),'(?<=^|\\s)e','E'),'(?<=^|\\s)f','F'),'(?<=^|\\s)g','G'),"
						+ "'(?<=^|\\s)h','H'),'(?<=^|\\s)i','I'),'(?<=^|\\s)j','J'),'(?<=^|\\s)k','K'),"
						+ "'(?<=^|\\s)l','L'),'(?<=^|\\s)m','M'),'(?<=^|\\s)n','N'),'(?<=^|\\s)o','O'),"
						+ "'(?<=^|\\s)p','P'),'(?<=^|\\s)q','Q'),'(?<=^|\\s)r','R'),'(?<=^|\\s)s','S'),"
						+ "'(?<=^|\\s)t','T'),'(?<=^|\\s)u','U'),'(?<=^|\\s)v','V'),'(?<=^|\\s)w','W'),"
						+ "'(?<=^|\\s)x','X'),'(?<=^|\\s)y','Y'),'(?<=^|\\s)z','Z')";

			}
			if (data.contains("MOD(")) {
				int position = data.indexOf("MOD(");
				if (position == 1) {

					int position1 = data.lastIndexOf(",");
					StringBuilder str1 = new StringBuilder(data);
					str1.setCharAt(position1, '%');
					data = str1.toString();
				} else {
					int position1 = data.indexOf(",");
					StringBuilder str1 = new StringBuilder(data);
					str1.setCharAt(position1, '%');
					data = str1.toString();
				}
				data = data.replace("MOD", "");

			}

			if (data.contains("EVEN(")) {

				int position = data.indexOf("EVEN");
				int position1 = data.indexOf(")");

				if (position > 1) {
					data = data.substring(0, position) + "IF CONTAINS(STR(" + data.substring(position + 5, position1)
							+ "),\".\") THEN \"FALSE\" ELSE IIF(INT(" + data.substring(position + 5, position1)
							+ ")%2!=0,\"FALSE\",\"TRUE\")) END";

				} else {
					data = data.substring(0, position) + "IF CONTAINS(STR(" + data.substring(position + 5, position1)
							+ "),\".\") THEN \"FALSE\" ELSE IIF(INT(" + data.substring(position + 5, position1)
							+ ")%2!=0,\"FALSE\",\"TRUE\") END";

				}

			}

			if (data.contains("ODD(")) {
				int position = data.indexOf("ODD");
				int position1 = data.indexOf(")");

				if (position > 1) {
					data = data.substring(0, position) + "IF CONTAINS(STR(" + data.substring(position + 4, position1)
							+ "),\".\") THEN \"TRUE\" ELSE IIF(INT(" + data.substring(position + 4, position1)
							+ ")%2!=0,\"TRUE\",\"FALSE\")) END";
				}
				else {
					data = data.charAt(0) + "IF CONTAINS(STR(" + data.substring(position + 4, position1)

							+ "),\".\") THEN \"TRUE\" ELSE IIF(INT(" + data.substring(position + 4, position1)
							+ ")%2!=0,\"TRUE\",\"FALSE\") END";
				}
			}
			if (data.contains("ISNUMBER(")) {
				int count = 0;
				int position = data.indexOf("ISNUMBER(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String s[] = data.substring(0, position2).split("\\(");
				for (int i = 0; i < s.length; i++) {
					if (available.contains(s[i].replace("=", ""))) {
						count++;
					}
				}
				String exactdata = data.substring(position + 9, position1);
				if (count == s.length) {
					data = data.substring(0, position) + "IIF(ISNULL(INT(" + exactdata
							+ data.substring(position1, data.length()) + "),\"FALSE\",\"TRUE\"";
				}
			}

			if (data.contains("ISSTRING(")) {
				int count = 0;
				int position = data.indexOf("ISSTRING(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String s[] = data.substring(0, position2).split("\\(");
				for (int i = 0; i < s.length; i++) {
					if (available.contains(s[i].replace("=", ""))) {
						count++;
					}
				}

				String exactdata = data.substring(position + 9, position1);
				if (count == s.length) {
					data = data.substring(0, position) + "IIF(ISNULL(INT(" + exactdata
							+ data.substring(position1, data.length()) + "),\"TRUE\",\"FALSE\")";
				}
			}
			if (data.contains("RUNNING_AVG")) {
				int count = 0;
				int position = data.indexOf("RUNNING_AVG(");
				int position1 = data.indexOf(")");
				int position2 = data.lastIndexOf("(");
				String s[] = data.substring(0, position2).split("\\(");
				for (int i = 0; i < s.length; i++) {
					if (available.contains(s[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == s.length) {
					data = data.substring(0, position + 12) + "SUM(" + data.substring(position + 12, data.length())
							+ ")";
				} else {
					data = "=INVALID FUNCTION NAME";
				}
			}
			if (data.contains("RUNNING_COUNT"))
			{
				int count = 0;
				int position = data.indexOf("RUNNING_COUNT(");
				int position2 = data.lastIndexOf("(");
				String s[] = data.substring(0, position2).split("\\(");
				for (int i = 0; i < s.length; i++) {
					if (available.contains(s[i].replace("=", ""))) {
						count++;
					}
				}

				if (count == s.length) {
					data = data.substring(0, position + 14) + "SUM(" + data.substring(position + 14, data.length())
							+ ")";
				}
			}
			if (data.contains("RUNNING_MAX"))
			{
				int count = 0;
				int position = data.indexOf("RUNNING_MAX(");
				int position2 = data.lastIndexOf("(");
				String s[] = data.substring(0, position2).split("\\(");
				for (int i = 0; i < s.length; i++) {
					if (available.contains(s[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == s.length) {
					data = data.substring(0, position + 12) + "SUM(" + data.substring(position + 12, data.length())
							+ ")";
				}
			}
			if (data.contains("RUNNING_MIN"))
			{
				int count = 0;
				int position = data.indexOf("RUNNING_MIN(");
				int position2 = data.lastIndexOf("(");
				String s[] = data.substring(0, position2).split("\\(");
				for (int i = 0; i < s.length; i++) {
					if (available.contains(s[i].replace("=", ""))) {
						count++;
					}
				}

				if (count == s.length) {
					data = data.substring(0, position + 12) + "SUM(" + data.substring(position + 12, data.length())
							+ ")";
				}
			}
			if (data.contains("RUNNING_SUM"))
			{
				int count = 0;
				int position = data.indexOf("RUNNING_SUM(");
				int position2 = data.lastIndexOf("(");
				String s[] = data.substring(0, position2).split("\\(");
				for (int i = 0; i < s.length; i++) {
					if (available.contains(s[i].replace("=", ""))) {
						count++;
					}
				}
				if (count == s.length) {
					data = data.substring(0, position + 12) + "SUM(" + data.substring(position + 12, data.length())
							+ "))";
				}
			}
			if (data.contains("NOT AVAILABLE")) {
				data = "=FUNCTION IS NOT AVAILABLE IN THE CURRENT TABLEAU VERSION";
			}
			return data;
		}

	}

	/**
	 * Checks if a string is empty ("") or null.
	 **/
	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	/**
	 * Counts how many times the substring appears in the larger string.
	 **/
	public static int countMatches(String text, String str) {
		if (isEmpty(text) || isEmpty(str)) {
			return 0;
		}

		int index = 0, count = 0;
		while (true) {
			index = text.indexOf(str, index);
			if (index != -1) {
				count++;
				index += str.length();
			} else {
				break;
			}
		}

		return count;
	}

	/**
	 * Checking values which is inside double quotes
	 * 
	 * @param data
	 * @return
	 */

	private static String doubleQuoteValue(String data) {
		String doubleQuote = "";
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher(data);
		while (m.find()) {
			doubleQuote = m.group(0).toString();

		}
		return doubleQuote;
	}

	/**
	 * Here the If End closing bracket placed after end of word.
	 * 
	 * @param str
	 * @param key
	 * @return
	 */
	private static String iFThenElseEndBracketCalculation(String str, String key) {
		int lastElse = str.lastIndexOf(key);
		String post_fix = getPostFix(str, lastElse, key);

		int endBraces = getEndBresesPosition(str, lastElse, key);
		int wordEndPosition = getNextWord(str, endBraces);

		StringBuffer stringBuffer = new StringBuffer(str);
		if (key.equalsIgnoreCase("TK_KEY_ELSE_{")) {

			str = stringBuffer.insert(wordEndPosition, ")").toString();
			str = replaceLast(str, "TK_KEY_ELSE" + post_fix, "");
		}
		if (key.equalsIgnoreCase("TK_END_THEN_{")) {
			str = stringBuffer.insert(wordEndPosition, ")").toString();
			str = replaceLast(str, "TK_END_THEN" + post_fix, "");
		}
		str = replaceLast(str, "TK_KEY_THEN" + post_fix, "");
		str = replaceLast(str, "If" + post_fix, "If(");

		return str;
	}

	/**
	 * Extract the post fix of IF, ELse, then as _{counter} like _{1}, _{3}, _{99}
	 * Note : Token key "TK_END_THEN_{" and "TK_KEY_ELSE_{" both are 13 char. So if
	 * you add different token below number need to change. Now
	 * 
	 * @param str
	 * @param lastElse
	 * @return
	 */
	private static String getPostFix(String str, int lastElse, String key) {
		char c = str.charAt(lastElse + key.length());
		char d = str.charAt(lastElse + key.length() + 1);
		char e = str.charAt(lastElse + key.length() + 2);

		if ((!Character.isDigit(d)) && (Character.toString(d).equalsIgnoreCase("}"))) {
			return (str.substring(lastElse + key.length() - 2, lastElse + key.length() + 2));

		} else if ((!Character.isDigit(e)) && (Character.toString(e).equalsIgnoreCase("}"))) {
			return (str.substring(lastElse + key.length() - 2, key.length() + 3));
		}
		return null;
	}

	/**
	 * Return the position of Last Curly Braces "}" for KEY like "TK_END_THEN_{" and
	 * "TK_KEY_ELSE_{"
	 * 
	 * @param str
	 * @param lastElse
	 * @param key
	 * @return
	 */
	private static int getEndBresesPosition(String str, int lastElse, String key) {
		char d = str.charAt(lastElse + key.length() + 1);
		char e = str.charAt(lastElse + key.length() + 2);
		if ((!Character.isDigit(d)) && (Character.toString(d).equalsIgnoreCase("}"))) {
			return lastElse + key.length() + 1;
		}
		return lastElse + key.length() + 2;
	}

	/**
	 * Extract the END Word or numeric afterIF Else token. IF(A>5,1999, "DES") .
	 * Here 1999 or "DES" or (ADDD) or [FFFF] are extracted
	 * 
	 * @param str
	 * @param endBraces
	 * @return
	 */
	private static int getNextWord(String str, int endBraces) {
		int endPosition = 0;
		char d = str.charAt(endBraces + 1);
		if (Character.isWhitespace(d)) {
			getNextWord(str, endBraces + 1);
		} else {
			char a = str.charAt(endBraces + 1);
			if (endBraces + 2 < str.length()) {
				if (Character.isDigit(a)) {
					for (int i = endBraces + 2; i < str.length(); i++) {
						char x = str.charAt(i);
						if (Character.isWhitespace(x)) {
							endPosition = i;
							break;
						}
					}
					endPosition = str.length();
				} else if (a == '"') {
					for (int i = endBraces + 2; i < str.length(); i++) {
						char x = str.charAt(i);
						if (x == '"') {
							endPosition = i + 1;
							break;
						}
					}
				} else if (a == '(') {
					int count = 0;
					for (int i = endBraces + 2; i < str.length(); i++) {
						char x = str.charAt(i);
						if (x == '(') {
							count++;
						}
						if (x == ')') {
							if (count == 0) {
								endPosition = i + 1;
								break;
							} else {
								count--;
							}
						}
					}
				} else if (a == '[') {
					int count = 0;
					for (int i = endBraces + 2; i < str.length(); i++) {
						char x = str.charAt(i);
						if (x == '[') {
							count++;
						}
						if (x == ']') {
							if (count == 0) {
								endPosition = i + 1;
								break;
							} else {
								count--;
							}
						}
					}
				} else if (a == '{') {
					int count = 0;
					for (int i = endBraces + 2; i < str.length(); i++) {
						char x = str.charAt(i);
						if (x == '{') {
							count++;
						}
						if (x == '}') {
							if (count == 0) {
								endPosition = i + 1;
								break;
							} else {
								count--;
							}
						}
					}
				}

			} else {
				endPosition = str.length();
			}
		}

		return endPosition;
	}

	/**
	 * Replace last match from a line.
	 * 
	 * @param string
	 * @param toReplace
	 * @param replacement
	 * @return
	 */
	public static String replaceLast(String string, String toReplace, String replacement) {
		int pos = string.lastIndexOf(toReplace);
		if (pos > -1) {
			return string.substring(0, pos) + replacement + string.substring(pos + toReplace.length());
		} else {
			return string;
		}
	}

}
