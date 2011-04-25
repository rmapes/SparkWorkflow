package com.chronomus.workflow.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.chronomus.workflow.definition.Definition;
import com.chronomus.workflow.definition.Job;
import com.chronomus.workflow.definition.Workflow;
import com.chronomus.workflow.execution.Assignment;
import com.chronomus.workflow.execution.MethodCall;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.ListExpression;
import com.chronomus.workflow.execution.expressions.ParallelVariable;
import com.chronomus.workflow.execution.expressions.RangeExpression;
import com.chronomus.workflow.execution.expressions.StringExpression;
import com.chronomus.workflow.execution.expressions.Variable;
import com.chronomus.workflow.persistence.ServiceDbAccessor;

/**
 * Parse a text stream into a code representation of an uncompiled workflow definition.
 * 
 * The code follows the following format
 * 
 * workflow : (config_statement )* +  (definition)*
 * config_statement := ( variable_assignment | config_method_call )
 * definition := job_definition | method_definition  // Note, method_definition currently unimplemented
 * job_definition : job_signature + job_body
 * job_signature : name + param_definitions + job_config? 
 * job_body : ( variable_assignment | runtime_method_call )*  + return_statement?
 * return_statement : expression
 * job_config : (variable_assignment + ',')*
 * variable_assignment : variable_name + assignment_operator + expression  // TODO(rmapes): expand this to support tuples
 * config_method_call, runtime_method_call := method_call // Distinction is by the range of available methods, i.e. compile time issue
 * method_call : name + param_list
 * param_list : ( (expression + ',')* + expression )?
 * param_definitions : ( (name + ',')* + name )?
 * expression : simple_expression | complex_expression
 * simple_expression := variable_name | method_call | literal
 * complex_expression : expression + operator + expression
 * variable_name := name
 * 
 * name : [A-Za-z] + [A-z0-9]*  //Must not be a reserved word
 * assignment_operator : =
 * operator : [*+-/]
 * 
 * @author Richard.Mapes
 *
 */
public class SparkWorkflowParser {
	
	/**
	 * 
	 * @author Richard.Mapes
	 *
	 */
	
	class Tokens {
		public final static String WORKFLOW = "workflow";
		public final static String BLOCK_OPENER = "{";
		public final static String BLOCK_CLOSER = "}";
		public final static String DEFINITION = "def";
		public static final String JOB = "job";
		public static final String TRIGGER = "trigger";
		public static final String RETURN = "return";
	}
	
	class TokenTypes {
		public final static int BLOCK_OPENER = '{';//123; 
		public final static int BLOCK_CLOSER = '}';//125; 
		public final static int PARAMS_OPENER = '(';//40; 
		public final static int PARAMS_CLOSER = ')';//41; 
		public final static int TERMINATOR = ';';//59; 
		public final static int QUOTED_STRING = '"';//34; 
		public final static int ASSIGNMENT = '=';//61; 
		public static final int PARAM_SEPARATOR = ',';
		public static final int CONFIG_OPENER = ':';
		public static final int LIST_OPENER = '['; 
		public static final int LIST_CLOSER = ']'; 
		public static final int PARALLEL_VARIABLE = '*'; 
	}

	private ServiceDbAccessor dbAccessor;

	public SparkWorkflowParser(ServiceDbAccessor dbAccessor) {
		super();
		this.dbAccessor = dbAccessor;
	}

	public Workflow loadWorkflow(Reader input) throws ParseException {
		// Break the stream into tokens
		Workflow workflow = SparkParser.parse(dbAccessor, input);
		return workflow;
	}

	private Workflow parseWorkflow(StreamTokenizer tokenizer) throws ParseError, IOException {
		if (!Tokens.WORKFLOW.equalsIgnoreCase(tokenizer.sval)) {
			throw new ParseError("Definition file should start with workflow keyword", tokenizer);
		}
		tokenizer.nextToken();
		if (!(tokenizer.ttype == TokenTypes.BLOCK_OPENER)) {
			throw new ParseError("Incorrect formatting: could not find opening brace", tokenizer);
		}
		Workflow workflow = new Workflow(dbAccessor);
		parseWorkflowStatementBlock(tokenizer, workflow);
		return workflow;
	}

	private void parseWorkflowStatementBlock(StreamTokenizer tokenizer,
			Workflow workflow) throws ParseError, IOException {
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
			if (TokenTypes.BLOCK_CLOSER ==tokenizer.ttype) {
				return;
			} else 
			if (Tokens.DEFINITION.equals(tokenizer.sval)) {
				workflow.addDefinition(parseDefinition(workflow, tokenizer));
			} else {
				workflow.addTask(parseTask(workflow, tokenizer, TokenTypes.TERMINATOR));
				// Consume terminator
				tokenizer.nextToken();
			}
		}
		throw new ParseError("Unexpected end of file", tokenizer);
	}

	private Task parseTask(Workflow workflow, StreamTokenizer tokenizer, Integer... terminators) throws ParseError, IOException {
		List<Integer> terminatorList = Arrays.asList(terminators);
		if (tokenizer.ttype != StreamTokenizer.TT_WORD) {
			throw new ParseError("Unexpected statement type", tokenizer);
		}
		String name = tokenizer.sval;
		Task task = null;
		if (tokenizer.nextToken() == TokenTypes.PARAMS_OPENER) {
			task = new MethodCall(name, workflow);
			((MethodCall) task).setParameters(parseParameterValues(workflow, tokenizer));
			if (!terminatorList.contains(tokenizer.nextToken())) {
				throw new ParseError("Unterminated statement", tokenizer);
			}			
		} else if (tokenizer.ttype == TokenTypes.ASSIGNMENT) {
			task = new Assignment(name, parseExpression(workflow, tokenizer, terminators));
			if (!terminatorList.contains(tokenizer.nextToken())) {
				throw new ParseError("Unterminated statement", tokenizer);
			}
		} else {
			throw new ParseError(String.format("Unrecognised statement type: %s", name), tokenizer );
		}
		tokenizer.pushBack();
		return task;
	}

	private Expression parseExpression(Workflow workflow, StreamTokenizer tokenizer,
			Integer... terminators) throws IOException, ParseError {
		Expression leftHand = null;
		List<Integer> terminatorList = Arrays.asList(terminators);
		while (!terminatorList.contains(tokenizer.nextToken()) && tokenizer.ttype != StreamTokenizer.TT_EOF) {
			switch (tokenizer.ttype) {
				case (StreamTokenizer.TT_WORD): 
					// Distinguish between variable and method call
					String name = tokenizer.sval;
					if (tokenizer.nextToken() != TokenTypes.PARAMS_OPENER) {
						tokenizer.pushBack();
						leftHand = new Variable(name);
					} else {
						leftHand = new MethodCall(name, workflow);
						((MethodCall) leftHand).setParameters(parseParameterValues(workflow, tokenizer));						
					}
					break;
				case (TokenTypes.QUOTED_STRING):
					leftHand = new StringExpression(tokenizer.sval);
					break;
				case (TokenTypes.PARALLEL_VARIABLE):
					leftHand = new ParallelVariable(parseExpression(workflow, tokenizer, terminators));
					break;
				case (TokenTypes.LIST_OPENER):
					leftHand = new ListExpression(parseList(workflow, tokenizer));
					break;
				default:
					throw new ParseError(String.format("Unexpected token in expression: %c", tokenizer.ttype), tokenizer);
			} 
		}
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			throw new ParseError("Unexpected end of file", tokenizer);
		}
		tokenizer.pushBack();
		return leftHand;
	}

	private RangeExpression parseList(Workflow workflow, StreamTokenizer tokenizer) throws ParseError, IOException {
		if (tokenizer.nextToken()==StreamTokenizer.TT_NUMBER) {
			Double left = tokenizer.nval;	
			if (tokenizer.nextToken() != StreamTokenizer.TT_NUMBER) {
				throw new ParseError("Cannot handle list expression", tokenizer);				
			}
			Double right = tokenizer.nval;
			if (tokenizer.nextToken() != TokenTypes.LIST_CLOSER) {
				throw new ParseError("Cannot handle list expression", tokenizer);				
			}	
			long iLeft = left.longValue();
			if (left != iLeft) {
				throw new ParseError("Cannot use doubles in range", tokenizer);								
			}			
			long iRight = -right.longValue();
			if (right != -iRight) {
				throw new ParseError("Cannot use doubles in range", tokenizer);								
			}
			return new RangeExpression(iLeft, iRight);
		} else {
			throw new ParseError("Cannot handle list expression", tokenizer);
		}
	}

	private Definition parseDefinition(Workflow workflow, StreamTokenizer tokenizer) throws ParseError, IOException {
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
			if (Tokens.JOB.equals(tokenizer.sval)) {
				return parseJobDefinition(workflow, tokenizer);
			} else 
			if (Tokens.TRIGGER.equals(tokenizer.sval)) {
				return null;
			}
		}
		throw new ParseError("Unexpected end of file", tokenizer);
	}

	private Definition parseJobDefinition(Workflow workflow, StreamTokenizer tokenizer) throws IOException, ParseError {
		Job job = new Job(parseDefinitionName(tokenizer));
		job.setInputs(parseParameters(tokenizer));
		job.setConfig(parseJobConfig(workflow, tokenizer));
		if (!(tokenizer.nextToken() == TokenTypes.BLOCK_OPENER)) {
			throw new ParseError("Incorrect formatting: could not find opening brace", tokenizer);
		}		
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
			if (TokenTypes.BLOCK_CLOSER ==tokenizer.ttype) {
				return job;
			} else if (Tokens.RETURN.equals(tokenizer.sval)) {
				job.setOutputs(parseOutputParameters(workflow, tokenizer));
			} else {
				job.addTask(parseTask(workflow, tokenizer, TokenTypes.TERMINATOR));
				// Consume terminator
				tokenizer.nextToken();
			}
		}
		throw new ParseError("Unexpected end of file", tokenizer);
	}

	private List<Assignment> parseJobConfig(Workflow workflow, StreamTokenizer tokenizer) throws IOException, ParseError {
		if (tokenizer.nextToken() != TokenTypes.CONFIG_OPENER) {
			tokenizer.pushBack();
			return Collections.emptyList();
		}
		tokenizer.nextToken();
		// Config is present, so consume , separated assignments
		List<Assignment> assignments = new ArrayList<Assignment>();
		while (tokenizer.ttype != TokenTypes.BLOCK_OPENER) {
			Task task = parseTask(workflow, tokenizer, TokenTypes.PARAM_SEPARATOR, TokenTypes.BLOCK_OPENER);
			if (! (task instanceof Assignment) ) {
				throw new ParseError("Unexpected expression in method configuration", tokenizer);
			}
			assignments.add((Assignment) task);
		}
		return assignments;
	}

	private List<Expression> parseOutputParameters(Workflow workflow, StreamTokenizer tokenizer) throws IOException, ParseError {
		List<Expression> params = new ArrayList<Expression>();
		while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
			if (TokenTypes.TERMINATOR ==tokenizer.ttype) {
				return params;
			} else {
				params.add(parseExpression(workflow, tokenizer, TokenTypes.PARAM_SEPARATOR, TokenTypes.TERMINATOR));
			}
			tokenizer.nextToken();
		}
		throw new ParseError("Unexpected end of file", tokenizer);
	}

	private List<Expression> parseParameterValues(Workflow workflow, StreamTokenizer tokenizer) throws IOException, ParseError {
		List<Expression> params = new ArrayList<Expression>();
		if (tokenizer.nextToken() == TokenTypes.PARAMS_CLOSER) {
				return params;
		} else {
			tokenizer.pushBack();
		}
		while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
			if (TokenTypes.PARAMS_CLOSER ==tokenizer.ttype) {
				return params;
			} else {
				params.add(parseExpression(workflow, tokenizer, TokenTypes.PARAM_SEPARATOR, TokenTypes.PARAMS_CLOSER));
			}
			tokenizer.nextToken();
		}
		throw new ParseError("Unexpected end of file", tokenizer);
	}

	private List<String> parseParameters(StreamTokenizer tokenizer) throws IOException, ParseError {
		if (!(tokenizer.nextToken() == TokenTypes.PARAMS_OPENER)) {
			throw new ParseError("Incorrect formatting: could not find opening brace", tokenizer);
		}	
		List<String> params = new ArrayList<String>();
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
			if (TokenTypes.PARAMS_CLOSER ==tokenizer.ttype) {
				return params;
			} else if (tokenizer.ttype == TokenTypes.PARAM_SEPARATOR) {
				// Do nothing
			} else {
				params.add(tokenizer.sval);
			}
		}
		throw new ParseError("Unexpected end of file", tokenizer);
	}

	private String parseDefinitionName(StreamTokenizer tokenizer) throws IOException, ParseError {
		if (tokenizer.nextToken() != StreamTokenizer.TT_WORD) {
			throw new ParseError("Bad method name", tokenizer);
		}
		return tokenizer.sval;
	}


}
