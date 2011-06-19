package com.chronomus.workflow.execution.expressions.primitives;

import java.text.NumberFormat;
import java.text.ParseException;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.expressions.Operators.Binary;


public class NumberPrimitive implements Primitive {

	private static final NumberFormat numberFormat = NumberFormat.getInstance();

	private final String value;

	public NumberPrimitive(String value) {
		this.value = value;
	}

	@Override
	public String evaluate() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static NumberPrimitive combine(Primitive left, Primitive right, Binary op) throws ExecutionException {
		try {
			return combine(numberFormat.parse(left.evaluate()), numberFormat.parse(right.evaluate()), op);
		} catch (ParseException e) {
			throw new ExecutionException(e);
		}
	}

	static private NumberPrimitive combine(Number leftVal, Number rightVal, Binary op2) {
		double value = 0;;
		switch (op2) {
		case plus:
			value = leftVal.doubleValue() + rightVal.doubleValue();
			break;
		case minus:
			value = leftVal.doubleValue() - rightVal.doubleValue();
			break;
		case multiply:
			value = leftVal.doubleValue() * rightVal.doubleValue();
			break;
		case divide:
			value = leftVal.doubleValue() / rightVal.doubleValue();
			break;
		}
		return new NumberPrimitive(numberFormat.format(value));
	}

	public long longValue() {
		return toValue().longValue();
	}
	
	public Number toValue()  {
		try {
			return numberFormat.parse(value);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

}
