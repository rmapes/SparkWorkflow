package com.chronomus.workflow.execution;

public class JobQueueTrigger implements Trigger {

	private static final int QUEUE_POLLING_PERIOD = 1000;
	private Service target;
	private final JobQueue queue;
	private final JobQueue returnQueue;
	private boolean shouldRun = true;

	public JobQueueTrigger(JobQueue queue, JobQueue returnQueue, Service target) {
		this.queue = queue;
		this.returnQueue = returnQueue;
		this.target = target;
	}

	public Service getTarget() {
		return target;
	}

	public JobQueue getJobQueue() {
		return queue;
	}

	public JobQueue getReturnQueue() {
		return returnQueue;
	}

	@Override
	public void run() {
		// poll queue
		while (shouldRun) {
			for (ServiceContext context = queue.pop(); context != null; context = queue.pop()) {
				try {
					getTarget().run(context);
					if (getReturnQueue() != null) {
						getReturnQueue().add(context);
					}
				} catch (ExecutionException e) {
					// TODO Provide some kind of logging.
					e.printStackTrace();
				}
				Thread.yield();
			}
			try {
				Thread.sleep(QUEUE_POLLING_PERIOD);
			} catch (InterruptedException e) {
				shouldRun = false;
			}
		}
	}
}
