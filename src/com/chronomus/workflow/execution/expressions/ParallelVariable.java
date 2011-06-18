package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;

public class ParallelVariable implements Expression {

	private final Expression expression;

	public ParallelVariable(Expression expression) {
		super();
		this.expression = expression;
	}

	@Override
	public Primitive evaluate(VariableStore context) {
		// TODO Code Smell: Parallel Variable is being used as a marker interface
		throw new UnsupportedOperationException();
	}

	public Expression getExpression() {
		return expression;
	}

}
