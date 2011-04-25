package com.chronomus.workflow.persistence;

import java.io.IOException;
import java.io.StringReader;
import com.chronomus.workflow.compiler.CompilerError;
import com.chronomus.workflow.definition.Workflow;
import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.ServiceContext;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.jmx.JmxServer;
import com.chronomus.workflow.parsers.ParseException;
import com.chronomus.workflow.parsers.SparkWorkflowParser;
import com.chronomus.workflow.persistence.ServiceDbAccessor;
import com.chronomus.workflow.persistence.ServiceDbAccessorImpl;

import junit.framework.TestCase;

public class PersistenceTest extends TestCase {

	private ServiceDbAccessor dbAccessor;
	private JmxServer mockJmxServer = new JmxServer("dummy", 0);
	
	public void setUp() {
		dbAccessor = new ServiceDbAccessorImpl();
	}

	public class MockBlockingTask implements Task {
		
		boolean runToPause = false;
		boolean runToEnd = false;
		
		boolean pause = true;

		@Override
		public void run(ServiceContext context) {
			runToPause = true;
			while (pause);
			runToEnd = true;
		}
		
		public void unblock() {
			pause = false;
		}
	}
	
	class WorkflowRunner implements Runnable {
		private Workflow workflow;

		public WorkflowRunner(Workflow workflow) {
			super();
			this.workflow = workflow;
		}

		@Override
		public void run() {
			try {
				workflow.getService("test").run();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}			
		
	}

	@SuppressWarnings("deprecation")
	public void testSingleMethodPersistence() throws IOException, ParseException, CompilerError {
		String workflowStr = "workflow {\n" +
				"def job test() {\n" +
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		MockBlockingTask execution = new MockBlockingTask();
		workflow.getJobs().get("test").addTask(execution);
		
		workflow.compile(mockJmxServer);
		
		assertTrue(!execution.runToPause);
		assertTrue(!execution.runToEnd);
		
		Thread parallelRun = new Thread(new WorkflowRunner(workflow));
		parallelRun.setDaemon(true);
		parallelRun.start();
		
		for (int i = 0; !execution.runToPause; i++) {
			if (i>10000000) fail("Timeout waiting for blocking task to start");
			Thread.yield();
		}
		assertTrue(execution.runToPause);
		assertTrue(!execution.runToEnd);
		
		// I mean to do this. We're simulated an unexpected shutdown.
		parallelRun.stop();
		execution = null;
		workflow = null;
		parallelRun = null;
		
		workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		execution = new MockBlockingTask();
		workflow.getJobs().get("test").addTask(execution);
		
		workflow.compile(new JmxServer("dummy", 0));
		execution.unblock();
		// We need to call the launch services command here to restart any blocked jobs.
		// Since the test service did not complete it should launch automatically
		workflow.launchServices();
		
		for (int i = 0; !execution.runToEnd; i++) {
			if (i>10000000) fail("Timeout waiting for blocking task to complete");
			Thread.yield();
		}
		assertTrue(execution.runToEnd);		
		
		// Check that all contexts are cleared
		assertEquals(0, dbAccessor.getNumContexts());
	}
	
	@SuppressWarnings("deprecation")
	public void testTwoMethodsPersistenceBlockOnFirst() throws IOException, ParseException, CompilerError {
		String workflowStr = "workflow {\n" +
				"def job test() {\n" +
				"test1();\n" +
				"test2();\n" +
				"}\n" +
				"def job test1() {\n" +
				"}\n" +
				"def job test2() {\n" +
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		MockBlockingTask execution = new MockBlockingTask();
		workflow.getJobs().get("test1").addTask(execution);
		MockBlockingTask execution2 = new MockBlockingTask();
		workflow.getJobs().get("test2").addTask(execution2);
		execution2.unblock();
	
		workflow.compile(mockJmxServer);
		
		assertTrue(!execution.runToPause);
		assertTrue(!execution.runToEnd);
		assertTrue(!execution2.runToPause);
		assertTrue(!execution2.runToEnd);
		
		Thread parallelRun = new Thread(new WorkflowRunner(workflow));
		parallelRun.setDaemon(true);
		parallelRun.start();
		
		for (int i = 0; !execution.runToPause; i++) {
			if (i>10000000) fail("Timeout waiting for blocking task to start");
			Thread.yield();
		}
		assertTrue(execution.runToPause);
		assertTrue(!execution.runToEnd);
		assertTrue(!execution2.runToPause);
		assertTrue(!execution2.runToEnd);
		
		// I mean to do this. We're simulated an unexpected shutdown.
		parallelRun.stop();
		execution = null;
		workflow = null;
		parallelRun = null;
		
		workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		execution = new MockBlockingTask();
		workflow.getJobs().get("test1").addTask(execution);
		execution2 = new MockBlockingTask();
		workflow.getJobs().get("test2").addTask(execution2);
		execution2.unblock();
		
		workflow.compile(new JmxServer("dummy", 0));
		execution.unblock();
		// We need to call the launch services command here to restart any blocked jobs.
		// Since the test service did not complete it should launch automatically
		workflow.launchServices();
		
		for (int i = 0; !execution.runToEnd; i++) {
			if (i>10000000) fail("Timeout waiting for blocking task to complete");
			Thread.yield();
		}
		assertTrue(execution.runToEnd);		
		for (int i = 0; !execution2.runToEnd; i++) {
			if (i>10000000) fail("Timeout waiting for second task to complete");
			Thread.yield();
		}
		assertTrue(execution2.runToPause);
		assertTrue(execution2.runToEnd);
	}

	@SuppressWarnings("deprecation")
	public void testTwoMethodsPersistenceBlockOnSecond() throws IOException, ParseException, CompilerError {
		String workflowStr = "workflow {\n" +
				"def job test() {\n" +
				"test1();\n" +
				"test2();\n" +
				"}\n" +
				"def job test1() {\n" +
				"}\n" +
				"def job test2() {\n" +
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		MockBlockingTask execution = new MockBlockingTask();
		workflow.getJobs().get("test1").addTask(execution);
		MockBlockingTask execution2 = new MockBlockingTask();
		workflow.getJobs().get("test2").addTask(execution2);
		execution.unblock();
	
		workflow.compile(mockJmxServer);
		
		assertTrue(!execution.runToPause);
		assertTrue(!execution.runToEnd);
		assertTrue(!execution2.runToPause);
		assertTrue(!execution2.runToEnd);
		
		Thread parallelRun = new Thread(new WorkflowRunner(workflow));
		parallelRun.setDaemon(true);
		parallelRun.start();
		
		for (int i = 0; !execution2.runToPause; i++) {
			if (i>10000000) fail("Timeout waiting for blocking task to start");
			Thread.yield();
		}
		assertTrue(execution.runToPause);
		assertTrue(execution.runToEnd);
		assertTrue(execution2.runToPause);
		assertTrue(!execution2.runToEnd);
		
		// I mean to do this. We're simulated an unexpected shutdown.
		parallelRun.stop();
		execution = null;
		workflow = null;
		parallelRun = null;
		
		workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		execution = new MockBlockingTask();
		workflow.getJobs().get("test1").addTask(execution);
		execution2 = new MockBlockingTask();
		workflow.getJobs().get("test2").addTask(execution2);
		execution.unblock();
		
		workflow.compile(new JmxServer("dummy", 0));
		execution2.unblock();
		// We need to call the launch services command here to restart any blocked jobs.
		// Since the test service did not complete it should launch automatically
		workflow.launchServices();
		
		for (int i = 0; !execution2.runToEnd; i++) {
			if (i>10000000) fail("Timeout waiting for second task to complete");
			Thread.yield();
		}
		assertTrue(!execution.runToPause);
		assertTrue(!execution.runToEnd);
		assertTrue(execution2.runToPause);
		assertTrue(execution2.runToEnd);
	}
	
	@SuppressWarnings("deprecation")
	public void testTwoMethodsPersistenceBlockOnSecondFirstMethodCallIsAssignment() throws IOException, ParseException, CompilerError {
		String workflowStr = "workflow {\n" +
				"def job test() {\n" +
				"alpha = test1();\n" +
				"test2();\n" +
				"}\n" +
				"def job test1() {\n" +
				"	return \"anything\";\n" +
				"}\n" +
				"def job test2() {\n" +
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		MockBlockingTask execution = new MockBlockingTask();
		workflow.getJobs().get("test1").addTask(execution);
		MockBlockingTask execution2 = new MockBlockingTask();
		workflow.getJobs().get("test2").addTask(execution2);
		execution.unblock();
	
		workflow.compile(mockJmxServer);
		
		assertTrue(!execution.runToPause);
		assertTrue(!execution.runToEnd);
		assertTrue(!execution2.runToPause);
		assertTrue(!execution2.runToEnd);
		
		Thread parallelRun = new Thread(new WorkflowRunner(workflow));
		parallelRun.setDaemon(true);
		parallelRun.start();
		
		for (int i = 0; !execution2.runToPause; i++) {
			if (i>10000000) fail("Timeout waiting for blocking task to start");
			Thread.yield();
		}
		assertTrue(execution.runToPause);
		assertTrue(execution.runToEnd);
		assertTrue(execution2.runToPause);
		assertTrue(!execution2.runToEnd);
		
		// I mean to do this. We're simulated an unexpected shutdown.
		parallelRun.stop();
		execution = null;
		workflow = null;
		parallelRun = null;
		
		workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		execution = new MockBlockingTask();
		workflow.getJobs().get("test1").addTask(execution);
		execution2 = new MockBlockingTask();
		workflow.getJobs().get("test2").addTask(execution2);
		execution.unblock();
		
		workflow.compile(new JmxServer("dummy", 0));
		execution2.unblock();
		// We need to call the launch services command here to restart any blocked jobs.
		// Since the test service did not complete it should launch automatically
		workflow.launchServices();
		
		for (int i = 0; !execution2.runToEnd; i++) {
			if (i>10000000) fail("Timeout waiting for second task to complete");
			Thread.yield();
		}
		assertTrue(!execution.runToPause);
		assertTrue(!execution.runToEnd);
		assertTrue(execution2.runToPause);
		assertTrue(execution2.runToEnd);
	}

	@SuppressWarnings("deprecation")
	public void testTwoMethodsPersistenceBlockOnSecondSecondMethodCallIsAssignment() throws IOException, ParseException, CompilerError {
		String workflowStr = "workflow {\n" +
				"def job test() {\n" +
				"test1();\n" +
				"alpha = test2();\n" +
				"}\n" +
				"def job test1() {\n" +
				"}\n" +
				"def job test2() {\n" +
				"	return \"anything\";\n" +
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		MockBlockingTask execution = new MockBlockingTask();
		workflow.getJobs().get("test1").addTask(execution);
		MockBlockingTask execution2 = new MockBlockingTask();
		workflow.getJobs().get("test2").addTask(execution2);
		execution.unblock();
	
		workflow.compile(mockJmxServer);
		
		assertTrue(!execution.runToPause);
		assertTrue(!execution.runToEnd);
		assertTrue(!execution2.runToPause);
		assertTrue(!execution2.runToEnd);
		
		Thread parallelRun = new Thread(new WorkflowRunner(workflow));
		parallelRun.setDaemon(true);
		parallelRun.start();
		
		for (int i = 0; !execution2.runToPause; i++) {
			if (i>10000000) fail("Timeout waiting for blocking task to start");
			Thread.yield();
		}
		assertTrue(execution.runToPause);
		assertTrue(execution.runToEnd);
		assertTrue(execution2.runToPause);
		assertTrue(!execution2.runToEnd);
		
		// I mean to do this. We're simulated an unexpected shutdown.
		parallelRun.stop();
		execution = null;
		workflow = null;
		parallelRun = null;
		
		workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new StringReader(workflowStr));
		execution = new MockBlockingTask();
		workflow.getJobs().get("test1").addTask(execution);
		execution2 = new MockBlockingTask();
		workflow.getJobs().get("test2").addTask(execution2);
		execution.unblock();
		
		workflow.compile(new JmxServer("dummy", 0));
		execution2.unblock();
		// We need to call the launch services command here to restart any blocked jobs.
		// Since the test service did not complete it should launch automatically
		workflow.launchServices();
		
		for (int i = 0; !execution2.runToEnd; i++) {
			if (i>10000000) fail("Timeout waiting for second task to complete");
			Thread.yield();
		}
		assertTrue(!execution.runToPause);
		assertTrue(!execution.runToEnd);
		assertTrue(execution2.runToPause);
		assertTrue(execution2.runToEnd);
	}
}
