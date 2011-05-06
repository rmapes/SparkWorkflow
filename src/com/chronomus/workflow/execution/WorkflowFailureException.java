package com.chronomus.workflow.execution;

public class WorkflowFailureException extends ExecutionException {

	/**
	 * Version 1: regenerate this id if version changes
	 */
	private static final long serialVersionUID = 4988798919613719244L;
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

	public String getCommand() {
		return command;
	}

	public StringBuilder getOutput() {
		return output;
	}
}
