public class SystemOnTest {
  double testMethod(double a, int b) {
    double result;
    if (a > b){
      result = a / (b - 2);
      a += 1;
    }
    else{
      result = a + b;
      a += 1;
    }
    return result;
  }
}
