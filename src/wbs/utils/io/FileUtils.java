package wbs.utils.io;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import lombok.NonNull;

import org.apache.commons.io.IOUtils;

public
class FileUtils {

	public static
	boolean fileExists (
			@NonNull String filename) {

		File file =
			new File (
				filename);

		return file.exists ();

	}

	public static
	boolean fileExistsFormat (
			@NonNull String ... arguments) {

		return fileExists (
			stringFormatArray (
				arguments));

	}

	public static
	void directoryCreateWithParents (
			@NonNull String directoryName) {

		try {

			org.apache.commons.io.FileUtils.forceMkdir (
				new File (
					directoryName));

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

	public static
	void directoryCreateWithParentsFormat (
			@NonNull String ... arguments) {

		directoryCreateWithParents (
			stringFormatArray (
				arguments));

	}

	public static
	void deleteDirectory (
			@NonNull String path) {

		try {

			org.apache.commons.io.FileUtils.deleteDirectory (
				new File (
					path));

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void forceMkdir (
			@NonNull String path) {

		try {

			org.apache.commons.io.FileUtils.forceMkdir (
				new File (
					path));

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	byte[] fileReadBytes (
			@NonNull String path) {

		try (

			FileInputStream inputStream =
				new FileInputStream (
					path);

		) {

			return IOUtils.toByteArray (
				inputStream);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void fileWriteBytes (
			@NonNull String path,
			@NonNull byte[] bytes) {

		try (

			FileOutputStream outputStream =
				new FileOutputStream (
					path);

		) {

			IOUtils.write (
				bytes,
				outputStream);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	@SuppressWarnings ("resource")
	public static
	SafeBufferedReader fileReaderBuffered (
			@NonNull String filename) {

		try {

			return new SafeBufferedReader (
				new InputStreamReader (
					new FileInputStream (
						filename),
					"utf-8"));

		} catch (UnsupportedEncodingException unsupportedEncodingException) {

			throw shouldNeverHappen ();

		} catch (FileNotFoundException fileNotFoundException) {

			throw new RuntimeFileNotFoundException (
				fileNotFoundException);

		}

	}

}