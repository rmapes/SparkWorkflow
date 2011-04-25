package com.chronomus.workflow.execution;

import java.util.Properties;

import com.chronomus.workflow.execution.JobQueue;
import com.chronomus.workflow.execution.ProxyService;
import com.chronomus.workflow.execution.ServiceContext;
import com.chronomus.workflow.persistence.MockServiceDbAccessor;

import junit.framework.TestCase;

public class ProxyServiceTest extends TestCase {
	
	private MockServiceDbAccessor mockDbAccessor = new MockServiceDbAccessor();

	public void testProxyServiceOutputQueue() throws Exception {
		ProxyService proxy = new ProxyService("dummy", new Properties(), mockDbAccessor);
		JobQueue queue = proxy.getOutputQueue();
		assertEquals(null, queue.pop());
		proxy.run();
		ServiceContext context = queue.pop();
		assertNotNull(context);
	}
	
	public void testProxyServiceReturnQueue() throws Exception {
		// Create proxy
		ProxyService proxy = new ProxyService("dummy", new Properties(), mockDbAccessor);
		// Launch trigger thread
		Thread triggerThread = new Thread(proxy.getReturnQueueTrigger());
		triggerThread.setDaemon(true);
		triggerThread.start(); 
		MockTask task = new MockTask();
		proxy.addTask(task);
		JobQueue queue = proxy.getReturnQueue();
		assertTrue(!task.isCompleted());
		queue.add(new ServiceContext());
		int sentinel = 0;
		while(!task.isCompleted() && sentinel < 1000) {
			sentinel++;
			Thread.sleep(1);
		}
		assertTrue(task.isCompleted());
		
	}

}
