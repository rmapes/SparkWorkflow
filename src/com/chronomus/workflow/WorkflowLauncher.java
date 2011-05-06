package com.chronomus.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.chronomus.workflow.compiler.CompilerError;
import com.chronomus.workflow.definition.Workflow;
import com.chronomus.workflow.jmx.JmxServer;
import com.chronomus.workflow.parsers.ParseException;
import com.chronomus.workflow.parsers.SparkWorkflowParser;
import com.chronomus.workflow.persistence.ServiceDbAccessor;
import com.chronomus.workflow.persistence.ServiceDbAccessorImpl;

public class WorkflowLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean isInteractive = false; // This should come from args

		WorkflowLauncher launcher = new WorkflowLauncher(new ServiceDbAccessorImpl());
		launcher.createServer();
		launcher.loadWorkflows();
		launcher.launchServices();
		launcher.launchTriggers();
		launcher.start();
		
		if (isInteractive ) {
			System.out.println("Workflow Server running. Press return to stop.");
			System.console().readLine();
			launcher.stop();
		}
	}

	private JmxServer server;

	private void createServer() {
		server = new JmxServer("localhost", 1099); //TODO(rmapes): get these params from config
	}

	private List<Workflow> workflows = new ArrayList<Workflow>();
	private ServiceDbAccessor dbAccessor;
	public WorkflowLauncher(ServiceDbAccessor dbAccessor) {
		super();
		this.dbAccessor = dbAccessor;
	}

	private void launchServices() {
		for (Workflow workflow : workflows) {
			workflow.launchServices();
		}
	}

	private void launchTriggers() {
		for (Workflow workflow : workflows) {
			workflow.launchTriggers();
		}
	}

	private void loadWorkflows() {
		String configDirectoryName = "workflows";
		File workflowDir = new File(configDirectoryName);
		if (!workflowDir.exists()) {
			throw new RuntimeException("Could not find workflow configuration: " + configDirectoryName + " does not exist.");
		}
		if (!workflowDir.isDirectory()) {
			throw new RuntimeException("Could not find workflow configuration: " + configDirectoryName + " is not a directory.");
		}
		String[] workflowDefinitionFilenames = workflowDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File directory, String filename) {
				if (filename.endsWith(".wdl")) {
					return true;
				}
				return false;
			}
			
		});
		try {
			loadWorkflows(workflowDir, workflowDefinitionFilenames);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unexpected error: directory listing not found when attempting to load workflow", e);
		}
	}

	private void loadWorkflows(File workflowDir,
			String[] workflowDefinitionFilenames) throws FileNotFoundException {
		for (String filename : workflowDefinitionFilenames) {
			try {
				Workflow workflow = new SparkWorkflowParser(dbAccessor).loadWorkflow(new FileReader(new File(workflowDir, filename)));
				workflow.compile(server);
				workflows.add(workflow);
			} catch (ParseException e) {
				// Report warning but do not stop loading definitions
				// TODO: improve handling here
				e.printStackTrace();
			} catch (CompilerError e) {
				// Report warning but do not stop loading definitions
				// TODO: improve handling here
				e.printStackTrace();
			}
		}
	}

	private void start()  {
		try {
			java.rmi.registry.LocateRegistry.createRegistry(server.getPort());
			server.start();
			// Add the ability to stop system via JMX command
			server.register(new WorkflowMaster(), "master");
		} catch (RemoteException e) {
			System.out.println("Failed to start RMI server.\n" + e.getMessage());
			System.exit(-1);
			
		}
	}

	private void stop()  {
		System.out.println("Shutting down service");
		server.stop();
	}

	// Define master controller, to stop system by JMX
	public interface WorkflowMasterMBean {
		public void shutdown();
	}
	
	private class WorkflowMaster implements WorkflowMasterMBean {
		public void shutdown() {
			stop();
		}
	}
}

