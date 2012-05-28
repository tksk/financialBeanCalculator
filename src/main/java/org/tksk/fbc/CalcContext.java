package org.tksk.fbc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

public class CalcContext extends HashMap<String, BigDecimal> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4631224281227128322L;

	public static CalcContext create() {
		return new CalcContext();
	}

	public CalcContext putAnd(String key, BigDecimal value) {
		super.put(key, value);
		return this;
	}

	public CalcContext putAnd(String key, long value) {
		super.put(key, BigDecimal.valueOf(value));
		return this;
	}

	public CalcContext putAnd(String key, long value, int scale) {
		super.put(key, BigDecimal.valueOf(value, scale));
		return this;
	}

	public CalcContext putAnd(String key, int value) {
		super.put(key, BigDecimal.valueOf(value));
		return this;
	}

	public CalcContext putAnd(String key, String value) {
		super.put(key, new BigDecimal(value));
		return this;
	}

	public CalcContext putAnd(String key, String value, MathContext mc) {
		super.put(key, new BigDecimal(value, mc));
		return this;
	}
}
