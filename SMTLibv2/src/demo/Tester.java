package demo;

import gov.nasa.jpf.symbc.Debug;
import vn.edu.vnu.uet.quannk_56.thesis.VerificationTool;
import vn.edu.vnu.uet.quannk_56.thesis.constraint.PathConstraint;

public class Tester extends VerificationTool {
	private PathConstraint pathCondition = new PathConstraint();
	
	public static void main(String[] args) {
		Tester tester = new Tester();
		tester.runTest();
	}

	public void UserCalls() {
		// Initiate the symbolic variable here
		int a = checkInInteger("a");
		int b = checkInInteger("b");
		int c = checkInInteger("c");

		// Run the system on test
		SystemOnTest system = new SystemOnTest();
		int result = 0;
		result = system.testMethod(a, b, c);

		// check out result
		checkOutInteger(a, "a2");
		checkOutInteger(b, "b2");
		checkOutInteger(c, "c2");
		checkOutInteger(result, "result");
	}

	private void runTest() {
		UserCalls();

		getConditions();
	}

	private void getConditions() {
		Debug.printPC("path ");
	}
}
