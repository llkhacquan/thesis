package vn.edu.vnu.uet.quannk_56.thesis;

public class SMTConvert {
	/**
	 * 
	 * @param symbol
	 *            = a_1_SYMINT, CONST_2, CONST_0.0, a_1_SYMREAL
	 */
	static public String getDeclaration(String symbol) {
		String result = "";
		if (symbol.substring(0, 6).equals("CONST_")) {
			// it is a const
			String sValue = symbol.substring(6);
			if (sValue.contains(".")) {
				// this const is a real const
				result = "(declare-const " + symbol + " Real) ";
				result += "(assert( = " + symbol + " " + sValue + " ))";
			} else {
				result = "(declare-const " + symbol + " Int) ";
				result += "(assert( = " + symbol + " " + sValue + " ))";
			}
		} else if (symbol.contains("SYM")) {
			String type = symbol.substring(symbol.indexOf("SYM") + 3);
			if (type.equals("INT"))
				result = "(declare-const " + symbol + " Int)";
			else if (type.equals("REAL"))
				result = "(declare-const " + symbol + " Real)";
		}

		return result;
	}

	static public String getAssert(String condition){
		return "(assert (" + condition + "))";
	}
}
