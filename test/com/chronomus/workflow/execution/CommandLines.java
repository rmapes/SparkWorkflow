package com.chronomus.workflow.execution;

public class CommandLines {
	
	private static CommandLineProvider osSpecificDelegate;
	
	static {
		String os = System.getProperty("os.name");
		if (os.indexOf("win") >= 0) {
			osSpecificDelegate = new WindowsCommandLines();
		} else {
			osSpecificDelegate = new LinuxCommandLines();
		}
	}

	public static String outputToScreen() {
		return osSpecificDelegate.outputToScreen();
	}
	
	public static String outputToFile() {
		return osSpecificDelegate.outputToFile();
	}

	public static String helloWorld() {
		return osSpecificDelegate.helloWorld();
	}

	public static String testFileLocation(String filename) {
		return osSpecificDelegate.testFileLocation(filename);
	}
}

interface CommandLineProvider {
	String outputToScreen();
	String outputToFile();
	String helloWorld();
	String testFileLocation(String filename);
}

class WindowsCommandLines implements CommandLineProvider {
	public String outputToScreen() {
		return "windows/outputToScreen.bat";
	}		
	public String outputToFile() {
		return "windows/outputToFile.bat";
	}		
	public String helloWorld() {
		return "windows/helloWorld.bat";
	}
	public String testFileLocation(String filename) {
		return "C:/temp/" + filename;
	}		
}

class LinuxCommandLines implements CommandLineProvider{
	public String outputToScreen() {
		return "./linux/outputToScreen.sh";
	}		
	public String outputToFile() {
		return "./linux/outputToFile.sh";
	}		
	public String helloWorld() {
		return "./linux/helloWorld.sh";
	}		
	public String testFileLocation(String filename) {
		return "/tmp/" + filename;
	}		
}
