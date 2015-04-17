package vn.edu.vnu.uet.quannk_56.thesis.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.eclipse.ui.commands.ExecutionException;

import vn.edu.vnu.uet.quannk_56.thesis.JPFDemo;

public class Core {

	public Path workingDirPath;
	public String originalCode;
	public String testCode;
	public JavaCompiler compiler;

	/**
	 * 
	 * @param originalSourceCode
	 * @return
	 */
	public static String getTestableSourceCode(String originalSourceCode) {
		return originalSourceCode;
	}

	public void getSystemCompiler() throws Exception {
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler != null)
			return;

		String oldJavaHome = System.getProperty("java.home");
		String newJavaHome = System.getenv("JAVA_HOME");

		if (newJavaHome == null) {
			throw new Exception(
					"Please set system variable JAVA_HOME to jdk location.\nFor example: C:\\Program Files\\Java\\jdk1.8.0_40");
		}

		Logger.log("Current java.home: " + oldJavaHome);
		Logger.log("Current JAVA_HOME: " + newJavaHome);
		System.setProperty("java.home", newJavaHome + "\\jre");

		Logger.log("Current java.home: " + System.getProperty("java.home"));
		Logger.log("java.home is set to " + newJavaHome);
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new Exception(
					"JDK not found, please make sure JAVA_HOME is set to jdk location (eg:C:\\Program Files\\Java\\jdk1.8.0_40");
		} else {
			Logger.log("Java compiler is loaded successfuly");
		}
		System.setProperty("java.home", oldJavaHome);
		Logger.log("java.home is set back to " + oldJavaHome);
	}

	public void runSymbc() {
		JPFDemo.run(workingDirPath + "\\src\\Test.jpf");
	}

	public static String readFile(String path) {
		String content = "";
		try {
			content = new Scanner(new File(path)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	public void saveFile(String path, String content)
			throws FileNotFoundException {
		PrintWriter out = new PrintWriter(path);
		out.print(content);
		out.close();
	}

	public String getContentOfJPFFile() {
		return "";
	}

	public static void main(String... args) {
		try {
			String currentDir = System.getProperty("user.dir");
	        System.out.println("Current dir using System:" +currentDir);
			Core core = new Core();
			core.getSystemCompiler();
			core.makeWorkingFolder();
			core.initResources();
			String originalSourceCode = readFile("resources\\SystemOnTest.java");
			core.setCode(originalSourceCode);
			core.createTestSystem();
			core.compile();
			core.runSymbc();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void compile() throws IOException {
		String args[] = new String[5];
		args[0] = "-cp";
		args[1] = "C:/Users/wind/Desktop/ThesisTemp/src/jpf.jar;"
				+ "C:/Users/wind/Desktop/ThesisTemp/src/jpf-symbc-classes.jar";
		args[2] = "C:/Users/wind/Desktop/ThesisTemp/src/SystemOnTest.java";
		args[3] = "C:/Users/wind/Desktop/ThesisTemp/src/Tester.java";
		args[4] = "C:/Users/wind/Desktop/ThesisTemp/src/UserTest.java";
		compiler.run(null, null, null, args);
	}

	private void createTestSystem() {
		// TODO make everything ready to compile
	}

	private void setCode(String code) throws FileNotFoundException {
		originalCode = code;
		testCode = getTestableSourceCode(code);
		Logger.log("Original code:\n" + code);
		Logger.log("Testable code:\n" + testCode);

		// for now. we assume original code is tesable
		// save the test code
		String testCodeFileName = "SystemOnTest.java";
		String pathToTestCode = (workingDirPath.toString() + "/" + "src/" + testCodeFileName);
		saveFile(pathToTestCode, testCode);
	}

	private void initResources() throws IOException {
		if (Config.isDebug()) {
		} else {
			FileUtils.copyDirectoryToDirectory(new File("resources/classes"),
					workingDirPath.toFile());
			new File(workingDirPath.toString() + "/" + "src").mkdir();
		}
	}

	private void makeWorkingFolder() throws Exception {
		if (Config.isDebug()) {
			Logger.log("Debug mode, use C:\\Users\\wind\\Desktop\\ThesisTemp as working folder");
			workingDirPath = new File("C:\\Users\\wind\\Desktop\\ThesisTemp")
					.toPath();
		} else {
			workingDirPath = Files.createTempDirectory("");
			if (!workingDirPath.toFile().exists()) {
				throw new Exception("Cannot create temp directory");
			} else {
				System.out.println("Created temp dir: " + workingDirPath.toString());
			}
			workingDirPath.toFile().deleteOnExit();
		}
	}
}
