package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;

/**
 * Expression to prevent repartitioning of binary expressions,
 * such that the contents of this expression will always be 
 * evaluated in precedence to being combined with
 * other operators
 * i.e. 3 + (2 * 5) + 7 = 3 + (10) + 7,   not ((3 + 2) * 5) + 7
 * @author rmapes
 *
 */
public class BracketedExpression implements Expression {

	private final Expression expr;

	public BracketedExpression(Expression expr) {
		this.expr = expr;
	}

	@Override
	public Primitive evaluate(VariableStore context) throws ExecutionException {
		return expr.evaluate(context);
	}

	@Override
	public String toString() {
		return "(" + expr + ")";
	}

}
