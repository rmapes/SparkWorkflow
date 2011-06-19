package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.Operators.Binary;
import com.chronomus.workflow.execution.expressions.primitives.DatePrimitive;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;
import com.chronomus.workflow.execution.expressions.primitives.TimespanPrimitive;

public class DateBinaryExpression implements Expression {

	private final Expression left;
	private final Expression right;
	private final Binary op;

	public DateBinaryExpression(Expression left, Expression right,
			Binary op) {
				this.left = left;
				this.right = right;
				this.op = op;
	}

	@Override
	public Primitive evaluate(VariableStore context) throws ExecutionException {
		Primitive leftPrim = left.evaluate(context);
		if (!(leftPrim instanceof DatePrimitive)) {
			throw new ExecutionException("Left hand of date expression should be a date");
		}
		Primitive rightPrim = right.evaluate(context);
		if (!(rightPrim instanceof TimespanPrimitive)) {
			throw new ExecutionException("Right hand of date expression should be a timespan");
		}
		return ((DatePrimitive) leftPrim).apply((TimespanPrimitive) rightPrim, op);
	}


}
