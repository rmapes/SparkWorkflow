package com.chronomus.workflow.execution;

import java.util.Properties;

import com.chronomus.workflow.persistence.ServiceDbAccessor;

public class ProxyService extends JobService {

	private Trigger returnQueueTrigger;
	private JobQueue outputQueue;
	private JobQueue returnQueue;

	public ProxyService(String serviceName, Properties baseContext,
			ServiceDbAccessor dbAccessor) {
		super(serviceName, baseContext, dbAccessor);
		// Set up outgoing queue
		outputQueue = new JobQueue(serviceName+"OutputQueue", dbAccessor);
		// Set up return queue
		returnQueue = new JobQueue(serviceName+"ReturnQueue", dbAccessor);
		returnQueueTrigger = new JobQueueTrigger(returnQueue, null, new Service() {

			@Override
			protected void run(ServiceContext context)
					throws ExecutionException {
				getAccessor().markContextComplete(name, context);
				ProxyService.super.run(context);
			}
			
		});
	}

	protected ServiceDbAccessor getAccessor() {
		return dbAccessor;
	}

	@Override
	protected void run(ServiceContext context) throws ExecutionException {
		// delegate to request queue and wait for return queue to be populated
		getAccessor().storeContext(name, context);
		outputQueue.add(context);
	}

	public Trigger getReturnQueueTrigger() {
		return returnQueueTrigger;
	}

	public JobQueue getOutputQueue() {
		return outputQueue;
	}

	public JobQueue getReturnQueue() {
		return returnQueue;
	}

}
