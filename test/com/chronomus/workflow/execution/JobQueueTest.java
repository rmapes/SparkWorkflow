package com.chronomus.workflow.execution;

import com.chronomus.workflow.execution.JobQueue;
import com.chronomus.workflow.execution.ServiceContext;
import com.chronomus.workflow.persistence.MockServiceDbAccessor;

import junit.framework.TestCase;

public class JobQueueTest extends TestCase {
	
	private MockServiceDbAccessor mockDbAccessor = new MockServiceDbAccessor();

	public void testJobQueue() {
		ServiceContext context = new ServiceContext();
		JobQueue queue = new JobQueue("dummy", mockDbAccessor);
		queue.add(context);
		assertEquals(context, queue.pop());
	}

}
