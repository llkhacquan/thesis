package demo;

import gov.nasa.jpf.symbc.Debug;

public class Test3 {

	public static void test(int a, int b) {
		
		Debug.assume(a < 10 && a > 0);
		Debug.assume(b < 5 && b > 0);

		int c = 0;
		while (a != b) {
			if (a > b)
				a = a - b;
			else
				b = b - a;
		}
		c = a;

		Debug.printPC("PrePC");
		System.out.println("=>");
		System.out.println("c_SYMINT == " + Debug.getSymbolicIntegerValue(c));
		System.out.println("a_SYMINT == " + Debug.getSymbolicIntegerValue(a));
		System.out.println("b_SYMINT == " + Debug.getSymbolicIntegerValue(b));
		System.out.println("PostPC");
		System.out.println(Debug.getSolvedPC());
	}

	public static void main(String[] args) {
		test(0, 0);
	}
}
