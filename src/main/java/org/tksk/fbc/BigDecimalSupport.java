package org.tksk.fbc;

import java.math.BigDecimal;
import java.math.MathContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigDecimalSupport {

	private static Logger logger = LoggerFactory.getLogger(BigDecimalSupport.class);

	public BigDecimal compute(String expression) throws ArithmeticException {
		return Bdu.compute(expression, MathContext.UNLIMITED, this);
	}

	public BigDecimal compute(String expression, MathContext mc) throws ArithmeticException {
		return Bdu.compute(expression, MathContext.UNLIMITED, this);
	}
}
