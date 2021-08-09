package com.github.kailex.api.util;

/**
 * Service Class for Integers, Doubles, etc...
 *
 * @author Alexander Ley
 * @version 1.2.1
 */
public class NumberUtil {

	/**
	 * Only calculates the greatest common divisor.
	 * @return Returns gcd
	 */
	public static int ggT(int a, int b){
		a = Math.abs(a);
		b = Math.abs(b);

		//Check if 0
		if (a == 0) return b;
		if (b == 0) return a;

		while(b > 0){
			int z = a % b;
			a = b;
			b = z;
		}
		return a;
	}

	/**
	 * Check if two doubles almost are equal.
	 */
	public static boolean equals(double a, double b){
		double eps = 0.000001;
		return a == b || (a == 0 ? Math.abs(b) < eps : b == 0 ? Math.abs(a) < eps : Math.abs(a-b) / Math.min(Math.abs(a),Math.abs(b)) < eps);
	}

	/**
	 * Checks if string is any kind of number.
	 */
	public static boolean isNumber(String str){
		return isLong(str) || isDouble(str) || isInteger(str) || isFloat(str) || isShort(str) || isByte(str);
	}

	/**
	 * Checks if string can convert to a Byte.
	 */
	public static boolean isByte(String str){
		try{
			Byte.parseByte(str);
			return true;
		}
		catch (NumberFormatException ex){
			return false;
		}
	}

	/**
	 * Checks if string can convert to a Short.
	 */
	public static boolean isShort(String str){
		try{
			Short.parseShort(str);
			return true;
		}
		catch (NumberFormatException ex){
			return false;
		}
	}

	/**
	 * Checks if string can convert to an Integer.
	 */
	public static boolean isInteger(String str){
		try{
			Integer.parseInt(str);
			return true;
		}
		catch (NumberFormatException ex){
			return false;
		}
	}

	/**
	 * Checks if string can convert to an Long.
	 */
	public static boolean isLong(String str){
		try{
			Long.parseLong(str);
			return true;
		}
		catch (NumberFormatException ex){
			return false;
		}
	}

	/**
	 * Checks if string can convert to a Float.
	 */
	public static boolean isFloat(String str){
		try{
			Float.parseFloat(str);
			return true;
		}
		catch (NumberFormatException ex){
			return false;
		}
	}

	/**
	 * Checks if string can convert to a Double.
	 */
	public static boolean isDouble(String str){
		try{
			Double.parseDouble(str);
			return true;
		}
		catch (NumberFormatException ex){
			return false;
		}
	}
}