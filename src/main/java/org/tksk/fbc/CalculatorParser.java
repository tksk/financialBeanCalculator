package org.tksk.fbc;
/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.MemoMismatches;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.examples.java.JavaLetterMatcher;
import org.parboiled.examples.java.JavaLetterOrDigitMatcher;

import org.parboiled.BaseParser;
import org.parboiled.support.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * A calculator parser defining the same language as the CalculatorParser3 but using a rule building helper methods
 * to Factor out common constructs.
 */
@BuildParseTree
public class CalculatorParser extends BaseParser<CalcNode> {

	public static final String WRONG_IDENT_PLACEHOLDER = "?";

	private static final Logger logger = LoggerFactory.getLogger(Bdu.class);

	public final MathContext mc;
	public final Object bean;

	private String setterPropertyName = null;

	public CalculatorParser(MathContext mc, Object bean) {
		this.mc = mc;
		this.bean = bean;
	}

	public Rule InputLine() { return Sequence(Statement(), EOI); }

    public Rule Statement() {
    	return Sequence(
    			Optional(
    				Sequence(/*TestNot(Keyword()),*/ Letter(), ZeroOrMore(LetterOrDigit()), WhiteSpace()),
    				ACTION(assign(match())),
    				toRule("= ")
    			),

    			Expression()
    		);
    }

    public boolean assign(String name) {
    	this.setterPropertyName = name;
    	return true;
    }

    public Rule Expression() { 
    	return OperatorRule(Term(), FirstOf("+ ", "- ")); }

    public Rule Term() { return OperatorRule(Factor(), FirstOf("* ", "/ ")); }

    public Rule Factor() {
        // by using toRule("^ ") instead of Ch('^') we make use of the fromCharLiteral(...) transformation below
        return OperatorRule(Atom(), toRule("^ "));
    }

    public Rule OperatorRule(Rule subRule, Rule operatorRule) {
        Var<Character> op = new Var<Character>();
        return Sequence(
                subRule,
                ZeroOrMore(
                        operatorRule, op.set(matchedChar()),
                        subRule,
                        push(CalcNode.getNode(op.get(), pop(1), pop()))
                )
        );
    }

    public Rule Atom() { return FirstOf(Identifier(), Number(), Parens()); }

    public Rule Parens() { return Sequence("( ", Expression(), ") "); }

    public Rule Number() {
        return Sequence(
                Sequence(
                        Optional(Ch('-')),
                        OneOrMore(Digit()),
                        Optional(Ch('.'), OneOrMore(Digit()))
                ),
                // the action uses a default string in case it is run during error recovery (resynchronization)
                push(new CalcNode(new BigDecimal(matchOrDefault("0")), mc)),
                WhiteSpace()
        );
    }

    public Rule Digit() { return CharRange('0', '9'); }

    public Rule WhiteSpace() { return ZeroOrMore(AnyOf(" \t\r\n\f")); }

    @SuppressSubnodes
    @MemoMismatches
    Rule Identifier() {
        return Sequence(
        		Sequence(/*TestNot(Keyword()),*/ Letter(), ZeroOrMore(LetterOrDigit()), WhiteSpace()),
        		push(new CalcNode(BeanPropertyUtils.getBeanValue(bean, 
        				matchOrDefault(WRONG_IDENT_PLACEHOLDER), mc), mc))
        );
    }

/*
    // JLS defines letters and digits as Unicode characters recognized
    // as such by special Java procedures.
    Rule Keyword() {
        return Sequence(
                FirstOf("assert", "break", "case", "catch", "class", "const", "continue", "default", "do", "else",
                        "enum", "extends", "finally", "final", "for", "goto", "if", "implements", "import", "interface",
                        "instanceof", "new", "package", "return", "static", "super", "switch", "synchronized", "this",
                        "throws", "throw", "try", "void", "while"),
                TestNot(LetterOrDigit())
        );
    }
*/
    Rule Letter() {
        // switch to this "reduced" character space version for a ~10% parser performance speedup
        //return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_', '$');
        return FirstOf(Sequence('\\', UnicodeEscape()), new JavaLetterMatcher());
    }

    @MemoMismatches
    Rule LetterOrDigit() {
        // switch to this "reduced" character space version for a ~10% parser performance speedup
        //return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_', '$');
        return FirstOf(Sequence('\\', UnicodeEscape()), new JavaLetterOrDigitMatcher());
    }

    Rule UnicodeEscape() {
        return Sequence(OneOrMore('u'), HexDigit(), HexDigit(), HexDigit(), HexDigit());
    }

    Rule HexDigit() {
        return FirstOf(CharRange('a', 'f'), CharRange('A', 'F'), CharRange('0', '9'));
    }

    // we redefine the rule creation for string literals to automatically match trailing whitespace if the string
    // literal ends with a space character, this way we don't have to insert extra whitespace() rules after each
    // character or string literal
    @Override
    protected Rule fromStringLiteral(String string) {
        return string.endsWith(" ") ?
                Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace()) :
                String(string);
    }

	public String getSetterPropertyName() {
		return setterPropertyName;
	}
}
