package org.tksk.fbc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.math.BigDecimal;
import java.math.MathContext;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.testng.annotations.Test;

@Test
public class BduTest {

	@Test
	public void parseString() {
		assertEquals(Bdu.compute("0"), BigDecimal.ZERO);

		assertEquals(Bdu.compute("0 + 1"), BigDecimal.ONE);

		assertEquals(Bdu.compute("0+1"), BigDecimal.ONE);

		// float can not be in financial calculation
		assertNotEquals(0.1 * 3, 0.3);
		assertEquals(
				Bdu.compute("0.1 * 3"), 
				new BigDecimal("0.1").multiply(BigDecimal.valueOf(3)));

		assertEquals(Bdu.compute("0 / 1 + 1"), BigDecimal.ONE);

		assertEquals(Bdu.compute("0 / 1 + 1 * (1.1 - (0.1)) / 0.1"), BigDecimal.TEN);
	}

	@Test(expectedExceptions={ArithmeticException.class})
	public void nonTerminatingDec() {
		Bdu.compute("1/3"); // defaulting to MathContext.UNLIMITED
	}

	@Test
	public void parseStringMathContext() {
		MathContext mc = MathContext.DECIMAL32;

		assertEquals(
				Bdu.compute("1/3", mc),
				BigDecimal.ONE.divide(BigDecimal.valueOf(3), mc));
	}

	@Test
	public void parseStringMathContextObject() {
		ParameterBean param = new ParameterBean();

		param.setLeft(4);
		param.setRight(6);

		assertEquals(Bdu.compute("left + right", param), BigDecimal.TEN);


		param.setLeft(1);
		param.setRight(42);

		assertEquals(
				Bdu.compute("0.1 * 3 + left * right", param),

				new BigDecimal("0.1").multiply(BigDecimal.valueOf(3))
					.add(
						new BigDecimal(param.getLeft()).multiply(new BigDecimal(param.getRight()))
					)
			);

		assertEquals(
				Bdu.compute("1/3", MathContext.DECIMAL32, param /* won't be used... */),
				BigDecimal.ONE.divide(BigDecimal.valueOf(3), MathContext.DECIMAL32));


		CalcContext ctx = CalcContext.create()
				.putAnd("left", 1)
				.putAnd("right", 42);
		assertEquals(Bdu.compute("0.1 * 3 + left * right", ctx), new BigDecimal("42.3"));
	}


	@Test(expectedExceptions={IllegalArgumentException.class}, 
			expectedExceptionsMessageRegExp=".*Java's keyword.*")
	public void keyForJavaKeyword() {
		CalcContext ctx = CalcContext.create()
				.putAnd("left", 1)
				.putAnd("class", 42);

		assertEquals(Bdu.compute("0.1 * 3 + left * class + 1", ctx), new BigDecimal("42.3"));
	}


	@Test
	public void optimize() {
		assertEquals(
				Bdu.compute("(0+1) + (1+0)"  // 1 + 1
						+ "+ (1-0) + (0-1)"  // 1 + (-1)
						+ "+ (3*1) + (1*3) + (7483*0) + (0*33445)" // 3 + 3 + 0 + 0
						+ "+ (5/1) + (0/123133)"  // 5 + 0
						+ "+ (7^0) + (7^1) + (0^889894) + (1^7)" // 1 + 7 + 0 + 1
						),
				BigDecimal.valueOf(22)
			);


		ParameterBean param = new ParameterBean();

		param.setLeft(4);
		param.setRight(6);


		assertEquals(Bdu.compute("0 * (left + right + 31231231)", param), BigDecimal.ZERO);

		param.setLeft(0);
		param.setRight(6);

		assertEquals(Bdu.compute("left * (4 * right + 490284)", param), BigDecimal.ZERO);
	}

	@Test
	public void accumulation() {
		CalcContext ctx = CalcContext.create()
				.putAnd("buf", BigDecimal.ZERO);

		for(int i=0; i<=10; i++) {
			ctx.putAnd("i", i);
			Bdu.compute("buf = buf + i", ctx);
		}

		assertEquals(ctx.get("buf"), BigDecimal.valueOf(55));
	}

	@Test
	public void unicodeVars() {
		CalcContext ctx = CalcContext.create()
				.putAnd("合計", BigDecimal.ZERO);

		for(int i=0; i<=10; i++) {
			ctx.putAnd("i", i);
			Bdu.compute("合計 = 合計 + i", ctx);
		}

		assertEquals(ctx.get("合計"), BigDecimal.valueOf(55));
	}

	@Test
	public void areaOfTriangle() {
		Triangle t = new Triangle();
		t.setBase(5);
		t.setHeight(3);

		assertEquals(t.area(), new BigDecimal("7.5"));
	}
}

@Data
class ParameterBean {
	private int left;
	private int right;
}


@Data
@EqualsAndHashCode(callSuper=false)
class Triangle extends BigDecimalSupport {
	private int base;
	private int height;

	public BigDecimal area() {
		BigDecimal.valueOf(base).multiply(BigDecimal.valueOf(height)).multiply(BigDecimal.valueOf(2));

		return compute("base * height / 2");
	}
}
