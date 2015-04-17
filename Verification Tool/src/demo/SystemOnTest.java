package demo;

public class SystemOnTest {
	int testMethod(int a, int b, int c) {
		if (a >= b)
			return c / (a + b * c);
		else
			return c + b;
	}
}
