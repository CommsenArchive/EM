package com.commsen.em.demo.markup;

import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.commsen.em.demo.calc.api.Calculator;

public class Markup {

	ServiceLoader<Calculator> serviceLoader = ServiceLoader.load(Calculator.class);

	Pattern EXPR = Pattern.compile("\\<math\\>(?<exp>.*?)\\<\\/math\\>");

	public String transform(String input) {
		Matcher m = EXPR.matcher(input);
		StringBuffer sb = new StringBuffer();
		Calculator calc = serviceLoader.iterator().next();
		while (m.find()) {
			m.appendReplacement(sb, "" + calc.calculate(m.group("exp")));
		}
		m.appendTail(sb);
		return sb.toString();
	}

}
