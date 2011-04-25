package com.chronomus.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Iterator;

import junit.framework.TestCase;

import com.chronomus.workflow.definition.Job;
import com.chronomus.workflow.definition.Workflow;
import com.chronomus.workflow.execution.Assignment;
import com.chronomus.workflow.execution.CommandLines;
import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.JobQueueTrigger;
import com.chronomus.workflow.execution.JobService;
import com.chronomus.workflow.execution.MethodCall;
import com.chronomus.workflow.execution.ParallelMethod;
import com.chronomus.workflow.execution.ProxyService;
import com.chronomus.workflow.execution.Service;
import com.chronomus.workflow.execution.ServiceContext;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.execution.Trigger;
import com.chronomus.workflow.jmx.JmxServer;
import com.chronomus.workflow.parsers.SparkWorkflowParser;
import com.chronomus.workflow.persistence.MockServiceDbAccessor;

/**
 * Test to validate full lifecycle from Spark code to compiled services
 * @author Richard.Mapes
 *
 */
public class CompileFromCodeTest extends TestCase {
	
	private MockServiceDbAccessor mockDbAccessor = new MockServiceDbAccessor();
	private JmxServer mockJmxServer = new JmxServer("dummy", 0);

	/**
	 * Test creation of a basic service with a single task
	 * @throws Exception
	 */
	public void testBasicWorkflow() throws Exception {
		// Parse from code
		String basicWorkflow = "workflow { def job helloWorld() { runCommand(\"" + CommandLines.helloWorld() + "\"); } }";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertTrue(helloWorldService instanceof JobService);
		assertEquals(1, ((JobService) helloWorldService).getTasks().size());
		Task task = ((JobService) helloWorldService).getTasks().iterator().next();
		assertTrue(task instanceof MethodCall);
		assertTrue(((MethodCall) task).getName().equals("runCommand"));
		
		// Test run
		File testFile = new File(CommandLines.testFileLocation("helloWorldOut.txt"));
		testFile.delete();
		assertTrue(!testFile.exists());
		helloWorldService.run();
		assertTrue(testFile.exists());
		// Check that all contexts are cleared
		assertEquals(0, mockDbAccessor.getNumContexts());
	}
	
	/**
	 * Test creation of a single job with a single task where that task sets a variable
	 * @throws Exception
	 */
	public void testBasicAssignmentToVariable() throws Exception {
		// Parse from code
		String basicWorkflow = "workflow { def job helloWorld() { testVariable = \"hello\"; } }";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		
		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertTrue(helloWorldService instanceof JobService);
		assertEquals(1, ((JobService) helloWorldService).getTasks().size());
		Task task = ((JobService) helloWorldService).getTasks().iterator().next();
		assertTrue(task instanceof Assignment);
		
		// Test run
		helloWorldService.run();
		assertEquals("hello",mockDbAccessor.getContextVariable(helloWorldService, "testVariable").toString());
		// Check that all contexts are cleared
		assertEquals(0, mockDbAccessor.getNumContexts());
	}

	public void testCommandWithVariableInParameters() throws Exception {
		String outputFile = CommandLines.testFileLocation("CommandWithVariableInParameters.txt");

		String basicWorkflow = "workflow { def job helloWorld() { testVariable = \"" + CommandLines.outputToFile() + " hello " + outputFile + "\"; " +
				"runCommand(testVariable);} }";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		
		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertTrue(helloWorldService instanceof JobService);
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
		// Check that all contexts are cleared
		assertEquals(0, mockDbAccessor.getNumContexts());
	}
	
	public void testCommandWithVariableEmbeddedInParameters() throws Exception {
		String outputFile = CommandLines.testFileLocation("embeddedTest.txt");
		
		// Parse from code
		String basicWorkflow = "workflow { def job helloWorld() { testVariable = \"hello\"; " +
				"runCommand(\"" + CommandLines.outputToFile() + " ${testVariable} " + outputFile + "\");} }";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertTrue(helloWorldService instanceof JobService);
		assertEquals(2, ((JobService) helloWorldService).getTasks().size());
		Iterator<Task> iterator = ((JobService) helloWorldService).getTasks().iterator();
		Task task = iterator.next();
		assertTrue(task instanceof Assignment);
		Task task2 = iterator.next();
		assertTrue(task2 instanceof MethodCall);
		assertTrue(((MethodCall) task2).getName().equals("runCommand"));
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
		// Check that all contexts are cleared
		assertEquals(0, mockDbAccessor.getNumContexts());
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
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(5, workflow.getJobServices().size());
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
		assertEquals("true",mockDbAccessor.getContextVariable(child1Service, "var1").toString());
		assertEquals("apple",mockDbAccessor.getContextVariable(child2Service, "var2").toString());
		
		// Check that all contexts are cleared
		assertEquals(0, mockDbAccessor.getNumContexts());
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
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(5, workflow.getJobServices().size());
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
		assertEquals("Expected: test1 = true",mockDbAccessor.getContextVariable(child1Service, "var1").toString());
		assertEquals("test1 result - Expected: test1 = true", mockDbAccessor.getContextVariable(child2Service, "var2"));
		// Check that all contexts are cleared
		assertEquals(0, mockDbAccessor.getNumContexts());
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
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

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
		// Check that all contexts are cleared
		assertEquals(0, mockDbAccessor.getNumContexts());
	}

	public void testSequencedJobsParallelConfig_Basic() throws Exception {
		String basicWorkflow = "workflow {\n" +
		"def job child_job1(input1) : dbNode=*[1-2] {\n" +
		"} \n" +
		"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		// Now compile and check
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
	
	public void testSequencedJobsParallelConfig() throws Exception {
		 String basicWorkflow = "workflow {\n" +
			"def job parent() {\n" +
			"   top1 = \"test1\\ntest2\\ntest3\";\n"  +
			"	top2 = child_job1(*split(top1));\n" +
			"	child_job2(top1, top2);\n" +
			"}\n" +
			"def job child_job1(input1) : dbNode=*[1-2] {\n" +
			"	var1 = \"Expected: ${input1} = true\";\n" +
			"	return var1;\n" +
			"} \n" +
			"def job child_job2(input1, input2) {\n" +
			"}\n" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		// Add counting task to check job completed
		Job finalJob = workflow.getJobs().get("child_job2");
		final CountingTask countingTask = new CountingTask();
		finalJob.addTask(countingTask);
		
		// Now compile and run
		workflow.compile(mockJmxServer);
		assertEquals(0, countingTask.finalTaskCount);
		workflow.getService("parent").run();
		waitFor("parent", 1);
		waitFor("parent_parallel", 3);
		waitFor("parent_parallel_child_job1", 3);
		waitFor("parent_parallel_child_job1_child_job2", 3);
		assertEquals(3, countingTask.finalTaskCount);
	}
	
	class CountingTask implements Task {

		private int finalTaskCount = 0;

		@Override
		public void run(ServiceContext context) throws ExecutionException {
			finalTaskCount ++;
		}
			
	}

	private void waitFor(String serviceName, int parallelRuns) {
		for (int i = 0; mockDbAccessor.getServiceCompletedContextStore(serviceName).size() < parallelRuns; i++) {
			if (i>=10000000) 
				assertEquals(parallelRuns, mockDbAccessor.getServiceCompletedContextStore(serviceName).size());
		}
	}
}
