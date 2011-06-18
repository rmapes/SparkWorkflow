package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;

public class Variable implements Expression {

	private final String name;

	public Variable(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public Primitive evaluate(VariableStore context) {
		return context.get(name);
	}

}
