import gov.nasa.jpf.symbc.Debug;

public class UserTest extends TesterAdapter {
  public static void main(String[] args) {
    UserTest tester = new UserTest();
    tester.runTest();
  }

  public void UserCalls() {
    // CHECK_IN
    double a = checkInReal("a");
    double b = checkInReal("b");
    double c = checkInReal("c");

    // RUN_SYSTEM_TEST
    SystemOnTest system = new SystemOnTest();
    double result = 0;
    result = system.testMethod(a, b, c);

    // CHECK_OUT
    checkOutReal(a, "a2");
    checkOutReal(b, "b2");
    checkOutReal(c, "c2");
    checkOutReal(result, "result");
    // END_USER_CALL_METHOD
  }

  private void runTest() {
    UserCalls();
    getConditions();
  }

  private void getConditions() {
    Debug.printPC("path ");
  }
}
