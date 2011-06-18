package com.chronomus.workflow.execution.expressions.primitives;


public class NumberPrimitive implements Primitive {

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
}
