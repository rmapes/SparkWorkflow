import junit.framework.TestSuite;

import com.chronomus.workflow.CompileFromCodeTest;
import com.chronomus.workflow.compiler.WorkflowCompilerTest;
import com.chronomus.workflow.execution.JobQueueTest;
import com.chronomus.workflow.execution.ProxyServiceTest;
import com.chronomus.workflow.execution.RunCommandMethodTest;
import com.chronomus.workflow.jmx.JmxTriggerTest;
import com.chronomus.workflow.parsers.SparkWorkflowParserTest;
import com.chronomus.workflow.persistence.PersistenceTest;


public class AllWorkflowFrameworkTests extends TestSuite {

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(SparkWorkflowParserTest.class);
		suite.addTestSuite(WorkflowCompilerTest.class);
		suite.addTestSuite(CompileFromCodeTest.class);
		suite.addTestSuite(PersistenceTest.class);
		suite.addTestSuite(JobQueueTest.class);
		suite.addTestSuite(ProxyServiceTest.class);
		suite.addTestSuite(RunCommandMethodTest.class);
		suite.addTestSuite(JmxTriggerTest.class);
		return suite;
	}

}
