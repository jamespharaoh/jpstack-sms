package wbs.utils.etc;

import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormatArray;

import lombok.NonNull;

public
class DebugUtils {

	public static
	void debugFormat (
			@NonNull String ... arguments) {

		System.err.print (
			joinWithoutSeparator (
				"====== ",
				Thread.currentThread ().getName (),
				" ",
				stringFormatArray (
					arguments),
				"\n"));

	}

}
