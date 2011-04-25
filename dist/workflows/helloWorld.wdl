workflow {

	entryPoint(helloWorld);

	def job helloWorld() {
		runCommand("cmd /c echo Hello");
	}
}
