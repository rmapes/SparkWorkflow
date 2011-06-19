package com.chronomus.workflow.parsers;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.chronomus.workflow.compiler.CompilerError;
import com.chronomus.workflow.definition.Workflow;
import com.chronomus.workflow.execution.Assignment;
import com.chronomus.workflow.execution.JobService;
import com.chronomus.workflow.execution.Service;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.DateBinaryExpression;
import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.primitives.DatePrimitive;
import com.chronomus.workflow.execution.expressions.primitives.NumberPrimitive;
import com.chronomus.workflow.execution.expressions.primitives.Primitive;
import com.chronomus.workflow.execution.expressions.primitives.StringPrimitive;
import com.chronomus.workflow.execution.expressions.primitives.TimespanPrimitive;
import com.chronomus.workflow.jmx.JmxServer;
import com.chronomus.workflow.persistence.MockServiceDbAccessor;

public class ExpressionsParserTest extends TestCase {

	private MockServiceDbAccessor mockDbAccessor = new MockServiceDbAccessor();
	private JmxServer mockJmxServer = new JmxServer("dummy", 0);

////////////////////////////////////////////////
// Literals
	/**
	 * Test string assignment
	 * @throws Exception
	 */
	public void testSimpleStringVariableAssignment() throws Exception {
		// Parse from code
		String testValue = "testValue";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = \"" + testValue + "\";" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof StringPrimitive);
		assertEquals(testValue, result.evaluate());
	}


	/**
	 * Test integer assignment
	 * @throws Exception
	 */
	public void testSimpleNumberVariableAssignment() throws Exception {
		// Parse from code
		String testValue = "2";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = " + testValue + ";" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals(testValue, result.evaluate());
	}

	/**
	 * Test double assignment
	 * @throws Exception
	 */
	public void testDoubleNumberVariableAssignment() throws Exception {
		// Parse from code
		String testValue = "2.13";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = " + testValue + ";" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals(testValue, result.evaluate());
	}
	
	/**
	 * Test date assignment
	 * @throws Exception
	 */
	public void testDateVariableAssignment() throws Exception {
		// Parse from code
		String testValue = "12 Jun 1993";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = @" + testValue + "@;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof DatePrimitive);
		assertEquals(testValue, result.evaluate());
	}
	
	/**
	 * Test date assignment from today literal
	 * @throws Exception
	 */
	public void testTodaysDateVariableAssignment() throws Exception {
		// Parse from code
		DateFormat df = new SimpleDateFormat("dd MMM yyyy");
		String testValue = df.format(new Date());
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = today;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof DatePrimitive);
		assertEquals(testValue, result.evaluate());
	}
	
	/**
	 * Test date assignment
	 * @throws Exception
	 */
	public void testTimespanAssignment() throws Exception {
		// Parse from code
		String testValue = "1 day";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = day;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof TimespanPrimitive);
		assertEquals(testValue, result.toString());
	}

	/**
	 * Test date assignment
	 * @throws Exception
	 */
	public void testTimespanWithMultiplierAssignment() throws Exception {
		// Parse from code
		String testValue = "1 day";
		String basicWorkflow = 
			"workflow { \n" +
				"def job helloWorld() { \n" +
					"var = 1 * day;\n" + 
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof TimespanPrimitive);
		assertEquals(testValue, result.toString());
	}

/////////////////////////////////////
// Variable assignments

	/**
	 * Test integer assignment
	 * @throws Exception
	 */
	public void testSimpleVariableVariableAssignment() throws Exception {
		// Parse from code
		String testValue = "2";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = " + testValue + ";" + 
					"var2 = var;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression[] exprs = getExpressions(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Expression expr = exprs[0];
		Primitive result = expr.evaluate(context);
		assertEquals(NumberPrimitive.class, result.getClass());
		assertEquals(testValue, result.evaluate());
		context.put("var", result);
		result = exprs[1].evaluate(context);
		assertEquals(NumberPrimitive.class, result.getClass());
		assertEquals(testValue, result.evaluate());
	}
	
/////////////////////////////////////
/// Operators
	
	/**
	 * Test integer addition
	 * @throws Exception
	 */
	public void testSimpleNumberAddition() throws Exception {
		// Parse from code
		String testValue1 = "2";
		String testValue2 = "7";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = " + testValue1 + "+" + testValue2 + ";" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("9", result.evaluate());
	}
	
	/**
	 * Test integer subtraction
	 * @throws Exception
	 */
	public void testSimpleNumberSubtraction() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 5 - 2;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("3", result.evaluate());
	}

	/**
	 * Test fraction addition
	 * @throws Exception
	 */
	public void testFractionalNumberAddition() throws Exception {
		// Parse from code
		String testValue1 = "2.5";
		String testValue2 = "7.5";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = " + testValue1 + "+" + testValue2 + ";" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("10", result.evaluate());
	}

	/**
	 * Test multiplication
	 * @throws Exception
	 */
	public void testSimpleNumberMultiplication() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 5 * 2;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("10", result.evaluate());
	}

	/**
	 * Test division
	 * @throws Exception
	 */
	public void testSimpleNumberDivision() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 15 / 3;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("5", result.evaluate());
	}

////////////////////////////////////
/// Date expressions
	
	/*
	 * Test date manipulation by subtracting 1 day
	 * @throws Exception
	 */
	public void testSimpleDateExpression() throws Exception {
		// Parse from code
		String testValue = "12 Jun 1993";
		String expectedValue = "11 Jun 1993";
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = @" + testValue + "@ - 1 * day;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertEquals(DatePrimitive.class, result.getClass());
		assertEquals(expectedValue, result.evaluate());
	}
	
////////////////////////////////////
//// Complex expressions
	/**
	 * Test integer addition
	 * @throws Exception
	 */
	public void testMultipleNumberAddition() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 2 + 3 + 4;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("9", result.evaluate());
	}
	
	public void testMultipleNumberMultiplication() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 2 * 3 * 4;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("24", result.evaluate());
	}
	
	// Note left to right precedence - no operator precedence
	public void testMixedOperators() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 2 + 3 * 4;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("20", result.evaluate());
	}
	
	// Note left to right precedence - no operator precedence
	public void testMixedOperatorsMultiplicationFirst() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 3 * 4 + 2;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("14", result.evaluate());
	}

	public void testMixedOperatorsLongExpression() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 3 * 4 + 2 / 7 + 2.5 - 3 - 6;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("-4.5", result.evaluate());
	}

	public void testBracketedExpression() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { " +
				"def job helloWorld() { " +
					"var = 3 * (4 + 2) + 3;" + 
				"}" +
			"}";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("21", result.evaluate());
	}

	public void testLHSBracketExpression() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { \n" +
				"def job helloWorld() { \n" +
					"var = (2 + 3) * 2;\n" + 
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("10", result.evaluate());
	}

	public void testRHSBracketExpression() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { \n" +
				"def job helloWorld() { \n" +
					"var = 2 * (2 + 3);\n" + 
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("10", result.evaluate());
	}

	public void testNestedBracketsExpression() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { \n" +
				"def job helloWorld() { \n" +
					"var = 3 * ((3 + (4 / 4) + (1 * 2))) + 3;\n" + 
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("21", result.evaluate());
	}

	public void testNestedBracketsExpression2() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { \n" +
				"def job helloWorld() { \n" +
					"var = 3 * (3 + (4 / 4) + (1 * 2));\n" + 
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("18", result.evaluate());
	}

	public void testSimpleNestedBrackets() throws Exception {
		// Parse from code
		String basicWorkflow = 
			"workflow { \n" +
				"def job helloWorld() { \n" +
					"var = ((3));\n" + 
				"}\n" +
			"}\n";
		Workflow workflow = new SparkWorkflowParser(mockDbAccessor).loadWorkflow(new StringReader(basicWorkflow));

		Expression expr = getExpression(workflow);
		
		// Test run
		VariableStore context = new VariableStore();
		Primitive result = expr.evaluate(context);
		assertTrue(result instanceof NumberPrimitive);
		assertEquals("3", result.evaluate());
	}

/////////////////////////////////////
/// Helper routines
	
	private Expression getExpression(Workflow workflow) throws CompilerError {
		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertTrue(helloWorldService instanceof JobService);
		assertEquals(1, ((JobService) helloWorldService).getTasks().size());
		Task task = ((JobService) helloWorldService).getTasks().iterator().next();
		assertTrue(task instanceof Assignment);
		Expression expr = ((Assignment) task).getRvalue();
		return expr;
	}

	private Expression[] getExpressions(Workflow workflow) throws CompilerError {
		// Now compile and check
		workflow.compile(mockJmxServer);
		assertEquals(1, workflow.getJobServices().size());
		Service helloWorldService = workflow.getJobServices().iterator().next();
		assertEquals("helloWorld", helloWorldService.getName());
		assertTrue(helloWorldService instanceof JobService);
		int numExpressions = ((JobService) helloWorldService).getTasks().size();
		Expression[] exprList = new Expression[numExpressions];
		int i = 0;
		for (Task task : ((JobService) helloWorldService).getTasks()) {
			assertTrue(task instanceof Assignment);
			exprList[i++] = ((Assignment) task).getRvalue();
		}
		return exprList;
	}
}
