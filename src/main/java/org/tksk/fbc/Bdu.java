package org.tksk.fbc;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.trees.GraphUtils.printTree;

import java.math.BigDecimal;
import java.math.MathContext;

import org.parboiled.Parboiled;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.trees.GraphNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bdu {
	private static Logger logger = LoggerFactory.getLogger(Bdu.class);

	public static BigDecimal compute(String expression) throws ArithmeticException {
		return compute(expression, MathContext.UNLIMITED, null);
	}

	public static BigDecimal compute(String expression, MathContext mc) throws ArithmeticException {
		return compute(expression, mc, null);
	}

	public static BigDecimal compute(String expression, Object bean) throws ArithmeticException {
		return compute(expression, MathContext.UNLIMITED, bean);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static BigDecimal compute(String expression, MathContext mc, Object bean) throws ArithmeticException {

		if(expression == null || expression.trim().equals("")) {
			throw new NullPointerException("expression must no be null nor empty");
		}

		try {
			// return a BigDecimal itself if expression is simple just a number as string
			return new BigDecimal(expression.trim(), mc);
		} catch (NumberFormatException e) {
			// go ahead
		}

		CalculatorParser parser = Parboiled.createParser(CalculatorParser.class, mc, bean);

		try {
			ParsingResult<CalcNode> result = new RecoveringParseRunner<CalcNode>(parser.InputLine()).run(expression.trim());

	        if (result.hasErrors()) {
	        	throw new ArithmeticException("\nParse Errors:\n" + printParseErrors(result));
	        }

	        Object value = result.parseTreeRoot.getValue();
	        if (value instanceof GraphNode) {
	        	logger.info("Abstract Syntax Tree:\n" +
	        			printTree((GraphNode) value, new ToStringFormatter(null)));
	        } else {
	        	logger.info("Parse Tree:\n" + printNodeTree(result));
	        }

	        CalcNode node = result.parseTreeRoot.getValue();
	        if(parser.getSetterPropertyName() != null) {
	        	// statement
	        	BigDecimal ret = node.getValue();
	        	BeanPropertyUtils.setBeanValue(bean, parser.getSetterPropertyName(), ret);
	        	return ret;
	        } else {
	        	return node.getValue();
	        }
		} catch (ParserRuntimeException e) {
			throw new IllegalArgumentException(e.getCause().getMessage(), e);
		}
	}
}
