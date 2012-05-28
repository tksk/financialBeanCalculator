package org.tksk.fbc;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

public class GetYield {

	public BigDecimal computeYield(BigDecimal startBalance,
			BigDecimal interest, int days, int years) {

		int precision = 7;
		RoundingMode roundMode = RoundingMode.HALF_UP;

		BigDecimal currentBalance = startBalance.setScale(precision, roundMode);
		BigDecimal interestDaily = interest.setScale(precision, roundMode);
		interestDaily = interestDaily.multiply(BigDecimal.valueOf(years))
				.divide(BigDecimal.valueOf(days), roundMode);

		for (int calcDay = 1; calcDay <= days; ++calcDay) {
			currentBalance = currentBalance.add(currentBalance
					.multiply(interestDaily));
		}

		return currentBalance.setScale(2, RoundingMode.FLOOR);
	}

	public BigDecimal computeYield2(BigDecimal startBalance,
			BigDecimal interest, int days, int years) {

		MathContext mc = new MathContext(7, RoundingMode.HALF_UP);

		CalcContext ctx = CalcContext.create()
				.putAnd("balance", startBalance.setScale(7, RoundingMode.HALF_UP))
				.putAnd("interest", interest.setScale(7, RoundingMode.HALF_UP))
				.putAnd("years", years)
				.putAnd("days", days);

		//interestDaily = interestDaily.multiply(BigDecimal.valueOf(years)).divide(BigDecimal.valueOf(days), roundMode);

		Bdu.compute("interest = interest * years / days", mc, ctx);

		for (int calcDay = 1; calcDay <= days; ++calcDay) {
			Bdu.compute("balance = balance + balance * interest", mc, ctx);
		}

		return ctx.get("balance").setScale(2, RoundingMode.FLOOR);
	}

	@Test
	public void test1() {
		BigDecimal yield = computeYield(BigDecimal.valueOf(1000095.20),
				BigDecimal.valueOf(0.0625), 9132, 25);

		assertEquals(yield, new BigDecimal("4770479.09"));
	}

	@Test
	public void test2() {

		BigDecimal yield = computeYield2(BigDecimal.valueOf(1000095.20),
				BigDecimal.valueOf(0.0625), 9132, 25);

		assertEquals(yield, new BigDecimal("4770479.09"));
	}
}