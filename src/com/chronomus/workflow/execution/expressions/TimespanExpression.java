package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;
import com.chronomus.workflow.execution.expressions.primitives.TimespanPrimitive;

public class TimespanExpression implements Expression {

	private TimespanPrimitive primitive;

	public TimespanExpression(TimespanPrimitive primitive) {
		this.primitive = primitive;
	}

	@Override
	public Primitive evaluate(VariableStore context) throws ExecutionException {
		return primitive;
	}

}
