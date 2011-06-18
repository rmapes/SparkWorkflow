package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;

public class PrimitiveExpression implements Expression {

	private Primitive thePrimitive;

	public PrimitiveExpression(Primitive thePrimitive) {
		super();
		this.thePrimitive = thePrimitive;
	}

	@Override
	public Primitive evaluate(VariableStore context) throws ExecutionException {
		return thePrimitive;
	}

	@Override
	public String toString() {
		return thePrimitive.evaluate();
	}

}
