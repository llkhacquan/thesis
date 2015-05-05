package quannk.thesis.gui;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

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

import quannk.thesis.core.Config;
import quannk.thesis.core.Core;
import quannk.thesis.core.Logger;

import org.eclipse.swt.widgets.Label;

public class MainPage {
  protected Core core;
  protected Shell shell;
  public Console console;

  private Text textPathToOriginalSource;
  private StyledText consoleText;
  private Text checkCondition;
  protected String sourcePath;
  private Button btnDevelopmentMode;

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
    Config.DEVELOP_MODE = btnDevelopmentMode.getSelection(); 
    Logger.setGUI(this);

    // Setting core
    core = new Core();

    Vector<String> restriction = Core.getRestrictions();
    if (restriction != null && restriction.size() > 0) {
      // print restriction of input java file
      console.writeln("===========================================================================");
      console.writeln("There is some restriction for input java file (for now):");
      for (String s : restriction) {
        console.writeln("  " + s);
      }
      
      console.writeln("\n===========================================================================\n");
    }
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
    shell.setText("Symbolic Verificaltion Tool");

    Group grpSources = new Group(shell, SWT.NONE);
    grpSources.setText("Sources");
    grpSources.setBounds(10, 10, 257, 54);

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
    textPathToOriginalSource.setText("Double click to browse source code");
    textPathToOriginalSource.setToolTipText("Double click to select original source");
    textPathToOriginalSource.setBounds(10, 22, 237, 21);

    Group grpConsole = new Group(shell, SWT.NONE);
    grpConsole.setText("Console");
    grpConsole.setBounds(10, 138, 764, 313);

    TextViewer textViewer = new TextViewer(grpConsole, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
    consoleText = textViewer.getTextWidget();
    consoleText.setEditable(false);
    consoleText.setBounds(10, 21, 744, 282);
    consoleText.addListener(SWT.Modify, new Listener() {
      @Override
      public void handleEvent(Event e) {
        consoleText.setTopIndex(consoleText.getLineCount() - 1);
      }
    });

    Group grpVerificator = new Group(shell, SWT.NONE);
    grpVerificator.setText("Verificator");
    grpVerificator.setBounds(273, 10, 501, 122);

    Button btnStartVerification = new Button(grpVerificator, SWT.NONE);
    btnStartVerification.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          if (sourcePath==null){
            Logger.messageBox("Select an input java file first");
            textPathToOriginalSource.notifyListeners(SWT.MouseDoubleClick, null);
            return;
          }
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
    checkCondition.setText("Example: a > b");
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
    
    Label lblNewLabel = new Label(grpVerificator, SWT.NONE);
    lblNewLabel.setBounds(10, 63, 224, 15);
    lblNewLabel.setText("Verification condition");
    
    Group grpOptions = new Group(shell, SWT.NONE);
    grpOptions.setText("Options");
    grpOptions.setBounds(10, 78, 257, 54);
    
    btnDevelopmentMode = new Button(grpOptions, SWT.CHECK);
    btnDevelopmentMode.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        Config.DEVELOP_MODE = btnDevelopmentMode.getSelection();
      }
    });
    btnDevelopmentMode.setBounds(10, 28, 161, 16);
    btnDevelopmentMode.setText("Development Mode");
  }
}
