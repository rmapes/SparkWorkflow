package com.chronomus.workflow.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.chronomus.workflow.definition.Job;
import com.chronomus.workflow.definition.Workflow;
import com.chronomus.workflow.execution.Assignment;
import com.chronomus.workflow.execution.CommandLines;
import com.chronomus.workflow.execution.JobQueueTrigger;
import com.chronomus.workflow.execution.JobService;
import com.chronomus.workflow.execution.MethodCall;
import com.chronomus.workflow.execution.ParallelMethod;
import com.chronomus.workflow.execution.ProxyService;
import com.chronomus.workflow.execution.Service;
import com.chronomus.workflow.execution.ServiceContext;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.execution.Trigger;
import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.ListExpression;
import com.chronomus.workflow.execution.expressions.ParallelVariable;
import com.chronomus.workflow.execution.expressions.RangeExpression;
import com.chronomus.workflow.execution.expressions.StringExpression;
import com.chronomus.workflow.execution.expressions.Variable;
import com.chronomus.workflow.jmx.JmxServer;
import com.chronomus.workflow.persistence.MockServiceDbAccessor;

public class WorkflowCompilerTest extends TestCase {
		
	private MockServiceDbAccessor mockDbAccessor = new MockServiceDbAccessor();
	private JmxServer mockJmxServer = new JmxServer("dummy", 0);

	public void testBasicWorkflow() throws Exception {
		String outputFile = CommandLines.testFileLocation("helloWorldOutCompiler.txt");
		Workflow workflow = new Workflow(mockDbAccessor);
		MethodCall helloWorldTask = new MethodCall("runCommand", workflow);
		List<Expression> params = new ArrayList<Expression>();
		params.add(new StringExpression(CommandLines.outputToFile() + "  Hello " + outputFile));
		helloWorldTask.setParameters(params);
		Job helloWorld = new Job("helloWorld");
		helloWorld.addTask(helloWorldTask);
		workflow.addDefinition(helloWorld);
		
		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertEquals(1, ((JobService) helloWorldService).getTasks().size());
		Task task = ((JobService) helloWorldService).getTasks().iterator().next();
		assertTrue(task instanceof MethodCall);
		assertTrue(((MethodCall) task).getName().equals("runCommand"));
		
		// Test run
		File testFile = new File(outputFile);
		testFile.delete();
		assertTrue(!testFile.exists());
		helloWorldService.run();
		assertTrue(testFile.exists());
		// Read content
		BufferedReader reader = new BufferedReader(new FileReader(testFile));
		assertEquals("Hello", reader.readLine());
	}

	public void testAssignmentsInWorkflow() throws Exception {
		Workflow workflow = new Workflow(mockDbAccessor);
		Assignment helloWorldTask = new Assignment("testVariable", new StringExpression("hello"));
		Job helloWorld = new Job("helloWorld");
		helloWorld.addTask(helloWorldTask);
		workflow.addDefinition(helloWorld);
		
		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertEquals(1, ((JobService) helloWorldService).getTasks().size());
		Task task = ((JobService) helloWorldService).getTasks().iterator().next();
		assertTrue(task instanceof Assignment);
		
		// Test run
		helloWorldService.run();
		assertEquals("hello",mockDbAccessor.getContextVariable(helloWorldService, "testVariable").toString());
	}
	
	public void testCommandWithVariableInParameters() throws Exception {
		String outputFile = CommandLines.testFileLocation("output.txt");

		Workflow workflow = new Workflow(mockDbAccessor);
		Job helloJob = new Job("helloWorld");
		workflow.addDefinition(helloJob);
		Assignment Assignment = new Assignment("testVariable", new StringExpression(CommandLines.outputToFile() + "  hello " + outputFile));
		MethodCall runCommand = new MethodCall("runCommand", workflow);
		List<Expression> parseParameters = new ArrayList<Expression>();
		parseParameters.add(new Variable("testVariable"));
		runCommand.setParameters(parseParameters);
		helloJob.addTask(Assignment);
		helloJob.addTask(runCommand);
		
		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertEquals(2, ((JobService) helloWorldService).getTasks().size());
		Iterator<Task> iterator = ((JobService) helloWorldService).getTasks().iterator();
		Task task = iterator.next();
		assertTrue(task instanceof Assignment);
		Task task2 = iterator.next();
		assertTrue(task2 instanceof MethodCall);
		assertTrue(((MethodCall) task2).getName().equals("runCommand"));

		// Test run
		File testFile = new File(outputFile);
		testFile.delete();
		assertTrue(!testFile.exists());
		helloWorldService.run();
		assertTrue(testFile.exists());
		// Read content
		BufferedReader reader = new BufferedReader(new FileReader(testFile));
		assertEquals("hello", reader.readLine());
	}

	public void testCommandWithVariableEmbeddedInParameters() throws Exception {
		String outputFile = CommandLines.testFileLocation("variableEmbeddedTest");
		
		Workflow workflow = new Workflow(mockDbAccessor);
		Job helloJob = new Job("helloWorld");
		workflow.addDefinition(helloJob);
		Assignment Assignment = new Assignment("testVariable", new StringExpression("hello"));
		MethodCall runCommand = new MethodCall("runCommand", workflow);
		List<Expression> parseParameters = new ArrayList<Expression>();
		parseParameters.add(new StringExpression(CommandLines.outputToFile() + " ${testVariable} " + outputFile));
		runCommand.setParameters(parseParameters);
		helloJob.addTask(Assignment);
		helloJob.addTask(runCommand);
		
		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertEquals(2, ((JobService) helloWorldService).getTasks().size());
		Iterator<Task> iterator = ((JobService) helloWorldService).getTasks().iterator();
		Task task = iterator.next();
		assertTrue(task instanceof Assignment);
		Task task2 = iterator.next();
		assertTrue(task2 instanceof MethodCall);
		assertEquals(CommandLines.outputToFile() + " ${testVariable} " + outputFile, ((MethodCall) task2).getParameters().iterator().next().toString());

		// Test run
		File testFile = new File(outputFile);
		testFile.delete();
		assertTrue(!testFile.exists());
		helloWorldService.run();
		assertTrue(testFile.exists());
		// Read content
		BufferedReader reader = new BufferedReader(new FileReader(testFile));
		assertEquals("hello", reader.readLine());
	}
	
	public void testSequencedJobsSingleThreaded() throws Exception {
	/*	String basicWorkflow = "workflow {\n" +
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
			"}"; */
		Workflow workflow = new Workflow(mockDbAccessor);
		Job parent = new Job("parent");
		Job child1 = new Job("child_job1");
		Job child2 = new Job("child_job2");
		
		workflow.addDefinition(parent);
		MethodCall method1 = new MethodCall("child_job1", workflow);
		method1.setParameters(new ArrayList<Expression>());
		parent.addTask(method1);
		MethodCall method2 = new MethodCall("child_job2", workflow);
		method2.setParameters(new ArrayList<Expression>());
		parent.addTask(method2);

		workflow.addDefinition(child1);
		Assignment var1 = new Assignment("var1", new StringExpression("true"));
		child1.addTask(var1);

		workflow.addDefinition(child2);
		Assignment var2 = new Assignment("var2", new StringExpression("apple"));
		child2.addTask(var2);
		
		// Now compile and check
		workflow.compile(mockJmxServer);
//		assertEquals(3, workflow.getJobServices().size());
		Service parentService = workflow.getService("parent");
		Service child1Service = workflow.getService("parent_child_job1");
		Service child2Service = workflow.getService("parent_child_job1_child_job2");

		assertTrue("Parent service should exist", parentService != null);
		assertTrue("Child1 service should exist", child1Service != null);
		assertTrue("Child2 service should exist", child2Service != null);

		// Test run
		assertEquals(null,mockDbAccessor.getContextVariable(child1Service, "var1"));
		assertEquals(null,mockDbAccessor.getContextVariable(child2Service, "var2"));
		parentService.run();
		assertEquals("true", mockDbAccessor.getContextVariable(child1Service, "var1"));
		assertEquals("apple", mockDbAccessor.getContextVariable(child2Service, "var2"));
		
	}
	
	public void testSequencedJobsSingleThreadedParameterPassing() throws Exception {
		Workflow workflow = new Workflow(mockDbAccessor);
		Job parent = new Job("parent");
		Job child1 = new Job("child_job1");
		Job child2 = new Job("child_job2");
		
		Assignment task1 = new Assignment("top1", new StringExpression("test1"));
		parent.addTask(task1);
		MethodCall child1Call = new MethodCall("child_job1", workflow);
		List<Expression> child1Parameters = new ArrayList<Expression>();
		child1Parameters.add(new Variable("top1"));
		child1Call.setParameters(child1Parameters);
		Assignment task2 = new Assignment("top2", child1Call);
		parent.addTask(task2);
		MethodCall child2Call = new MethodCall("child_job2", workflow);
		List<Expression> child2Parameters = new ArrayList<Expression>();
		child2Parameters.add(new Variable("top1"));
		child2Parameters.add(new Variable("top2"));
		child2Call.setParameters(child2Parameters);
		parent.addTask(child2Call);
		workflow.addDefinition(parent);
		
		Assignment var1 = new Assignment("var1", new StringExpression("Expected: ${input1} = true"));
		List<String> inputs = new ArrayList<String>();
		inputs.add("input1");
		child1.setInputs(inputs);
		child1.addTask(var1);
		List<Expression> outputs = new ArrayList<Expression>();
		outputs.add(new Variable("var1"));
		child1.setOutputs(outputs);
		workflow.addDefinition(child1);

		Assignment var2 = new Assignment("var2", new StringExpression("${input1} result - ${input2}"));
		List<String> inputs2 = new ArrayList<String>();
		inputs2.add("input1");
		inputs2.add("input2");
		child2.setInputs(inputs2);
		child2.addTask(var2);
		workflow.addDefinition(child2);

		// Now compile and check
		workflow.compile(mockJmxServer);
//		assertEquals(3, workflow.getJobServices().size());
		Service parentService = workflow.getService("parent");
		Service child1Service = workflow.getService("parent_child_job1");
		Service child2Service = workflow.getService("parent_child_job1_child_job2");
		assertTrue("Parent service should exist", parentService != null);
		assertTrue("Child1 service should exist", child1Service != null);
		assertTrue("Child2 service should exist", child2Service != null);

		// Test run
		assertEquals(null,mockDbAccessor.getContextVariable(child1Service, "var1"));
		assertEquals(null,mockDbAccessor.getContextVariable(child2Service, "var2"));
		parentService.run();
		assertEquals("Expected: test1 = true", mockDbAccessor.getContextVariable(child1Service, "var1"));
		assertEquals("test1 result - Expected: test1 = true", mockDbAccessor.getContextVariable(child2Service, "var2"));
	}

	public void testSequencedJobsParallelParameterPassing() throws Exception {
		/*String basicWorkflow = "workflow {\n" +
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
			"}";*/
		Workflow workflow = new Workflow(mockDbAccessor);
		
		Job parentJob = new Job("parent");
		Job childJob1 = new Job("child_job1");
		Job childJob2 = new Job("child_job2");

		workflow.addDefinition(parentJob);
		workflow.addDefinition(childJob1);
		workflow.addDefinition(childJob2);
		
		Task task1 = new Assignment("top1", new StringExpression("test1\ntest2\ntest3"));
		parentJob.addTask(task1);
		MethodCall childJob1Call = new MethodCall("child_job1", workflow);
		List<Expression> parameters = new ArrayList<Expression>();
		MethodCall splitCall = new MethodCall("split", workflow);
		List<Expression> splitParams = new ArrayList<Expression>();
		splitParams.add(new Variable("top1"));
		splitCall.setParameters(splitParams);
		parameters.add(new ParallelVariable(splitCall));
		childJob1Call.setParameters(parameters);
		Task task2 = new Assignment("top2", childJob1Call);
		parentJob.addTask(task2);
		MethodCall task3 = new MethodCall("child_job2", workflow);
		List<Expression> parameters2 = new ArrayList<Expression>();
		parameters2.add(new Variable("top1"));
		parameters2.add(new Variable("top2"));
		task3.setParameters(parameters2);
		parentJob.addTask(task3);
		
		Task child1Task1 = new Assignment("var1", new StringExpression("Expected: ${input1} = true"));
		childJob1.addTask(child1Task1);
		List<String> inputs = new ArrayList<String>();
		inputs.add("input1");
		childJob1.setInputs(inputs);
		List<Expression> outputs = new ArrayList<Expression>();
		outputs.add(new Variable("var1"));
		childJob1.setOutputs(outputs);
		
		Assignment child2Task1 = new Assignment("var2", new StringExpression("${input1} result - ${input2}"));
		childJob2.addTask(child2Task1);
		List<String> child2Inputs = new ArrayList<String>();
		child2Inputs.add("input1");
		child2Inputs.add("input2");
		childJob2.setInputs(child2Inputs);

		// Now compile and check
		workflow.compile(mockJmxServer);
//		assertEquals(3, workflow.getJobServices().size());
		Service parentService = workflow.getService("parent");
		Service parallelService = workflow.getService("parent_parallel");
		Service child1Service = workflow.getService("parent_parallel_child_job1");
		Service child2Service = workflow.getService("parent_parallel_child_job1_child_job2");
		assertTrue("Parent service should exist", parentService != null);
		assertTrue("Child1 service should exist", child1Service != null);
		assertTrue("Child2 service should exist", child2Service != null);
		
		assertTrue("Parallel service should exist", parallelService != null);
		assertEquals(1, ((JobService)parallelService).getTasks().size());

		assertEquals(3, ((JobService)child1Service).getTasks().size());

		// Test run
		assertEquals(null,mockDbAccessor.getContextVariable(child1Service, "var1"));
		assertEquals(null,mockDbAccessor.getContextVariable(child2Service, "var2"));
		parentService.run();
		waitFor("parent", 1);
		waitFor("parent_parallel", 3);
		waitFor("parent_parallel_child_job1", 3);
		waitFor("parent_parallel_child_job1_child_job2", 3);
		boolean test1Found = false, test2Found = false, test3Found = false;
		for (ServiceContext context : mockDbAccessor.getServiceCompletedContextStore("parent_parallel_child_job1_child_job2").values()) {
			String testVar = context.getVariables().get(ParallelMethod.OUTPUT).toString();
			if ("test1".equals(testVar)) {
				test1Found = true;
			} else if ("test2".equals(testVar)) {
				test2Found = true;
			} else if ("test3".equals(testVar)) {
				test3Found = true;
			} else {
				fail("Unrecognised parallel output " + testVar);
			}
			String expectedVar1 = "Expected: " + testVar + " = true";
			String expectedVar2 = "test1\ntest2\ntest3 result - " + expectedVar1;
			assertEquals(expectedVar1, context.getVariables().get("var1").toString());
			assertEquals(expectedVar2, context.getVariables().get("var2").toString());
		}
		assertTrue(test1Found);
		assertTrue(test2Found);
		assertTrue(test3Found);
	}

	public void testSequencedJobsParallelConfig_Basic() throws Exception {
/*		String basicWorkflow = "workflow {\n" +
		"def job child_job1(input1) : dbNode=*[1-2] {\n" +
		"} \n" +
		"}";
*/	
		Workflow workflow =  new Workflow(mockDbAccessor);
		Job childJob1 = new Job("child_job1");
		List<String> inputs = new ArrayList<String>();
		inputs.add("input1");
		childJob1.setInputs(inputs);
		ParallelVariable config = new ParallelVariable(new ListExpression(new RangeExpression(1, 2)));
		List<Assignment> assignments = new ArrayList<Assignment>();
		assignments.add(new Assignment("dbNode", config));
		childJob1.setConfig(assignments);
		workflow.addDefinition(childJob1);
		
		workflow.compile(mockJmxServer);
		// Should create 2 services, each attached to a JobQueueTrigger, plus
		// a control service, itself attached to a JobQueueTrigger to poll
		// for completed jobs and trigger the remaining tasks
		assertEquals(3, workflow.getJobServices().size());
		Service proxyService = workflow.getService("child_job1");
		assertNotNull(proxyService);
		assertTrue(proxyService instanceof ProxyService);
		Service consumerService1 = workflow.getService("child_job1_service1");
		assertNotNull(consumerService1);
		Service consumerService2 = workflow.getService("child_job1_service2");
		assertNotNull(consumerService2);
		assertEquals(3, workflow.getTriggers().size());
		JobQueueTrigger returnTriggerFound = null,
			service1TriggerFound = null, service2TriggerFound = null;
		// Check triggers
		for (Trigger trigger : workflow.getTriggers()) {
			if (trigger instanceof JobQueueTrigger) {
				Service targetService = ((JobQueueTrigger)trigger).getTarget();
				if (trigger==((ProxyService)proxyService).getReturnQueueTrigger()) {
					returnTriggerFound = (JobQueueTrigger)trigger;
				} else if (targetService==consumerService1) {
					service1TriggerFound = (JobQueueTrigger)trigger;
				} else if (targetService==consumerService2) {
					service2TriggerFound = (JobQueueTrigger)trigger;
				}
			}
		}
		assertNotNull(returnTriggerFound);
		assertNotNull(service1TriggerFound);
		assertNotNull(service2TriggerFound);
		// Check queues
		assertEquals(((ProxyService)proxyService).getOutputQueue(), service1TriggerFound.getJobQueue());
		assertEquals(((ProxyService)proxyService).getOutputQueue(), service2TriggerFound.getJobQueue());
		assertEquals(((ProxyService)proxyService).getReturnQueue(), service1TriggerFound.getReturnQueue());
		assertEquals(((ProxyService)proxyService).getReturnQueue(), service2TriggerFound.getReturnQueue());
	}

	// TODO: refactor this and corresponding in CompileFromCodeTest to be same
	private void waitFor(String serviceName, int parallelRuns) {
		for (int i = 0; mockDbAccessor.getServiceCompletedContextStore(serviceName).size() < parallelRuns; i++) {
			if (i>=10000000) 
				assertEquals(parallelRuns, mockDbAccessor.getServiceCompletedContextStore(serviceName).size());
		}
	}
}
