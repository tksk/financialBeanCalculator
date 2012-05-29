package org.tksk.fbc;

import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalSupport {

	public BigDecimal compute(String expression) throws ArithmeticException {
		return Bdu.compute(expression, MathContext.UNLIMITED, this);
	}

	public BigDecimal compute(String expression, MathContext mc) throws ArithmeticException {
		return Bdu.compute(expression, MathContext.UNLIMITED, this);
	}
}
