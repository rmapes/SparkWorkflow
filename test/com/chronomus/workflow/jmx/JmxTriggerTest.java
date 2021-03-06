package com.chronomus.workflow.jmx;

import java.io.StringReader;

import com.chronomus.workflow.definition.Job;
import com.chronomus.workflow.definition.Workflow;
import com.chronomus.workflow.execution.MockTask;
import com.chronomus.workflow.jmx.JmxClient;
import com.chronomus.workflow.parsers.SparkWorkflowParser;
import com.chronomus.workflow.persistence.MockServiceDbAccessor;

import junit.framework.TestCase;

public class JmxTriggerTest extends JmxTestCase {

	private MockServiceDbAccessor mockDbAccessor = new MockServiceDbAccessor();
	public void testBasicWorkflowTrigger() throws Exception {

		String basicWorkflow = "workflow {\n" +
		"entryPoint(testJob);\n" +
		"def job testJob()  {\n" +
		"} \n" +
		"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));
		Job testJob = workflow.getJobs().get("testJob");
		MockTask mockTask = new MockTask();
		testJob.addTask(mockTask);
		
		// Now compile and check
		workflow.compile(server);
		
		// Launch
		server.start();
		workflow.launchServices();
		workflow.launchTriggers();
		
		// Check state
		assertEquals(false, mockTask.isCompleted());
		
		// Create client and fire command
		JmxClient client = new JmxClient(host, port);
		//TODO(rmapes): specify name for workflow in script and use in jmxcall 
		client.runService("workflowRunner", "runService", new Object[] {"testJob"}, new String[] {String.class.getCanonicalName()});

		// Check state
		assertEquals(true, mockTask.isCompleted());		
	}
}
