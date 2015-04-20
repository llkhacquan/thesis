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
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;

import quannk.thesis.constraint.CNFClause;
import quannk.thesis.constraint.PathConstraint;
import quannk.thesis.core.TestMethod.Parameter;

public class Core {
  public Path workingDirPath;
  public File inputFile;
  public JavaCompiler compiler;
  public String jpfOutput;
  public JPFOutputProcesser jpfOutputProcessor;
  public TestMethod method;

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
      core.runJPFSymbc();
      core.processJPFOutput();

      {
        Logger.outlnInDevMode("TESTING: Error check constraint: if this is SAT, there should be error(s)");
        CNFClause errorCheckConstraint = core.jpfOutputProcessor.getErrorCheckConstraint();
        Logger.outlnInDevMode(errorCheckConstraint.getUserFriendlyString() + "\n");
        Logger.outlnInDevMode(errorCheckConstraint.getSMTDeclare());
        Logger.outlnInDevMode(errorCheckConstraint.getSMTAsserts());
      }

      {
        Logger.outlnInDevMode("TESTING: # Paths: " + core.jpfOutputProcessor.pathConstraints.size());
        for (PathConstraint path : core.jpfOutputProcessor.pathConstraints) {
          Logger.outlnInDevMode(path.getUserFriendlyString());
          Logger.outlnInDevMode(path.getSMTAsserts());
          Logger.out();
        }
      }
      {

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void getSystemCompiler() {
    Logger.outln("GET SYSTEM JAVA COMPILER");
    compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler != null) {
      Logger.outln("\tGet system java compiler successfully");
      return;
    }
    Logger.outln("\tGet system java compiler failed. Try another ways...");

    String oldJavaHome = System.getProperty("java.home");
    String newJavaHome = System.getenv("JAVA_HOME");

    if (newJavaHome == null) {
      Logger.messageBox("Please set system variable JAVA_HOME to jdk location.\nFor example: C:\\Program Files\\Java\\jdk1.8.0_40");
      return;
    }

    Logger.outlnInDevMode("Current java.home: " + oldJavaHome);
    Logger.outlnInDevMode("Current JAVA_HOME: " + newJavaHome);
    System.setProperty("java.home", newJavaHome + "\\jre");

    Logger.outlnInDevMode("Current java.home: " + System.getProperty("java.home"));
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
      Logger.outln("Run from Eclipse, use C:\\Users\\wind\\Desktop\\ThesisTemp as working folder");
      workingDirPath = new File("C:\\Users\\wind\\Desktop\\ThesisTemp").toPath();
    } else {
      Logger.outln("Run outside of Eclipse, create new temporary directory.");
      workingDirPath = Files.createTempDirectory("VerificationTool_");
      if (!workingDirPath.toFile().exists()) {
        Logger.messageBox("Cannot create temp directory");
        return;
      } else {
        Logger.outln("Created temp dir: " + workingDirPath.toString());
        Logger.outln("This folder will be deleted automatically");
      }
      workingDirPath.toFile().deleteOnExit();
    }
  }

  public static File getFileResource(String pathFromResource) {
    if (Config.runFromJarFile()) {
      assert (false);
    } else {
      return new File(pathFromResource);
    }
    return null;
  }

  public void copyResources() {
    Logger.outln("INIT RESOURCES");
    if (Config.runFromJarFile()) {
      assert (false);// chua implement
    } else {
      File resourcesFolder = new File(workingDirPath.toString() + "\\resources");
      if (resourcesFolder.exists()) {
        for (File f : resourcesFolder.listFiles()) {
          if (f.getName().compareTo("jpf.jar") == 0)
            continue;
          if (f.getName().compareTo("jpf-symbc-classes.jar") == 0)
            continue;
          f.delete();
        }
        try {
          String[] filesList = new String[] { "jpf.jar", "jpf-symbc-classes.jar", "TesterAdapter.java", "UserTest.java" };
          for (String s : filesList) {
            File f = new File(workingDirPath.toString() + "\\resources\\" + s);
            if (!f.exists())
              FileUtils.copyFile(getFileResource("resources\\" + s), f);
          }
        } catch (IOException e) {
          Logger.outln("ERROR:\n" + e.getStackTrace().toString());
          e.printStackTrace();
          return;
        }
        return;
      } else {
        Logger.outlnInDevMode("resources do not exist. Copying...");
        try {
          FileUtils.copyDirectoryToDirectory(new File("resources"), workingDirPath.toFile());
        } catch (IOException e) {
          Logger.outlnInDevMode("Copy error:\n" + e.getStackTrace().toString());
        }
      }
    }
  }

  public void processJPFOutput() {
    Logger.outln("Processing output...");
    jpfOutputProcessor = new JPFOutputProcesser(jpfOutput);
    Logger.outln("Processing output: done");
  }

  public void runJPFSymbc() {
    String fileJPF = workingDirPath + "\\resources\\Test.jpf";
    Logger.outln("Running Symbc on " + fileJPF);
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
    jpfOutput = newOut.toString();
    Logger.outlnInDevMode(jpfOutput);
    Logger.outln("Running Symbc: DONE");
  }

  public static String getTestableSourceCode(String originalSourceCode) {
    return originalSourceCode;
  }

  public void compile() throws IOException {
    Logger.outln("Compiling...");
    String args[] = new String[5];
    String curDir = workingDirPath.toString() + "\\resources\\";
    args[0] = "-cp";
    args[1] = curDir + "jpf.jar;" + curDir + "jpf-symbc-classes.jar";
    args[2] = curDir + "SystemOnTest.java";
    args[3] = curDir + "TesterAdapter.java";
    args[4] = curDir + "UserTest.java";
    int result = compiler.run(null, null, null, args);
    if (result == 0) {
      Logger.outln("Compiled successfully");
    } else {
      Logger.outln("Compiled failed");
    }
  }

  public void createTestSystem() {
    Logger.outln("Creating Test System...");
    try {
      FileUtils.copyFile(inputFile, new File(workingDirPath.toString() + "\\SystemOnTest.java"));
    } catch (IOException e) {
      Logger.outln("ERROR:\n" + e.getStackTrace().toString());
      e.printStackTrace();
      return;
    }

    // Create Test.jpf
    {
      StringBuilder test_jpf = new StringBuilder();
      test_jpf.append("target=UserTest\n");
      test_jpf.append("classpath=" + workingDirPath.toString().replace("\\", "/") + "/resources\n");
      test_jpf.append("sourcepath=" + workingDirPath.toString().replace("\\", "/") + "/resources\n");
      test_jpf.append("search.multiple_errors=true\nsymbolic.dp=coral\n");
      try {
        saveFile(workingDirPath.toString() + "\\resources\\Test.jpf", test_jpf.toString());
      } catch (IOException e) {
        Logger.outln("ERROR:\n" + e.getStackTrace().toString());
        e.printStackTrace();
        return;
      }
    }

    // Modify UserTest.java
    {
      try {
        // read and parse old UserTest.java
        Scanner sc = new Scanner(new File(workingDirPath + "\\resources\\UserTest.java"));
        Vector<String> lines = new Vector<String>();
        while (sc.hasNextLine()) {
          lines.add(sc.nextLine());
        }
        sc.close();

        int a = -1, b = -1, c = -1, d = -1;
        for (int i = 0; i < lines.size(); i++) {
          if (lines.elementAt(i).trim().contains("// CHECK_IN"))
            a = i;
          else if (lines.elementAt(i).trim().contains("// RUN_SYSTEM_TEST"))
            b = i;
          else if (lines.elementAt(i).trim().contains("// CHECK_OUT"))
            c = i;
          else if (lines.elementAt(i).trim().contains("// END_USER_CALL_METHOD"))
            d = i;
        }
        assert (a > 0 && b > a && c > b && d > c);
        { // modify CHECK_IN
          for (int i = 0; i < b - a - 1; i++) {
            lines.remove(a + 1);
          }
          for (Parameter para : method.parameters) {
            String newLine = "    " + para.type + " " + para.name + " = ";
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
          }
          b += method.parameters.length - (b - a - 1);
          c += method.parameters.length - (b - a - 1);
          d += method.parameters.length - (b - a - 1);
        } // end CHECK_IN

        String nameOfResult = "result";
        {
          // modify RUN_SYSTEM_TEST

          // calculate nameOfResult
          while (true) {
            boolean b1 = false;
            for (Parameter para : method.parameters) {
              if (para.name.compareTo(nameOfResult) == 0) {
                nameOfResult += "_";
                b1 = true;
                break;
              }
            }
            if (!b1)
              break;
          }

          for (int iLine = b; iLine < c; iLine++) {
            if (lines.elementAt(iLine).trim().contains("result")) {
              String newLine = "    " + method.returnType.toString() + " " + nameOfResult + ";";
              lines.set(iLine, newLine);
              break;
            }
          }

          for (int iLine = b; iLine < c; iLine++) {
            if (lines.elementAt(iLine).trim().contains("system.testMethod")) {
              String newLine = "    " + nameOfResult + " = system.testMethod(" + method.parameters[0].name;

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
            lines.remove(c);
          }
          for (Parameter para : method.parameters) {
            String newLine = "    ";
            switch (para.type) {
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
            newLine += "(" + para.name + ", \"_" + para.name + "\");";
            lines.insertElementAt(newLine, c + 1);
          }
          if (method.returnType != TestMethod.Type.VOID) {
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
            newLine += "(" + nameOfResult + ", \"_" + nameOfResult + "\");";
            lines.insertElementAt(newLine, c + 1);
          }
        } // end CHECK_OUT

        // write to file
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
          sb.append(line + "\n");
        }
        saveFile(workingDirPath + "\\resources\\UserTest.java", sb.toString());
      } catch (IOException e) {
        Logger.outln("ERROR:\n" + e.getStackTrace().toString());
        e.printStackTrace();
        return;
      }
    }
    Logger.outln("Test System is created.");
  }

  public static String readFile(File inputFile) throws IOException {
    byte[] encoded = Files.readAllBytes(inputFile.toPath());
    return new String(encoded);
  }

  public void saveFile(String path, String content) throws IOException {
    File fileDir = new File(path);
    Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
    out.append(content);
    out.flush();
    out.close();
  }

  public void loadInputJavaFile(File inputFile) {
    Logger.outln("\n===========CHECK INPUT JAVA FILE===========\n");
    this.inputFile = inputFile;
    try {
      FileUtils.copyFile(inputFile, new File(workingDirPath.toString() + "\\resources\\SystemOnTest.java"));
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
      File desFile = new File(workingDirPath.toString() + "\\SystemOnTest.java");
      try {
        FileUtils.copyFile(inputFile, desFile);
      } catch (IOException e) {
        Logger.outln(e.getStackTrace().toString());
        return;
      }

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      int result = compiler.run(null, os, os, new String[] { desFile.getPath() });
      if (result != 0) {
        Logger.outln("Cannot compile the input source file:");
        Logger.outln(new String(os.toByteArray()));
        return;
      } else {
        desFile.delete();
        new File(workingDirPath.toString() + "\\SystemOnTest.class").delete();
      }
    }

    method = null;
    {
      List<String> lines = null;
      try {
        lines = Files.readAllLines(inputFile.toPath(), Charset.forName("UTF-8"));
      } catch (IOException e) {
        Logger.outln(e.getStackTrace());
      }
      for (String line : lines) {
        if (line.contains("testMethod") && line.contains("(") && line.contains(")")) {
          method = new TestMethod(line);
          break;
        }
      }
    }

    if (method == null) {
      Logger.outln("Cannot parse testMethod...");
      return;
    }

    // pass all test
    Logger.outln("Check input file: successful");
  }
}
