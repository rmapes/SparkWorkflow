package com.chronomus.workflow.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.chronomus.workflow.persistence.ServiceDbAccessor;

public class JobService extends Service {

	protected ServiceDbAccessor dbAccessor;
	private List<Task> tasks = new ArrayList<Task>();
	
	public JobService(String name, Properties baseContext, ServiceDbAccessor dbAccessor) {
		super(name, baseContext);
		this.dbAccessor = dbAccessor;
	}

	public void addTask(Task task) {
		tasks .add(task);
	}

	protected void run(ServiceContext context) throws ExecutionException {
		storeContext(context);
		for (Task task : tasks) {
			if (task instanceof MethodCall) {
				if (((MethodCall) task).isJobService()) { //TODO: do something more clever here.
					markContextComplete(context);
				}
			}
			task.run(context);
		}
		markContextComplete(context);
	}
	
	void markContextComplete(ServiceContext context) {
		dbAccessor.markContextComplete(this.name, context);
	}

	private void storeContext(ServiceContext context) {
		dbAccessor.storeContext(this.name, context);
	}

	// For tests only
	public List<Task> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	public void launchService() {
		// Read contexts from database
		Collection<ServiceContext> contexts = new ArrayList<ServiceContext>(dbAccessor.getContexts(this.name));
		// Start a thread for each context
		for (ServiceContext context : contexts) {
			Thread restart = new RestartTrigger(this, context);
			restart.setDaemon(true);
			restart.start();
		}
	}
}
