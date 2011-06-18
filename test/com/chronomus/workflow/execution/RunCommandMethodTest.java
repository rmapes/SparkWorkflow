package com.chronomus.workflow.execution;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.chronomus.workflow.execution.RunCommandMethod;
import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;
import com.chronomus.workflow.execution.expressions.primitives.StringPrimitive;

public class RunCommandMethodTest extends TestCase {
	
	public void testCaptureOutput() throws Exception {
		RunCommandMethod method = new RunCommandMethod();
		List<Primitive> inputs = new ArrayList<Primitive>();
		inputs.add(new StringPrimitive(CommandLines.outputToScreen() + " hello"));
		Primitive output = method.run(inputs, new VariableStore());
		assertEquals("hello", output.toString());
	}

}
