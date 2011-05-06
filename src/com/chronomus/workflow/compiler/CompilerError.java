package com.chronomus.workflow.compiler;

public class CompilerError extends Exception {

	/**
	 * Version 1: regenerate this id if version changes
	 */
	private static final long serialVersionUID = 3466414790265159893L;

	public CompilerError(String errMsg) {
		super(errMsg);
	}
	
	public CompilerError(Throwable e) {
		super(e);
	}

}
