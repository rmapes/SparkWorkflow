package com.chronomus.workflow.execution;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;

public abstract class Service {

	protected final String name;
	protected ServiceContext baseContext;
	private List<Expression> output;
	private List<String> inputs;

	public Service(String name, Properties configuration) {
		this.name = name;
		// Make all of base context available to service
		this.baseContext = new ServiceContext(configuration);
	}

	// Used for anonymous services only
	public Service() {
		this(null, new Properties());
	}

	public void setInputs(List<String> inputs) {
		this.inputs = inputs;		
	}

	public void setOutput(List<Expression> outputList) {
		this.output = outputList;
	}

	public Primitive run() throws ExecutionException {
		return run(Collections.<Primitive>emptyList(), new VariableStore());
	}

	public Primitive run(List<Primitive> inputParameters, VariableStore inputContext) throws ExecutionException {
		// Bit of debug here
		//System.out.println(String.format("Running: %s", this.name));
		// Check that number of input values match number of parameters
		ServiceContext context = new ServiceContext(baseContext);
		context.getVariables().putAll(inputContext);
		if (inputs != null) {
			if (inputParameters.size() != inputs.size()) {
				throw new RuntimeException("Mismatch between method input parameters and method signature");
			}
			for (int i = 0; i < inputParameters.size(); i++) {
				context.getVariables().put(inputs.get(i), inputParameters.get(i));
			}
		}
		run(context);
		return output != null ? output.iterator().next().evaluate(context.getVariables()) : Primitive.NULL;
	}

	protected abstract void run(ServiceContext context) throws ExecutionException;

	// For tests only
	public String getName() {
		return name;
	}
}
