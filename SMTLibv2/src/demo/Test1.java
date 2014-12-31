package demo;

import gov.nasa.jpf.symbc.Debug;

public class Test1 {
	private static boolean isBigger(double a, double b) {
		return a > b;
	}

	public static double test(double x, double y) {
		double z = x + y;
		z = Debug.makeSymbolicInteger("z123123");
		if (isBigger(z, 0)) {
			z = 1;
		} else {
			z = z - x;
		}
		z = 2 * z;

		Debug.printPC("-------------PrePC----------\n");
		System.out.println("=>");
		System.out.println("z_SYMREAL == " + Debug.getSymbolicRealValue(z));
		System.out.println("x_SYMREAL == " + Debug.getSymbolicRealValue(x));
		System.out.println("y_SYMREAL == " + Debug.getSymbolicRealValue(y));
		System.out.println("-------------PostPC----------\n\n");
		Debug.printPC("-------------PC--------------");
		
		return z;
	}

	public static void main(String[] args) {
		test(0, 0);
		
	}
}
