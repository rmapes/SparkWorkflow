package com.chronomus.workflow.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.Variable;
import com.chronomus.workflow.execution.expressions.primitives.ListPrimitive;

public class SplitMethod extends Service {

	private final static String EXPRESSION = "expression";

	public SplitMethod() {
		super("split", new Properties());
		List<String> inputs = new ArrayList<String>();
		inputs.add(EXPRESSION);
		setInputs(inputs);	
		List<Expression> outputs = new ArrayList<Expression>();
		outputs.add(new Variable("output"));
		setOutput(outputs);
	}

	@Override
	protected void run(ServiceContext context) {
		String evaluatedExpression = context.getVariables().getProperty(EXPRESSION);
		// Need to split into list by \n
		String[] splitValues = evaluatedExpression.split("\n");
		context.getVariables().put("output", new ListPrimitive(splitValues));
	}

}
