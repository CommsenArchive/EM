package com.commsen.em.demo.calc.simple;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.commsen.em.demo.calc.api.Calculator;

public class SimpleCalculator implements Calculator {

	Pattern EXPR = Pattern.compile("\\s*(?<left>\\d+\\.?\\d*)\\s*(?<op>\\+|-)\\s*(?<right>\\d+\\.?\\d*)\\s*");

	public Number calculate(String expression) {
		Matcher m = EXPR.matcher(expression);
		if (!m.matches()) {
			throw new IllegalArgumentException("Invalid expression " + expression);
		}

		double left = Double.valueOf(m.group("left"));
		double right = Double.valueOf(m.group("right"));
		switch (m.group("op")) {
		case "+":
			return left + right;
		case "-":
			return left - right;
		}
		return Double.NaN;
	}

}
