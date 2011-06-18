package com.chronomus.workflow.parsers;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.chronomus.workflow.definition.Job;
import com.chronomus.workflow.definition.Workflow;
import com.chronomus.workflow.execution.Assignment;
import com.chronomus.workflow.execution.MethodCall;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.ListExpression;
import com.chronomus.workflow.execution.expressions.ParallelVariable;
import com.chronomus.workflow.execution.expressions.StringExpression;
import com.chronomus.workflow.execution.expressions.Variable;
import com.chronomus.workflow.execution.expressions.primitives.ListPrimitive;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;
import com.chronomus.workflow.parsers.SparkWorkflowParser;
import com.chronomus.workflow.persistence.MockServiceDbAccessor;
import com.chronomus.workflow.persistence.ServiceDbAccessor;

public class SparkWorkflowParserTest extends TestCase {
	
	private ServiceDbAccessor dbAccessor = new MockServiceDbAccessor();

	public void testBasicWorkflow() throws Exception {
		String basicWorkflow = "workflow { def job helloWorld() { runCommand(\"echo Hello World\"); } }";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		assertEquals(1, workflow.getJobs().size());
		Job helloJob = workflow.getJobs().get("helloWorld");
		assertEquals("helloWorld", helloJob.getName());
		assertEquals(1, helloJob.getTasks().size());
		Task task = helloJob.getTasks().iterator().next();
		assertTrue(task instanceof MethodCall);
		assertEquals("echo Hello World", ((MethodCall) task).getParameters().iterator().next().toString());
	}

	public void testAssignmentsInWorkflow() throws Exception {
		String basicWorkflow = "workflow { def job helloWorld() { testVariable = \"hello\"; } }";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		assertEquals(1, workflow.getJobs().size());
		Job helloJob = workflow.getJobs().get("helloWorld");
		assertEquals("helloWorld", helloJob.getName());
		assertEquals(1, helloJob.getTasks().size());
		Task task = helloJob.getTasks().iterator().next();
		assertTrue(task instanceof Assignment);
		assertEquals("hello", ((Assignment) task).getRvalue().toString());
	}
	
	public void testCommandWithVariableInParameters() throws Exception {
		String basicWorkflow = "workflow { def job helloWorld() { testVariable = \"outputToFile.bat hello\"; " +
				"runCommand(testVariable);} }";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		assertEquals(1, workflow.getJobs().size());
		Job helloJob = workflow.getJobs().get("helloWorld");
		assertEquals("helloWorld", helloJob.getName());
		assertEquals(2, helloJob.getTasks().size());
		Iterator<Task> iterator = helloJob.getTasks().iterator();
		Task task = iterator.next();
		assertTrue(task instanceof Assignment);
		assertEquals("testVariable", ((Assignment) task).getName());
		assertEquals("outputToFile.bat hello", ((Assignment) task).getRvalue().toString());
		Task task2 = iterator.next();
		assertTrue(task2 instanceof MethodCall);
		Expression expression = ((MethodCall) task2).getParameters().iterator().next();
		assertTrue(expression instanceof Variable);
		Variable var = (Variable) expression;
		assertEquals("testVariable", var.getName());
	}

	public void testCommandWithVariableEmbeddedInParameters() throws Exception {
		String basicWorkflow = "workflow { def job helloWorld() { testVariable = \"hello\"; " +
				"runCommand(\"outputToFile ${testVariable}\");} }";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		assertEquals(1, workflow.getJobs().size());
		Job helloJob = workflow.getJobs().get("helloWorld");
		assertEquals("helloWorld", helloJob.getName());
		assertEquals(2, helloJob.getTasks().size());
		Iterator<Task> iterator = helloJob.getTasks().iterator();
		Task task = iterator.next();
		assertTrue(task instanceof Assignment);
		assertEquals("hello", ((Assignment) task).getRvalue().toString());
		Task task2 = iterator.next();
		assertTrue(task2 instanceof MethodCall);
		assertEquals("outputToFile ${testVariable}", ((MethodCall) task2).getParameters().iterator().next().toString());
	}
	
	public void testSequencedJobsSingleThreaded() throws Exception {
		String basicWorkflow = "workflow {\n" +
				"def job parent() {\n" +
				"	child_job1();\n" +
				"	child_job2();\n" +
				"}\n" +
				"def job child_job1() {\n" +
				"	var1 = \"true\";\n" +
				"} \n" +
				"def job child_job2() {\n" +
				"	var2 = \"apple\";\n" +
				"}\n" +
			"}";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		
		assertEquals(3, workflow.getJobs().size());
		Map<String, Job> jobs = workflow.getJobs();
		Job parentJob = jobs.get("parent");
		Job childJob1 = jobs.get("child_job1");
		Job childJob2 = jobs.get("child_job2");
		
		assertEquals("parent", parentJob.getName());
		assertEquals(2, parentJob.getTasks().size());
		Iterator<Task> taskIterator = parentJob.getTasks().iterator();
		Task task1 = taskIterator.next();
		assertTrue(task1 instanceof MethodCall);
		assertEquals("child_job1", ((MethodCall) task1).getName());
		Task task2 = taskIterator.next();
		assertTrue(task2 instanceof MethodCall);
		assertEquals("child_job2", ((MethodCall) task2).getName());
		
		assertEquals("child_job1", childJob1.getName());
		assertEquals(1, childJob1.getTasks().size());
		taskIterator = childJob1.getTasks().iterator();
		task1 = taskIterator.next();
		assertTrue(task1 instanceof Assignment);
		Assignment assignment = (Assignment) task1;
		assertEquals("var1", assignment.getName());
		assertEquals("true", assignment.getRvalue().evaluate(null).toString());

		assertEquals("child_job2", childJob2.getName());
		assertEquals(1, childJob2.getTasks().size());
		taskIterator = childJob2.getTasks().iterator();
		task1 = taskIterator.next();
		assertTrue(task1 instanceof Assignment);
		assignment = (Assignment) task1;
		assertEquals("var2", assignment.getName());
		assertEquals("apple", assignment.getRvalue().evaluate(null).toString());
	}
	
	public void testSequencedJobsSingleThreadedParameterPassing() throws Exception {
		String basicWorkflow = "workflow {\n" +
				"def job parent() {\n" +
				"   top1 = \"test1\";\n"  +
				"	top2 = child_job1(top1);\n" +
				"	child_job2(top1, top2);\n" +
				"}\n" +
				"def job child_job1(input1) {\n" +
				"	var1 = \"Expected: ${input1} = true\";\n" +
				"	return var1;\n" +
				"} \n" +
				"def job child_job2(input1, input2) {\n" +
				"	var2 = \"${input1} result - ${input2}\";\n" +
				"}\n" +
			"}";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		
		assertEquals(3, workflow.getJobs().size());
		Map<String, Job> jobs = workflow.getJobs();
		Job parentJob = jobs.get("parent");
		Job childJob1 = jobs.get("child_job1");
		Job childJob2 = jobs.get("child_job2");
		
		assertEquals("parent", parentJob.getName());
		assertEquals(3, parentJob.getTasks().size());
		Iterator<Task> taskIterator = parentJob.getTasks().iterator();
		Task task1 = taskIterator.next();
		assertTrue(task1 instanceof Assignment);
		assertEquals("top1", ((Assignment) task1).getName());
		assertEquals("test1", ((Assignment) task1).getRvalue().toString());
		assertTrue(((Assignment) task1).getRvalue() instanceof StringExpression);
		Task task2 = taskIterator.next();
		assertTrue(task2 instanceof Assignment);
		assertEquals("top2", ((Assignment) task2).getName());
		assertTrue(((Assignment) task2).getRvalue() instanceof MethodCall);
		List<Expression> parameters = ((MethodCall)((Assignment) task2).getRvalue()).getParameters();
		assertEquals(1, parameters.size());
		Iterator<Expression> iterator = parameters.iterator();
		assertEquals("top1", ((Variable) iterator.next()).getName());
		Task task3 = taskIterator.next();
		assertTrue(task3 instanceof MethodCall);
		assertEquals("child_job2", ((MethodCall) task3).getName());
		parameters = ((MethodCall)task3).getParameters();
		assertEquals(2, parameters.size());
		iterator = parameters.iterator();
		assertEquals("top1", ((Variable) iterator.next()).getName());
		assertEquals("top2", ((Variable) iterator.next()).getName());
		
		assertEquals("child_job1", childJob1.getName());
		assertEquals(1, childJob1.getTasks().size());
		taskIterator = childJob1.getTasks().iterator();
		task1 = taskIterator.next();
		assertTrue(task1 instanceof Assignment);
		Assignment assignment = (Assignment) task1;
		assertEquals("var1", assignment.getName());
		assertEquals("Expected: ${input1} = true", assignment.getRvalue().toString());
		List<String> inputs = childJob1.getInputs();
		assertEquals(1, inputs.size());
		Iterator<String> iterator2 = inputs.iterator();
		assertEquals("input1", iterator2.next());
		List<Expression> outputs = childJob1.getOutputs();
		assertEquals("var1", ((Variable)outputs.iterator().next()).getName());
		
		assertEquals("child_job2", childJob2.getName());
		assertEquals(1, childJob2.getTasks().size());
		taskIterator = childJob2.getTasks().iterator();
		task1 = taskIterator.next();
		assertTrue(task1 instanceof Assignment);
		assignment = (Assignment) task1;
		assertEquals("var2", assignment.getName());
		assertEquals("${input1} result - ${input2}", assignment.getRvalue().toString());
		inputs = childJob2.getInputs();
		assertEquals(2, inputs.size());
		iterator2 = inputs.iterator();
		assertEquals("input1", iterator2.next());
		assertEquals("input2", iterator2.next());
	}

	public void testSequencedJobsParallelParameterPassing() throws Exception {
		String basicWorkflow = "workflow {\n" +
				"def job parent() {\n" +
				"   top1 = \"test1\\ntest2\\ntest3\";\n"  +
				"	top2 = child_job1(*split(top1));\n" +
				"	child_job2(top1, top2);\n" +
				"}\n" +
				"def job child_job1(input1) {\n" +
				"	var1 = \"Expected: ${input1} = true\";\n" +
				"	return var1;\n" +
				"} \n" +
				"def job child_job2(input1, input2) {\n" +
				"	var2 = \"${input1} result - ${input2}\";\n" +
				"}\n" +
			"}";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		
		assertEquals(3, workflow.getJobs().size());
		Map<String, Job> jobs = workflow.getJobs();
		Job parentJob = jobs.get("parent");
		Job childJob1 = jobs.get("child_job1");
		Job childJob2 = jobs.get("child_job2");
		
		assertEquals("parent", parentJob.getName());
		assertEquals(3, parentJob.getTasks().size());
		Iterator<Task> taskIterator = parentJob.getTasks().iterator();
		Task task1 = taskIterator.next();
		assertTrue(task1 instanceof Assignment);
		assertEquals("top1", ((Assignment) task1).getName());
		assertEquals("test1\ntest2\ntest3", ((Assignment) task1).getRvalue().toString());
		assertTrue(((Assignment) task1).getRvalue() instanceof StringExpression);
		Task task2 = taskIterator.next();
		assertTrue(task2 instanceof Assignment);
		assertEquals("top2", ((Assignment) task2).getName());
		assertTrue(((Assignment) task2).getRvalue() instanceof MethodCall);
		List<Expression> parameters = ((MethodCall)((Assignment) task2).getRvalue()).getParameters();
		assertEquals(1, parameters.size());
		Iterator<Expression> iterator = parameters.iterator();
		Expression top1 = iterator.next();
		assertTrue(top1 instanceof ParallelVariable);
		Expression top1Expression = ((ParallelVariable) top1).getExpression();
		assertTrue(top1Expression instanceof MethodCall);
		MethodCall splitCall = (MethodCall) top1Expression;
		assertEquals("split", splitCall.getName());
		Expression splitParams = splitCall.getParameters().iterator().next();
		assertTrue(splitParams instanceof Variable);
		assertEquals("top1", ((Variable)splitParams).getName());
		Task task3 = taskIterator.next();
		assertTrue(task3 instanceof MethodCall);
		assertEquals("child_job2", ((MethodCall) task3).getName());
		parameters = ((MethodCall)task3).getParameters();
		assertEquals(2, parameters.size());
		iterator = parameters.iterator();
		assertEquals("top1", ((Variable) iterator.next()).getName());
		assertEquals("top2", ((Variable) iterator.next()).getName());
		
		assertEquals("child_job1", childJob1.getName());
		assertEquals(1, childJob1.getTasks().size());
		taskIterator = childJob1.getTasks().iterator();
		task1 = taskIterator.next();
		assertTrue(task1 instanceof Assignment);
		Assignment assignment = (Assignment) task1;
		assertEquals("var1", assignment.getName());
		assertEquals("Expected: ${input1} = true", assignment.getRvalue().toString());
		List<String> inputs = childJob1.getInputs();
		assertEquals(1, inputs.size());
		Iterator<String> iterator2 = inputs.iterator();
		assertEquals("input1", iterator2.next());
		List<Expression> outputs = childJob1.getOutputs();
		assertEquals("var1", ((Variable)outputs.iterator().next()).getName());
		
		assertEquals("child_job2", childJob2.getName());
		assertEquals(1, childJob2.getTasks().size());
		taskIterator = childJob2.getTasks().iterator();
		task1 = taskIterator.next();
		assertTrue(task1 instanceof Assignment);
		assignment = (Assignment) task1;
		assertEquals("var2", assignment.getName());
		assertEquals("${input1} result - ${input2}", assignment.getRvalue().toString());
		inputs = childJob2.getInputs();
		assertEquals(2, inputs.size());
		iterator2 = inputs.iterator();
		assertEquals("input1", iterator2.next());
		assertEquals("input2", iterator2.next());
	}
	
	public void testSequencedJobsParallelConfig_Basic() throws Exception {
		String basicWorkflow = "workflow {\n" +
		"def job child_job1(input1) : dbNode=*[1-2] {\n" +
		"} \n" +
		"}";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));
	
		assertEquals(1, workflow.getJobs().size());
		Job childJob = workflow.getJobs().values().iterator().next();
		assertEquals(1, childJob.getConfigVariables().size());
		assertEquals("dbNode", childJob.getConfigVariables().keySet().iterator().next());
		Expression dbNode = childJob.getConfigVariables().values().iterator().next();
		assertTrue(dbNode instanceof ParallelVariable);
		ParallelVariable dbNodeValue = (ParallelVariable)dbNode;
		assertTrue(dbNodeValue.getExpression() instanceof ListExpression);
		Primitive contents = dbNodeValue.getExpression().evaluate(null);
		assertTrue(contents instanceof ListPrimitive);
		List<Primitive> range = ((ListPrimitive)contents).expressions();
		assertEquals(2, range.size());
		for (Primitive p : range) {
			assertTrue("12".contains(p.evaluate()));
		}
	}

/*	public void testEscapedStringParsing() throws Exception {
		String basicWorkflow = "workflow {\n" +
				"def job parent() {\n" +
				"   top1 = \"test1\\ntest2\\ntest3\";\n"  +
				"   top2 = \"test1\\ttest2\\ttest3\";\n"  +
				"}\n" +
			"}";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		assertEquals(1, workflow.getJobs().size());
		Job parentJob = workflow.getJobs().values().iterator().next();
		
	}
*/
}
