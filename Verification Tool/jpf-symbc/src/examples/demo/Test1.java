package demo;

import gov.nasa.jpf.symbc.Debug;

public class Test1 {

	public static void test(double a, double b) {
		double c;
		//c = Debug.makeSymbolicReal("c");
		c = a * b;
		if (a > b) {
			c = a - b;
			a = a + 2;
		} else {
			c = b - a;
			b = c + a;
		}

		Debug.printPC("PrePC");
		System.out.println("=>");
		System.out.println("c_SYMREAL == " + Debug.getSymbolicRealValue(c));
		System.out.println("a_SYMREAL == " + Debug.getSymbolicRealValue(a));
		System.out.println("b_SYMREAL == " + Debug.getSymbolicRealValue(b));
		System.out.println("PostPC");
		System.out.println(Debug.getSolvedPC());
	}

	public static void main(String[] args) {
		test(0, 0);
	}
}
