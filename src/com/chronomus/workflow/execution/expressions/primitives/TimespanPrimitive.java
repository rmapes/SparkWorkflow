package com.chronomus.workflow.execution.expressions.primitives;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.expressions.Operators;
import com.chronomus.workflow.parsers.ParseException;

public class TimespanPrimitive implements Primitive {

	public enum Type {
		millisecond, second, minute, hour, day, week, month, year;
	}

	private Type type;

	private NumberPrimitive multiplier;

	public TimespanPrimitive(NumberPrimitive multiplier, Type type) {
		this.multiplier = multiplier;
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public NumberPrimitive getMultiplier() {
		return multiplier;
	}

	@Override
	public String evaluate() {
		return multiplier.evaluate() + " " + type.name();
	}

	public TimespanPrimitive multiplyBy(NumberPrimitive multiplier2) throws ParseException {
		NumberPrimitive newMultiplier;
		try {
			newMultiplier = NumberPrimitive.combine(multiplier, multiplier2, Operators.Binary.multiply);
		} catch (ExecutionException e) {
			throw new ParseException(e.getMessage());
		}
		return new TimespanPrimitive(newMultiplier, type);
	}

	@Override
	public String toString() {
		return evaluate();
	}
	
	

}
