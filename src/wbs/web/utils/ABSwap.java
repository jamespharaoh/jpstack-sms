package wbs.web.utils;

/**
 * Simple class to help with alternating styles. toString () evaluates to one of
 * two strings which can be alternated by calling swap (). Alternatively, the
 * previous value is returned by the swap () function.
 */
public
class ABSwap {

	boolean nextIsA =
		true;

	String aString;
	String bString;

	/**
	 * Create a new swapper with the default values of "a" and "b".
	 */
	public
	ABSwap () {

		this (
			"a",
			"b");

	}

	/**
	 * Create a new ABSwap with the given values.
	 *
	 * @param newAString
	 *            the new 'A' string
	 * @param newBString
	 *            the new 'B' string
	 */
	public
	ABSwap (
			String newAString,
			String newBString) {

		aString =
			newAString;

		bString =
			newBString;

	}

	/**
	 * Swaps the current value and returns the previous one.
	 *
	 * @return the previous 'current' string
	 */
	public
	String swap () {

		String ret =
			nextIsA
				? aString
				: bString;

		nextIsA =
			! nextIsA;

		return ret;

	}

	/**
	 * Returns the 'current' string.
	 *
	 * @return the 'current' string.
	 */
	@Override
	public
	String toString () {

		return nextIsA
			? aString
			: bString;

	}

}
