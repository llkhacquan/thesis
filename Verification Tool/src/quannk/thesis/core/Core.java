package quannk.thesis.core;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;

import quannk.thesis.constraint.CNFClause;
import quannk.thesis.constraint.PathConstraint;
import quannk.thesis.convert.InfixToPrefix;
import quannk.thesis.convert.ParseException;
import quannk.thesis.core.TestMethod.Parameter;
import quannk.thesis.core.Z3Output.Declare;
import quannk.thesis.core.javaparser.JavaParser;

public class Core {
	public Path workingDirPath;
	public File inputFile;
	public JavaCompiler compiler;
	public JPFOutput jpfOutput;
	public TestMethod method;
	public Z3Output z3Output;

	public Core() throws Exception {
		getSystemCompiler();
		prepareWorkingFolder();
	}

	public static void main(String... args) {
		try {
			Core core = new Core();
			core.copyResources();
			core.loadInputJavaFile(new File("test/SystemOnTest.java"));
			core.createTestSystem();
			core.compile();
			core.runJPF();

			// Testing
			Logger.outln("===================General check==================");
			core.checkErrors();

			Logger.outln("==================Conditional check===============");
			core.checkErrors("(a > 1) && ( b > a)");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Vector<String> getRestrictions() {
		Vector<String> result = new Vector<String>();
		result.add("The file must be encoded in UTF-8 without BOM");
		result.add("The file must be name as SystemOnTest.java and there must be one and only one public class named SystemOnTest");
		result.add("The SystemOnTest object must be creatable by new SystemOnTest()");
		result.add("The SystemOnTest class must have a public method named testMethod");
		result.add("The testMethod must have only int or double typed parameters.");
		result.add("The testMethod can return int, double or none (void) only");
		result.add("The SystemOnTest.java can be compiled by command: javac SystemOnTest.java");
		return result;
	}

	public void checkErrors() throws IOException {
		{
			Logger.outlnInDevMode("TESTING: Error check constraint: if this is SAT, there should be error(s)");
			CNFClause errorCheckConstraint = jpfOutput
					.getErrorCheckConstraint();
			Logger.outlnInDevMode(errorCheckConstraint.getUserFriendlyString()
					+ "\n");

			StringBuilder sb = new StringBuilder(
					"(set-option :timeout 10000)\n");
			sb.append(errorCheckConstraint.getSMTDeclare() + "\n");
			sb.append(errorCheckConstraint.getSMTAsserts() + "\n");
			sb.append("(check-sat)\n");
			sb.append("(get-model)\n");

			Logger.outlnInDevMode("SMT problems:\n" + sb);
			String filename = workingDirPath.toString() + "\\smtLib.txt";
			saveFile(filename, sb.toString());
			Vector<String> z3result = Z3Output.runZ3(filename);
			z3Output = Z3Output.z3OutputProcess(z3result);
			if (z3Output.s != null)
				switch (z3Output.s) {
				case SATISFIABLE:
					Logger.outln("Error may orcur with these input:");
					Logger.outln(getStringZ3Model());
					break;
				case UNSATISFIABLE:
					Logger.outln("Does not find any error!");
					break;
				case UNKNOWN:
					Logger.outln("Sorry, we does not know if there is any errors here.");
					break;
				default:
					Logger.outln("Z3 Error run");
					for (String s : z3result) {
						Logger.outlnInDevMode(s);
					}
				}
			else {
				Logger.outln("Z3 Error run");
				for (String s : z3result) {
					Logger.outlnInDevMode(s);
				}
			}
		}
	}

	public void checkErrors(String condition) throws IOException {
		String preFix = null;
		try {
			preFix = InfixToPrefix.parse(condition);
		} catch (ParseException e) {
			Logger.outln("Cannot understand your condition.");
			Logger.outlnInDevMode(e.getMessage());
			return;
		}

		preFix = preFix.replaceAll("&&", "and").replaceAll("\\|\\|", "or")
				.replace("==", "=").replace("!", "not");

		StringBuilder sb = new StringBuilder("(set-option :timeout 10000)\n");
		sb.append(jpfOutput.getDeclares() + "\n");
		for (PathConstraint p : jpfOutput.pathConstraints) {
			sb.append(p.getSMTAsserts() + "\n");
		}
		sb.append(jpfOutput.getPreConstraintOfCode().getSMTAsserts() + "\n");
		sb.append("(assert (" + preFix + "))\n");
		sb.append("(check-sat)\n");
		sb.append("(get-model)\n");

		Logger.outlnInDevMode("SMT problems:\n" + sb);
		String filename = workingDirPath.toString() + "\\smtLib.txt";
		saveFile(filename, sb.toString());
		Vector<String> z3result = Z3Output.runZ3(filename);
		z3Output = Z3Output.z3OutputProcess(z3result);
		if (z3Output.s != null)
			switch (z3Output.s) {
			case SATISFIABLE:
				Logger.outln("These input would make the condition true.");
				Logger.outln(getStringZ3Model());
				break;
			case UNSATISFIABLE:
				Logger.outln("Does not exist an input would make the condition true.");
				break;
			case UNKNOWN:
				Logger.outln("Sorry, we dont know if there exists any input would make the condition true.");
				break;
			default:
				Logger.outln("Z3 Error run");
				for (String s : z3result) {
					Logger.outlnInDevMode(s);
				}
			}
		else {
			Logger.outln("Z3 Error run");
			for (String s : z3result) {
				Logger.outlnInDevMode(s);
			}
		}
	}

	public void getSystemCompiler() {
		Logger.outlnInDevMode("GET SYSTEM JAVA COMPILER");
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler != null) {
			Logger.outlnInDevMode("\tGet system java compiler successfully");
			return;
		}
		Logger.outlnInDevMode("\tGet system java compiler failed. Try another ways...");

		String oldJavaHome = System.getProperty("java.home");
		String newJavaHome = System.getenv("JAVA_HOME");

		if (newJavaHome == null) {
			Logger.messageBox("Please set system variable JAVA_HOME to jdk location.\nFor example: C:\\Program Files\\Java\\jdk1.8.0_40");
			return;
		}

		Logger.outlnInDevMode("Current java.home: " + oldJavaHome);
		Logger.outlnInDevMode("Current JAVA_HOME: " + newJavaHome);
		System.setProperty("java.home", newJavaHome + "\\jre");

		Logger.outlnInDevMode("Current java.home: "
				+ System.getProperty("java.home"));
		Logger.outlnInDevMode("java.home is set to " + newJavaHome);
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			Logger.messageBox("JDK not found, please make sure JAVA_HOME is set to jdk location (eg:C:\\Program Files\\Java\\jdk1.8.0_40");
			return;
		} else {
			Logger.outlnInDevMode("Java compiler is loaded successfuly");
		}
		System.setProperty("java.home", oldJavaHome);
		Logger.outlnInDevMode("java.home is set back to " + oldJavaHome);
	}

	public void prepareWorkingFolder() throws Exception {
		if (!Config.runFromJarFile()) {
			Logger.outlnInDevMode("Run from Eclipse, use Desktop\\ThesisTemp as working folder");
			File folder = new File(System.getProperty("user.home")
					+ "\\Desktop\\ThesisTemp");
			if (!folder.exists())
				folder.mkdirs();
			workingDirPath = folder.toPath();

		} else {
			Logger.outlnInDevMode("Run outside of Eclipse, create new temporary directory.");
			workingDirPath = Files.createTempDirectory("VerificationTool_");
			if (!workingDirPath.toFile().exists()) {
				Logger.messageBox("Cannot create temp directory");
				return;
			} else {
				Logger.outlnInDevMode("Created temp dir: "
						+ workingDirPath.toString());
				Logger.outlnInDevMode("This folder will be deleted automatically");
			}
			workingDirPath.toFile().deleteOnExit();
		}
	}

	public File getFileResource(String pathFromResource) {
		if (Config.runFromJarFile()) {
			return new File("resources\\" + pathFromResource);
		} else {
			return new File("resources\\" + pathFromResource);
		}
	}

	public void copyResources() {
		Logger.outlnInDevMode("INIT RESOURCES");
		File resourcesFolder = new File(workingDirPath.toString()
				+ "\\resources");
		if (resourcesFolder.exists()) {
			File fs[] = resourcesFolder.listFiles();
			for (File f : fs) {
				if (f.getName().compareTo("jpf.jar") == 0)
					continue;
				if (f.getName().compareTo("jpf-symbc-classes.jar") == 0)
					continue;
				f.delete();
			}
			try {
				String[] filesList = new String[] { "jpf.jar",
						"jpf-symbc-classes.jar", "TesterAdapter.java",
						"UserTest.java" };
				for (String s : filesList) {
					File f = new File(workingDirPath.toString()
							+ "\\resources\\" + s);
					if (f.exists()) {
						f.delete();
					}
					FileUtils.copyFile(getFileResource(s), f);
				}
			} catch (IOException e) {
				Logger.outln("ERROR when copy resources.");
				Logger.outlnInDevMode("ERROR:\n" + e.getMessage());
				e.printStackTrace();
				return;
			}
			return;
		} else {
			Logger.outlnInDevMode("resources do not exist. Copying...");
			try {
				FileUtils.copyDirectoryToDirectory(new File("resources"),
						workingDirPath.toFile());
			} catch (IOException e) {
				Logger.outln("Copy error:\n" + e.getStackTrace().toString());
			}
		}
	}

	public void runJPF() {
		String jpfOutputString = runJPFSymbc();
		Logger.outlnInDevMode(jpfOutputString);
		Logger.outlnInDevMode("Processing output...");
		this.jpfOutput = new JPFOutput(jpfOutputString);
		Logger.outlnInDevMode("Processing output: done");
	}

	private String runJPFSymbc() {
		String fileJPF = workingDirPath + "\\resources\\Test.jpf";
		Logger.outlnInDevMode("Running Symbc on " + fileJPF);
		// Create a stream to hold the output
		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(newOut);
		// IMPORTANT: Save the old System.out!
		PrintStream oldOut = System.out;
		// Tell Java to use your special stream
		System.setOut(ps);

		gov.nasa.jpf.JPF.main(new String[] { fileJPF });

		// Put things back
		System.out.flush();
		System.setOut(oldOut);
		Logger.outlnInDevMode(jpfOutput);
		Logger.outlnInDevMode("Running Symbc: DONE");
		return newOut.toString();
	}

	public String getStringZ3Model() {
		StringBuilder sb = new StringBuilder();
		for (Declare d : z3Output.declares) {
			for (Parameter p : method.parameters)
				if (p.name.compareTo(d.name) == 0)
					sb.append(d + "\n");

		}
		return sb.toString();
	}

	public static String getTestableSourceCode(String originalSourceCode) {
		return originalSourceCode;
	}

	public boolean compile() throws IOException {
		Logger.outlnInDevMode("Compiling...");
		String args[] = new String[5];
		String curDir = workingDirPath.toString() + "\\resources\\";
		args[0] = "-cp";
		args[1] = curDir + "jpf.jar;" + curDir + "jpf-symbc-classes.jar";
		args[2] = curDir + "SystemOnTest.java";
		args[3] = curDir + "TesterAdapter.java";
		args[4] = curDir + "UserTest.java";
		int result = compiler.run(null, null, null, args);
		if (result == 0) {
			Logger.outlnInDevMode("Compiled successfully");
		} else {
			Logger.outlnInDevMode("Compiled failed");
		}
		return result == 0;
	}

	public boolean createTestSystem() {
		Logger.outlnInDevMode("Creating Test System...");

		/* COPY AND MODIFY INPUT FILE */
		try {
			String source = readFile(inputFile);
			Logger.outln("content of input file");
			source = JavaParser.parse(source);
			Vector<String> sourceLines = new Vector<String>();
			sourceLines.addAll(Arrays.asList(source.split("\n")));
			for (int index = 0; index < sourceLines.size(); index++) {
				if (sourceLines.elementAt(index).contains("/*CHECK_OUT*/")) {
					String checkOut = "";
					for (Parameter para : method.parameters) {
						switch (para.type) {
						case INT:
							checkOut += "checkOutInteger";
							break;
						case DOUBLE:
							checkOut += "checkOutReal";
							break;
						case BOOLEAN:
							checkOut += "checkOutBoolean";
							break;
						default:
							assert (false);
						}
						checkOut += "(" + para.name + ", \"" + para.name
								+ "2\");";
					}
					sourceLines.set(index, sourceLines.elementAt(index)
							.replace("/*CHECK_OUT*/", checkOut));
				}
			}

			{
				StringBuilder sb = new StringBuilder();
				for (String s : sourceLines)
					sb.append(s + "\n");
				source = sb.toString();
			}

			saveFile(workingDirPath.toString()
					+ "\\resources\\SystemOnTest.java", source);
		} catch (IOException | quannk.thesis.core.javaparser.ParseException e) {
			Logger.outln("Error when copy and modify input file...");
			Logger.outlnInDevMode("ERROR:\n" + e.getStackTrace().toString());
			e.printStackTrace();
			return false;
		}

		/* Create Test.jpf */
		{
			String[] testJPFContent = readFile(getFileResource("Test.jpf"))
					.split("\n");
			StringBuilder test_jpf = new StringBuilder();

			for (String line : testJPFContent) {
				if (line.contains("target="))
					test_jpf.append("target=UserTest\n");
				else if (line.contains("classpath="))
					test_jpf.append("classpath="
							+ workingDirPath.toString().replace("\\", "/")
							+ "/resources\n");
				else if (line.contains("sourcepath="))
					test_jpf.append("sourcepath="
							+ workingDirPath.toString().replace("\\", "/")
							+ "/resources\n");
				else
					test_jpf.append(line.trim() + "\n");
			}
			try {
				saveFile(workingDirPath.toString() + "\\resources\\Test.jpf",
						test_jpf.toString());
			} catch (IOException e) {
				Logger.outln("ERROR:\n" + e.getStackTrace());
				e.printStackTrace();
				return false;
			}
		}

		// Modify UserTest.java
		{
			try {
				// read and parse old UserTest.java
				Scanner sc = new Scanner(getFileResource("UserTest.java"));
				Vector<String> lines = new Vector<String>();
				while (sc.hasNextLine()) {
					lines.add(sc.nextLine());
				}
				sc.close();

				int a = -1, b = -1, c = -1, d = -1;
				for (int i = 0; i < lines.size(); i++) {
					String line = lines.elementAt(i).trim();
					if (line.contains("// CHECK_IN"))
						a = i;
					else if (line.contains("// RUN_SYSTEM_TEST"))
						b = i;
					else if (line.contains("// CHECK_OUT"))
						c = i;
					else if (line.contains("// END_USER_CALL_METHOD"))
						d = i;
				}
				if (!(0 < a && a < b && b < c && c < d)) {
					Logger.messageBox("Error when create UserTest.java: " + a
							+ " " + b + " " + c + " " + d);
					return false;
				}
				{ // modify CHECK_IN
					for (int i = 0; i < b - a - 1; i++) {
						lines.remove(a + 1);
					}
					for (Parameter para : method.parameters) {
						String newLine = "    " + para.type + " " + para.name
								+ " = ";
						switch (para.type) {
						case INT:
							newLine += "checkInInteger";
							break;
						case DOUBLE:
							newLine += "checkInReal";
							break;
						case BOOLEAN:
							newLine += "checkInBoolean";
							break;
						default:
							assert (false);
						}
						newLine += "(\"" + para.name + "\");";
						lines.insertElementAt(newLine, a + 1);
						int delta = method.parameters.length - (b - a - 1);
						c += delta;
						d += delta;
						b += delta;
					}
				} // end CHECK_IN

				{ // start RUN_SYSTEM_TEST
					if (method.returnType != TestMethod.Type.VOID)
						for (int iLine = b; iLine < c; iLine++) {
							if (lines.elementAt(iLine).trim()
									.contains("result")) {
								String newLine = "    "
										+ method.returnType.toString()
										+ " result;";
								lines.set(iLine, newLine);
								break;
							}
						}

					for (int iLine = b; iLine < c; iLine++) {
						if (lines.elementAt(iLine).trim()
								.contains("system.testMethod")) {
							String newLine;
							if (method.returnType.toString().compareTo("void") != 0)
								newLine = "    result"
										+ " = system.testMethod("
										+ method.parameters[0].name;
							else
								newLine = "    system.testMethod("
										+ method.parameters[0].name;
							for (int i = 1; i < method.parameters.length; i++) {
								newLine += ", " + method.parameters[i].name;
							}
							newLine += ");";
							lines.set(iLine, newLine);
							break;
						}
					}
				} // end RUN_SYSTEM_TEST

				{ // modify CHECK_OUT
					for (int i = 0; i < d - c - 1; i++) {
						lines.remove(c + 1);
					}

					if (method.returnType.toString().compareTo("void") != 0) {
						String newLine = "    ";
						switch (method.returnType) {
						case INT:
							newLine += "checkOutInteger";
							break;
						case DOUBLE:
							newLine += "checkOutReal";
							break;
						case BOOLEAN:
							newLine += "checkOutBoolean";
							break;
						default:
							assert (false);
						}
						newLine += "(result, \"result\");";
						lines.insertElementAt(newLine, c + 1);
					}
				} // end CHECK_OUT

				// write to file
				StringBuilder sb = new StringBuilder();
				for (String line : lines) {
					sb.append(line + "\n");
				}
				saveFile(workingDirPath + "\\resources\\UserTest.java",
						sb.toString());
			} catch (IOException e) {
				Logger.outln("ERROR:\n" + e.getStackTrace().toString());
				e.printStackTrace();
				return false;
			}
		}
		Logger.outlnInDevMode("Test System is created.");
		return true;
	}

	public static String readFile(File inputFile) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(inputFile.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (encoded != null)
			return new String(encoded);
		else
			return null;
	}

	public static void saveFile(String path, Object[] objects)
			throws IOException {
		File fileDir = new File(path);
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileDir), "UTF8"));
		for (Object s : objects) {
			out.append(s + "\n");
		}
		out.flush();
		out.close();
	}

	public static void saveFile(String path, String content) throws IOException {
		File fileDir = new File(path);
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileDir), "UTF8"));
		out.append(content);
		out.flush();
		out.close();
	}

	public void loadInputJavaFile(File inputFile) {
		Logger.outlnInDevMode("\n===========CHECK INPUT JAVA FILE===========\n");
		this.inputFile = inputFile;
		try {
			FileUtils.copyFile(inputFile, new File(workingDirPath.toString()
					+ "\\resources\\SystemOnTest.java"));
		} catch (IOException e1) {
			Logger.outln("ERROR:\n" + e1.getStackTrace().toString());
			e1.printStackTrace();
			return;
		}

		{ // Check file name
			if (inputFile.getName().compareTo("SystemOnTest.java") != 0) {
				Logger.outln("Input file must be named as 'SystemOnTest.java'");
				return;
			}
		}

		{ // check compilable property
			File desFile = new File(workingDirPath.toString()
					+ "\\SystemOnTest.java");
			try {
				FileUtils.copyFile(inputFile, desFile);
			} catch (IOException e) {
				Logger.outln(e.getStackTrace().toString());
				return;
			}

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int result = compiler.run(null, os, os,
					new String[] { desFile.getPath() });
			if (result != 0) {
				Logger.outln("Cannot compile the input source file");
				Logger.outlnInDevMode(new String(os.toByteArray()));
				return;
			} else {
				desFile.delete();
				new File(workingDirPath.toString() + "\\SystemOnTest.class")
						.delete();
			}
		}

		method = null;
		{
			List<String> lines = null;
			try {
				lines = Files.readAllLines(inputFile.toPath(),
						Charset.forName("UTF-8"));
			} catch (IOException e) {
				Logger.outlnInDevMode(e.getStackTrace());
			}
			for (String line : lines) {
				if (line.contains("testMethod") && line.contains("(")
						&& line.contains(")")) {
					method = new TestMethod(line);
					break;
				}
			}
		}

		if (method == null) {
			Logger.outln("Cannot parse testMethod...");
			return;
		}

		for (Parameter p : method.parameters) {
			if (p.name.endsWith("2")) {
				Logger.outln("Parameter should not ends with '2' : " + p.type
						+ " " + p.name);
				method = null;
				return;
			} else if (p.name.compareTo("result") == 0) {
				Logger.outln("Parameter should not be 'result' : " + p.type
						+ " " + p.name);
				method = null;
				return;
			}
		}

		// pass all test
		Logger.outlnInDevMode("Check input file: successful");
	}
}
