package com.chronomus.workflow.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.Variable;
import com.chronomus.workflow.execution.expressions.primitives.ListPrimitive;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;
import com.chronomus.workflow.persistence.ServiceDbAccessor;



public class ParallelMethod extends JobService {

	public static final String OUTPUT = "parallelOutput";
	private static final String INPUT_LIST = "inputList";

	public ParallelMethod(String serviceName, Properties baseContext, ServiceDbAccessor dbAccessor) {
		super(serviceName, baseContext, dbAccessor);
		List<String> inputs = new ArrayList<String>();
		inputs.add(INPUT_LIST);
		setInputs(inputs);	
		List<Expression> outputs = new ArrayList<Expression>();
		outputs.add(new Variable(OUTPUT));
		setOutput(outputs);
	}

	@Override
	protected void run(ServiceContext context) {
		ListPrimitive listPrimitive = (ListPrimitive) context.getVariables().get(INPUT_LIST);
		// Now launch new threads for each of the parallel executions
		for (Primitive expression : listPrimitive.expressions()) {
			ServiceContext newContext = new ServiceContext(context);
			newContext.getVariables().put(OUTPUT, expression);
			Thread serviceThread = new ServiceThread(newContext);
			serviceThread.start();
		}
	}
	
	private class ServiceThread extends Thread {

		private final ServiceContext newContext;

		public ServiceThread(ServiceContext newContext) {
			this.newContext = newContext;
		}

		@Override
		public void run() {
			try {
				ParallelMethod.super.run(newContext);
			} catch (ExecutionException e) {
				// TODO: Signal to parent thread that workflow should be aborted
				e.printStackTrace();
			}
		}
		
	}

}
