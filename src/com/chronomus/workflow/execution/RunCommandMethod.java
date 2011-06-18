package com.chronomus.workflow.execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.Variable;
import com.chronomus.workflow.execution.expressions.primitives.StringPrimitive;


public class RunCommandMethod extends Service {

	private final static String COMMAND = "command";
	private final static String OUTPUT = "commandOutput";

	public RunCommandMethod() {
		super("runCommand", null);
		List<String> inputs = new ArrayList<String>();
		inputs.add(COMMAND);
		setInputs(inputs);
		List<Expression> outputs = new ArrayList<Expression>();
		outputs.add(new Variable(OUTPUT));
		setOutput(outputs);
	}
	
	@Override
	public void run(ServiceContext context) throws ExecutionException {
		String parsedCommand = context.getVariables().getProperty(COMMAND);
		try {
			Process p = Runtime.getRuntime().exec(parsedCommand);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//p.getOutputStream().flush();
			// Capture output
			StringBuilder output = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
			}
			// Write output to context
			context.getVariables().put(OUTPUT, new StringPrimitive(output.toString()));
			// Wait for process to complete
			p.waitFor();
			// TODO: check exit value and log warning if non zero
			if (p.exitValue() != 0 ) {
				// Should log alert here. Need to implement alerting mechanism
				//System.out.println("Warning: failed command - " + parsedCommand);
				throw new WorkflowFailureException(parsedCommand, output);
			}
		} catch (IOException e) {
			// TODO Log warning
			e.printStackTrace();
			} catch (InterruptedException e) {
			// TODO Do something about this
			e.printStackTrace();
		}
	}

}
