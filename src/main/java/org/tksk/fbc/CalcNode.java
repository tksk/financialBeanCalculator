package org.tksk.fbc;
import java.math.BigDecimal;
import java.math.MathContext;

import org.parboiled.trees.ImmutableBinaryTreeNode;

public class CalcNode extends ImmutableBinaryTreeNode<CalcNode> {
    BigDecimal value;
    private Operator operator;
    private MathContext mc;

    public CalcNode(BigDecimal value, MathContext mc) {
        super(null, null);
        this.value = value;
        this.mc = mc;
    }

    protected CalcNode(char operator, CalcNode left, CalcNode right) {
        super(left, right);
        this.operator = getOperator(operator);
        this.mc = left.mc;
    }

    public static CalcNode getNode(Character operator, CalcNode left, CalcNode right) {
        if (operator == null) throw new NullPointerException();

        //return new CalcNode(operator, left, right);
        MathContext mc = left == null? null : left.mc;
        return getOperator(operator).optimize(left, right, mc);
    }

    public BigDecimal getValue() {
        if (operator == null) return value;

        return operator.calc(left(), right(), mc);
    }

    @Override
    public String toString() {
        return (operator == null ? "Value " + value : "Operator '" + operator + '\'') + " | " + getValue();
    }

    public static CalcNode getZero(MathContext mc) {
    	return new CalcNode(BigDecimal.ZERO, mc);
    }

    public static Operator getOperator(char c) {
    	for(Operator op : Operators.values()) {
    		if(op.getIdent() == c) return op;
    	}

    	throw new IllegalArgumentException("unsupported operator: " + c);
    }

    static interface Operator {
    	BigDecimal calc(CalcNode left, CalcNode right, MathContext mc);
    	CalcNode optimize(CalcNode left, CalcNode right, MathContext mc);
    	char getIdent();
    }

    static boolean isZERO(CalcNode node) {
    	return node != null && node.value != null && node.value.equals(BigDecimal.ZERO);
    }

    static boolean isONE(CalcNode node) {
    	return node != null && node.value != null && node.value.equals(BigDecimal.ONE);
    }
}