package com.chronomus.workflow.execution;

import com.chronomus.workflow.execution.expressions.Expression;

public class Assignment implements Task {

	private final String name;
	private final Expression rvalue;

	public Assignment(String name, Expression expression) {
		this.name = name;
		this.rvalue = expression;
	}

	public String getName() {
		return name;
	}
	
	public Expression getRvalue() {
		return this.rvalue;
	}

	@Override
	public void run(ServiceContext context) throws ExecutionException {
		context.getVariables().put(name, rvalue.evaluate(context.getVariables()));
	}
}
