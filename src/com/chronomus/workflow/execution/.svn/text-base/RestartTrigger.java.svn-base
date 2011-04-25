package com.chronomus.workflow.execution;


public class RestartTrigger extends Thread {

	private final JobService jobService;
	private final ServiceContext context;

	public RestartTrigger(JobService jobService, ServiceContext context2) {
		this.jobService = jobService;
		this.context = context2;
	}

	@Override
	public void run() {
		try {
			this.jobService.run(context);
			this.jobService.markContextComplete(context);
			super.run();
		} catch (ExecutionException e) {
			// TODO: Signal to parent thread that restart should be aborted
			e.printStackTrace();
		}
	}

}
