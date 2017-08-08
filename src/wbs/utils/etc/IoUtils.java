package wbs.utils.etc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import lombok.NonNull;

import org.apache.commons.io.IOUtils;

import wbs.utils.io.RuntimeIoException;

public
class IoUtils {

	public static
	void writeBytes (
			@NonNull OutputStream target,
			@NonNull byte[] bytes) {

		try {

			target.write (
				bytes);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void writeString (
			@NonNull Writer target,
			@NonNull String string) {

		try {

			target.write (
				string);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void writeString (
			@NonNull Writer target,
			@NonNull CharSequence charSequence) {

		try {

			target.append (
				charSequence);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void writeByte (
			@NonNull OutputStream target,
			int value) {

		try {

			target.write (
				value);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	byte[] readBytes (
			@NonNull InputStream source) {

		try {

			return IOUtils.toByteArray (
				source);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	String readString (
			@NonNull Reader source) {

		try {

			return IOUtils.toString (
				source);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
