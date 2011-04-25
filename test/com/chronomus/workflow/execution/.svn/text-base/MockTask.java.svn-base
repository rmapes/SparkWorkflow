package com.chronomus.workflow.execution;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.ServiceContext;
import com.chronomus.workflow.execution.Task;

public class MockTask implements Task {

	private boolean completed;

	@Override
	public void run(ServiceContext context) throws ExecutionException {
		setCompleted(true);
	}

	private void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isCompleted() {
		return completed;
	}

}
