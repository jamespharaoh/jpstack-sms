package wbs.utils.etc;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import wbs.utils.io.RuntimeIoException;

public
class NetworkUtils {

	public static
	String runHostname () {

		try {

			Process process =
				Runtime.getRuntime ().exec ("hostname");

			String processOutput =
				IOUtils.toString (
					process.getInputStream ());

			String hostname =
				processOutput.trim ();

			return hostname;

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
