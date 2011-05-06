package com.chronomus.workflow.parsers;

import java.io.Reader;

import com.chronomus.workflow.definition.Workflow;
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
 * variable_assignment : variable_name + assignment_operator + expression  // TODO(rmapes): expand this to support tuples: Note(rmapes: 6/5/11) What's the difference between a Tuple and a List?
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

}
