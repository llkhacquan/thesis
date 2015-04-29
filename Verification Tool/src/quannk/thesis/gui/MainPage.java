package quannk.thesis.gui;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import quannk.thesis.core.Core;
import quannk.thesis.core.Logger;

public class MainPage {
  protected Core core;
  protected Shell shell;
  public Console console;

  private Text textPathToOriginalSource;
  private StyledText consoleText;
  private Text checkCondition;
  protected String sourcePath;

  /**
   * Launch the application.
   * 
   * @param args
   */
  public static void main(String[] args) {
    try {
      MainPage window = new MainPage();
      window.open();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Open the window.
   * 
   * @throws Exception
   */
  public void open() throws Exception {
    Display display = Display.getDefault();
    createContents();
    shell.open();
    shell.layout();
    onOpen();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    onDestroy();
  }

  private void onOpen() throws Exception {
    // Setting console
    console = Console.getConsole(consoleText);
    Logger.setGUI(this);

    // Setting core
    core = new Core();

    // print restriction of input java file
    console.writeln("===========================================================================");
    console.writeln("There is some restriction for input java file (for now):");
    console.writeln("  The file must be encoded in UTF-8 without BOM");
    console.writeln("  The file must be name as SystemOnTest.java and there must be one and only one public class named SystemOnTest");
    console.writeln("  The SystemOnTest object must be creatable by new SystemOnTest()");
    console.writeln("  The SystemOnTest class must have a public method named testMethod");
    console.writeln("  The testMethod must have only int or double typed parameters.");
    console.writeln("  The testMethod can return int, double or none (void) only");
    console.writeln("  The SystemOnTest.java can be compiled by command: javac SystemOnTest.java");
  }

  private void onDestroy() {

  }

  public MessageBox createMessageBox(Object content) {
    MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
    messageBox.setText("Info");
    messageBox.setMessage(content.toString());
    messageBox.open();
    return messageBox;
  }

  protected void createContents() {
    shell = new Shell();
    shell.setSize(800, 500);
    shell.setText("SWT Application");

    Group grpSources = new Group(shell, SWT.NONE);
    grpSources.setText("Sources");
    grpSources.setBounds(10, 0, 257, 193);

    textPathToOriginalSource = new Text(grpSources, SWT.BORDER);
    textPathToOriginalSource.setEditable(false);
    textPathToOriginalSource.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDoubleClick(MouseEvent e) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        String[] filterNames = new String[] { "Java Files", "All Files (*)" };
        String[] filterExtensions = new String[] { "*.java", "*" };
        String platform = SWT.getPlatform();
        if (platform.equals("win32")) {
          filterNames = new String[] { "Java Files", "All Files (*.*)" };
          filterExtensions = new String[] { "*.java", "*.*" };
        }
        dialog.setFilterNames(filterNames);
        dialog.setFilterExtensions(filterExtensions);
        dialog.setFileName("");
        sourcePath = dialog.open();
        if (sourcePath != null) {
          core.copyResources();
          core.loadInputJavaFile(new File(sourcePath));
          textPathToOriginalSource.setText(core.inputFile.getName());
        }
      }
    });
    textPathToOriginalSource.setText("Path to java test file");
    textPathToOriginalSource.setToolTipText("Double click to select original source");
    textPathToOriginalSource.setBounds(10, 22, 237, 21);

    Group grpConsole = new Group(shell, SWT.NONE);
    grpConsole.setText("Console");
    grpConsole.setBounds(10, 199, 764, 252);

    TextViewer textViewer = new TextViewer(grpConsole, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
    consoleText = textViewer.getTextWidget();
    consoleText.setEditable(false);
    consoleText.setBounds(10, 21, 744, 221);
    consoleText.addListener(SWT.Modify, new Listener() {
      @Override
      public void handleEvent(Event e) {
        consoleText.setTopIndex(consoleText.getLineCount() - 1);
      }
    });

    Group grpVerificator = new Group(shell, SWT.NONE);
    grpVerificator.setText("Verificator");
    grpVerificator.setBounds(273, 0, 501, 193);

    Button btnStartVerification = new Button(grpVerificator, SWT.NONE);
    btnStartVerification.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          core.copyResources();
          core.loadInputJavaFile(new File(sourcePath));
          if (!core.createTestSystem()) {
            return;
          }
          if (!core.compile())
            return;
          core.runJPF();
          core.checkErrors();
        } catch (IOException e1) {
          console.writeln(e1.getStackTrace());
          e1.printStackTrace();
        }
      }
    });
    btnStartVerification.setBounds(10, 20, 176, 25);
    btnStartVerification.setText("Find Errors Caused By Input");

    checkCondition = new Text(grpVerificator, SWT.BORDER);
    checkCondition.setBounds(10, 84, 481, 21);

    Button btnCheckPosibleOutcome = new Button(grpVerificator, SWT.NONE);
    btnCheckPosibleOutcome.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          core.copyResources();
          core.loadInputJavaFile(new File(sourcePath));
          if (!core.createTestSystem()) {
            return;
          }
          if (!core.compile())
            return;
          core.runJPF();

          String condition = checkCondition.getText();
          core.checkErrors(condition);
        } catch (IOException e1) {
          console.writeln(e1.getStackTrace());
          e1.printStackTrace();
        }
      }
    });
    btnCheckPosibleOutcome.setBounds(207, 20, 284, 25);
    btnCheckPosibleOutcome.setText("Check posible outcome conditions");
  }
}
