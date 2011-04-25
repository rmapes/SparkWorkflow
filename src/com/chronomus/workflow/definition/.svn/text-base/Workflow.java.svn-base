package com.chronomus.workflow.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.chronomus.workflow.compiler.CompilerError;
import com.chronomus.workflow.execution.Assignment;
import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.JobQueueTrigger;
import com.chronomus.workflow.execution.JobService;
import com.chronomus.workflow.execution.MethodCall;
import com.chronomus.workflow.execution.ParallelMethod;
import com.chronomus.workflow.execution.ProxyService;
import com.chronomus.workflow.execution.RunCommandMethod;
import com.chronomus.workflow.execution.Service;
import com.chronomus.workflow.execution.SplitMethod;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.execution.Trigger;
import com.chronomus.workflow.execution.VariableStore;
import com.chronomus.workflow.execution.expressions.Expression;
import com.chronomus.workflow.execution.expressions.ListPrimitive;
import com.chronomus.workflow.execution.expressions.ParallelVariable;
import com.chronomus.workflow.execution.expressions.Primitive;
import com.chronomus.workflow.execution.expressions.Variable;
import com.chronomus.workflow.jmx.JmxServer;
import com.chronomus.workflow.persistence.ServiceDbAccessor;

public class Workflow implements WorkflowMBean {

	private Map<String, Job> jobDefinitions = new HashMap<String, Job>();

	public Map<String, Job> getJobs() {
		return Collections.unmodifiableMap(jobDefinitions);
	}

	private Map<String, Service> services = new HashMap<String, Service>();
	private boolean compiled;
	private ServiceDbAccessor dbAccessor;
	private List<Trigger> triggers = new ArrayList<Trigger>();

	public Workflow(ServiceDbAccessor dbAccessor) {
		super();
		this.dbAccessor = dbAccessor;
	}

	public void addDefinition(Definition definition) {
		if (definition instanceof Job) {
			jobDefinitions.put(((Job) definition).getName(), (Job) definition);
		}
	}

	public void addTask(Task execution) {
		// TODO Auto-generated method stub
		
	}
	
	public void addService(Service service) {
		services.put(service.getName(), service);
	}

	public Service getService(String name) {
		return services.get(name);
	}

	private void addTrigger(Trigger trigger) {
		triggers.add(trigger);
	}

	public List<Trigger> getTriggers() {
		return triggers ;
	}

	// Compiler
	public void compile(JmxServer jmxServer) throws CompilerError {
		if (compiled) {
			throw new CompilerError("Workflow already compiled");
		}
		this.addService(new RunCommandMethod());
		this.addService(new SplitMethod());
		// TODO: define base context
		Properties baseContext = new Properties();
		// Add JMX trigger
		createWorkflowTrigger(jmxServer, "workflowRunner");
		// Populate config
		
		// Create services and job queues
		for (Job job : this.getJobs().values()) {
			String serviceName = job.getName();
			// Check whether the job is a standard job or a queue based job
			int queueConsumers = getQueueConsumers(job, new VariableStore(baseContext));
			if (queueConsumers > 0) {
				// Create proxy job
				ProxyService proxy = createProxyServiceForJob(baseContext, job, serviceName);
				for (int i = 0; i < queueConsumers; i++) {
					createQueueConsumer(baseContext, job, serviceName + "_service" + (i + 1), proxy);
				}
			} else {
				createServiceForJob(baseContext, job, serviceName);
			}
		}
		compiled = true;
	}

	private void createWorkflowTrigger(JmxServer server, String serviceName) {
		server.register(this, serviceName);
	}

	private int getQueueConsumers(Job job, VariableStore context) throws CompilerError {
		int consumers = 0;
		for (Expression expr : job.getConfigVariables().values()) {
			if (expr instanceof ParallelVariable) {
				if (consumers==0) {
					consumers = 1;
				}
				Primitive parallelValues;
				try {
					parallelValues = ((ParallelVariable)expr).getExpression().evaluate(context);
				} catch (ExecutionException e) {
					throw new CompilerError(e);
				}
				consumers *= (parallelValues instanceof ListPrimitive) ? ((ListPrimitive) parallelValues).expressions().size() : 1;
			}
		}
		return consumers;
	}

	private void createQueueConsumer(Properties baseContext, Job job,
			String serviceName, ProxyService proxy) throws CompilerError {
		Service service = createServiceForJob(baseContext, job, serviceName);
		addTrigger(new JobQueueTrigger(proxy.getOutputQueue(), proxy.getReturnQueue(), service));
	}

	private ProxyService createProxyServiceForJob(Properties baseContext,
			Job job, String serviceName) throws CompilerError {
		ProxyService service = new ProxyService(serviceName, baseContext, dbAccessor);
		populateService(baseContext, job, serviceName, service);
		addTrigger(service.getReturnQueueTrigger());
		return service;
	}

	private JobService createServiceForJob(Properties baseContext, Job job,
			String serviceName) throws CompilerError {
		JobService service = new JobService(serviceName, baseContext, dbAccessor);
		populateService(baseContext, job, serviceName, service);
		return service;
	}

	private void populateService(Properties baseContext, Job job,
			String serviceName, JobService service) throws CompilerError {
		this.addService(service);
		job.setService(service);
		service.setInputs(job.getInputs());
		for (Task rawExecution : job.getTasks()) {
			for (Task execution : expand(rawExecution)) {
				if (execution instanceof MethodCall) {
					MethodCall methodCall = (MethodCall) execution;
					String jobName = methodCall.getName();
					if (this.getJobs().containsKey(jobName) || jobName.equals("parallel")) {
						// This is a nested Service, so make into a linear call
						serviceName = serviceName+"_"+jobName;
						methodCall = new MethodCall(methodCall, serviceName);
						service.addTask(methodCall);
						if (jobName.equals("parallel")) {
							service = createParallelService(baseContext, serviceName);
						} else {
							service = createServiceForJob(baseContext, this.getJobs().get(jobName), serviceName);
						}
						continue;
					}
				}
				service.addTask((Task)execution);
			}
		}
		service.setOutput(job.getOutputs());
	}
	
	private JobService createParallelService(Properties baseContext, 
			String serviceName) throws CompilerError {
		JobService service = new ParallelMethod(serviceName, baseContext, dbAccessor);
		this.addService(service);
		return service;
	}

	private List<Task> expand(Task rawExecution) throws CompilerError {
		List<Task> tasks = new ArrayList<Task>();
		if (rawExecution instanceof Assignment) {
			Assignment assignment = (Assignment) rawExecution;
			Expression expr = assignment.getRvalue();
			if (expr instanceof MethodCall) { //TODO: account for complex expressions
				MethodCall methodCall = (MethodCall) expr;
				expandAndAddMethodCall(methodCall, tasks);
				Expression output = methodCall.getOutput();
				if (output == null) {
					throw new CompilerError(String.format("Method %s has no return value in a assignment to %s", methodCall.getName(), assignment.getName()));
				}
				tasks.add(new Assignment(assignment.getName(), output));
			} else {
				tasks.add(assignment);
			}
		} else if (rawExecution instanceof MethodCall){
			expandAndAddMethodCall(rawExecution, tasks);	
		} else {
			tasks.add(rawExecution);
		}
		return tasks;
	}

	private void expandAndAddMethodCall(Task rawExecution, List<Task> tasks) {
		MethodCall methodCall = (MethodCall) rawExecution;
		List<Expression> newParameters = new ArrayList<Expression>();
		for (Expression parameter : methodCall.getParameters()) {
			if (parameter instanceof ParallelVariable) { //TODO: account for complex expressions
				MethodCall parallelCall = new MethodCall("parallel", methodCall.getWorkflow());
				List<Expression> parallelParameters = new ArrayList<Expression>();
				parallelParameters.add(((ParallelVariable) parameter).getExpression());
				parallelCall.setParameters(parallelParameters );
				tasks.add(parallelCall);
				newParameters.add(new Variable(ParallelMethod.OUTPUT));
			} else {
				newParameters.add(parameter);
			}
		}
		methodCall.setParameters(newParameters);
		tasks.add(methodCall);
	}

	public Collection<JobService> getJobServices() {
		// Filter list
		List<JobService> retVal = new ArrayList<JobService>();
		for (Service service : services.values()) {
			if (service instanceof JobService) {
				retVal.add((JobService)service);
			}
		}
		return retVal;
	}

	public void launchServices() {
		for (JobService service : getJobServices()) {
			service.launchService();
		}
	}

	public void launchTriggers() {
		for (Trigger trigger : getTriggers()) {
			Thread triggerThread = new Thread(trigger);
			triggerThread.setDaemon(true);
			triggerThread.start();
		}
	}

	@Override
	public void runService(String serviceName) throws ExecutionException {
		System.out.println("Running service " + serviceName);
		services.get(serviceName).run();
	}

}
