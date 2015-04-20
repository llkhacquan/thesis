public class SystemOnTest {
  int testMethod(boolean a, int b, int c) {
    if (a)
      return c / (b + c);
    else
      return c + b;
  }
}
