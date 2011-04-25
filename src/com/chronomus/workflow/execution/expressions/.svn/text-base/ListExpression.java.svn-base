package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.VariableStore;

public class ListExpression implements Expression {

	private final Expression contents;

	public ListExpression(Expression contents) {
		this.contents = contents;
	}

	@Override
	public Primitive evaluate(VariableStore context) throws ExecutionException {
		return contents.evaluate(context);
	}

}
