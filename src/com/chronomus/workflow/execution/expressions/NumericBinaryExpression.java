package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.Operators.Binary;
import com.chronomus.workflow.execution.expressions.primitives.NumberPrimitive;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;

public class NumericBinaryExpression implements Expression {

	private Expression left;
	private Expression right;
	private Binary op;

	public NumericBinaryExpression(
			Expression left,
			Expression right,
			Binary op) {
		// Precedence set by parser is right to left evaluation.
		// Ths is non-intutitive so let's reverse it
		if (right instanceof NumericBinaryExpression) {
			repartition(left, (NumericBinaryExpression) right, op);
		} else {
				this.left = left;
				this.right = right;
				this.op = op;
		}
	}

	private void repartition(Expression left, NumericBinaryExpression right, Binary op) {
		this.left = new NumericBinaryExpression(left, right.left, op);
		this.op = right.op;
		this.right = right.right;
	}

	@Override
	public Primitive evaluate(VariableStore context) throws ExecutionException {
		return NumberPrimitive.combine(left.evaluate(context), right.evaluate(context), op);
	}

	@Override
	public String toString() {
		return left + toString(op) + right;
	}

	static private String toString(Binary op2) {
		switch (op2) {
		case plus:
			return("+");
		case minus:
			return("-");
		case multiply:
			return("*");
		case divide:
			return("/");
		}
		return "?";
	}

}
