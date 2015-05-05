public class SystemOnTest {
  double testMethod(double a, int b) {
    double result;
    if (a > b && b > 1)
      result = a / (b - 2);
    else
      result = a + b;
    return result;
  }
}
