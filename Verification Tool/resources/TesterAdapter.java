import gov.nasa.jpf.symbc.Debug;

public class TesterAdapter {
  static final public String CHECKIN_PREFIX = "CHECK_I_._";
  static final public String CHECKIN_PREFIX_I = "CHECK_I_I_";
  static final public String CHECKIN_PREFIX_R = "CHECK_I_R_";
  static final public String CHECKIN_PREFIX_B = "CHECK_I_B_";
  static final public String CHECKOUT_PREFIX = "CHECK_O_._";
  static final public String CHECKOUT_PREFIX_I = "CHECK_O_I_";
  static final public String CHECKOUT_PREFIX_R = "CHECK_O_R_";
  static final public String CHECKOUT_PREFIX_B = "CHECK_O_B_";

  protected int checkInInteger(String name) {
    return Debug.makeSymbolicInteger(CHECKIN_PREFIX_I + name);
  }

  protected double checkInReal(String name) {
    return Debug.makeSymbolicReal(CHECKIN_PREFIX_R + name);
  }

  protected boolean checkInBoolean(String name) {
    return Debug.makeSymbolicBoolean(CHECKIN_PREFIX_B + name);
  }

  protected void checkOutInteger(int a, String name) {
    String value = Debug.getSymbolicIntegerValue(a);
    System.out.println(CHECKOUT_PREFIX_I + name + " = " + value);
  }

  protected void checkOutReal(double a, String name) {
    String value = Debug.getSymbolicRealValue(a);
    System.out.println(CHECKOUT_PREFIX_R + name + " = " + value);
  }

  protected void checkOutBoolean(Boolean a, String name) {
    String value = Debug.getSymbolicBooleanValue(a);
    System.out.println(CHECKOUT_PREFIX_B + name + " = " + value);
  }
}
