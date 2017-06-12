package com.commsen.em.demo.calc.simple;

import com.commsen.em.demo.calc.api.Calculator;

public class Main {

	public static void main(String[] args) {
		
		Calculator calculator = new SimpleCalculator();
		System.out.println(calculator.calculate(args[0]));

	}

}
