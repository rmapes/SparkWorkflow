package com.chronomus.workflow.execution;

public class WorkflowFailureException extends ExecutionException {

	private final String command;
	private StringBuilder output;

	public WorkflowFailureException(String command) {
		super(String.format("Workflow failed while running %s", command));
		this.command = command;
	}

	public WorkflowFailureException(String command, StringBuilder output) {
		this(command);
		this.output = output;
	}
}
