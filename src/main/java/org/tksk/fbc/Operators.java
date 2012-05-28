package org.tksk.fbc;

import java.math.BigDecimal;
import java.math.MathContext;

import org.tksk.fbc.CalcNode.Operator;

enum Operators implements Operator {
	// --------------------------------------------------------------------
	EQ('=') {
		public BigDecimal calc(CalcNode left, CalcNode right, MathContext mc) {
			return right.value;
		}

		@Override
		public CalcNode optimize(CalcNode left, CalcNode right, MathContext mc) {
			return new CalcNode(this.getIdent(), left, right);
		}
	},

	// --------------------------------------------------------------------
	PLUS('+') {
		public BigDecimal calc(CalcNode left, CalcNode right, MathContext mc) {
			return left.getValue().add(right.getValue(), mc);
		}

		@Override
		public CalcNode optimize(CalcNode left, CalcNode right, MathContext mc) {
			//0 + x => x
			if(CalcNode.isZERO(left)) return right;

			// x + 0 => x
			if(CalcNode.isZERO(right)) return left;
			
        	return new CalcNode(this.getIdent(), left, right);
		}
	},

	// --------------------------------------------------------------------
	MINUS('-') {
		public BigDecimal calc(CalcNode left, CalcNode right, MathContext mc) {
			return left.getValue().subtract(right.getValue(), mc);
		}

		@Override
		public CalcNode optimize(CalcNode left, CalcNode right, MathContext mc) {
			// 0 - x => -x
			if(CalcNode.isZERO(left)) return new CalcNode(right.value.negate(), mc);

			// x - 0 => x
			if(CalcNode.isZERO(right)) return left;

        	return new CalcNode(this.getIdent(), left, right);
		}
	},

	// --------------------------------------------------------------------
	ASTAR('*') {
		public BigDecimal calc(CalcNode left, CalcNode right, MathContext mc) {
			return left.getValue().multiply(right.getValue(), mc);
		}

		@Override
		public CalcNode optimize(CalcNode left, CalcNode right, MathContext mc) {
			// 0 * x => 0
			if(CalcNode.isZERO(left)) return CalcNode.getZero(mc);
        	// 1 * x => x
			if(CalcNode.isONE(left)) return right;

        	// x * 0 => 0
			if(CalcNode.isZERO(right)) return CalcNode.getZero(mc);
        	// x * 1 => x
			if(CalcNode.isONE(right)) return left;

			return new CalcNode(this.getIdent(), left, right);
		}
	},

	// --------------------------------------------------------------------
	SLASH('/') {
		public BigDecimal calc(CalcNode left, CalcNode right, MathContext mc) {
			return left.getValue().divide(right.getValue(), mc);
		}

		@Override
		public CalcNode optimize(CalcNode left, CalcNode right, MathContext mc) {
			// 0 / x => 0
			if(CalcNode.isZERO(left)) return CalcNode.getZero(mc);

        	// throws ArithmeticException
        	//if(right.equals(BigDecimal.ZERO)) return getZero(left.mc);

        	// x / 1 => x
			if(CalcNode.isONE(right)) return left;

			return new CalcNode(this.getIdent(), left, right);
		}
	},

	// --------------------------------------------------------------------
	PERCENT('%') {
		public BigDecimal calc(CalcNode left, CalcNode right, MathContext mc) {
			return left.getValue().remainder(right.getValue(), mc);
		}

		@Override
		public CalcNode optimize(CalcNode left, CalcNode right, MathContext mc) {
			// 0 % x => 0
			if(CalcNode.isZERO(left)) return CalcNode.getZero(mc);

        	// throws ArithmeticException
        	//if(right.equals(BigDecimal.ZERO)) return getZero(left.mc);

        	// x % 1 => 0
			if(CalcNode.isONE(right)) return CalcNode.getZero(mc);

			return new CalcNode(this.getIdent(), left, right);
		}
	},

	// --------------------------------------------------------------------
	HAT('^') {
		public BigDecimal calc(CalcNode left, CalcNode right, MathContext mc) {
			return left.getValue().pow(right.getValue().intValueExact(), mc);
		}

		@Override
		public CalcNode optimize(CalcNode left, CalcNode right, MathContext mc) {
			// 0 ^ x => 0
			if(CalcNode.isZERO(left)) return CalcNode.getZero(mc);
			// 1 ^ x => 1
			if(CalcNode.isONE(left)) return new CalcNode(BigDecimal.ONE, mc);

        	// x ^ 0 => 1
			if(CalcNode.isZERO(right)) return new CalcNode(BigDecimal.ONE, mc);
        	// x ^ 1 => x
			if(CalcNode.isONE(right)) return left;

			return new CalcNode(this.getIdent(), left, right);
		}
	},

	;

	private final char ident;
	private Operators(char ident) { this.ident = ident; }
	public char getIdent() { return ident; }
}