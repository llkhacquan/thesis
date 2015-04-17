public class SystemOnTest {
	int testMethod(int a, int b, int c) {
		if (a >= b)
			return c / (a + b * c);
		else
			return c + b;
	}

	public static void main(String... args) {
		int a = 0;
		int b = 1;
		int c = 3;

		SystemOnTest system = new SystemOnTest();
		system.testMethod(a, b, c);
	}
}
