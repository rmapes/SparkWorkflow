package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.VariableStore;

public class RangeExpression implements Expression {

	private final long left;
	private final long right;

	public RangeExpression(long left, long right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Primitive evaluate(VariableStore context) throws ExecutionException {
		// TODO: support non string values
		String[] values = new String[(int) (right - left + 1)];
		for (long i = left; i<= right; i++) {
			values[(int) (i-left)] = Long.toString(i);
		}
		return new ListPrimitive(values);
	}

}
