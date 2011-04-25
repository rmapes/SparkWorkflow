package com.chronomus.workflow.execution.expressions;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.VariableStore;

public interface Expression {
	
	Primitive evaluate(VariableStore context) throws ExecutionException;

}
