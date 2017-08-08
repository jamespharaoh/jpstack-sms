package wbs.utils.etc;

import static wbs.utils.string.StringUtils.bytesToString;

import lombok.NonNull;

import org.apache.commons.codec.binary.Base64;

public
class BinaryUtils {

	// hex encoding

	public static
	byte[] bytesFromHex (
			@NonNull String hex) {

		byte[] bytes =
			new byte [
				hex.length () / 2];

		for (
			int index = 0;
			index < bytes.length;
			index ++
		) {

			bytes [index] = (byte)
				Integer.parseInt (
					hex.substring (
						2 * index,
						2 * index + 2),
					16);

		}

		return bytes;

	}

	static final
	byte[] HEX_CHAR_TABLE = {
		(byte) '0', (byte) '1', (byte) '2', (byte) '3',
		(byte) '4', (byte) '5', (byte) '6', (byte) '7',
		(byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
	};

	public static
	String bytesToHex (
			@NonNull byte[] byteValues) {

		byte[] hex =
			new byte [2 * byteValues.length];

		int index = 0;

		for (byte byteValue : byteValues) {

			int intValue =
				byteValue & 0xFF;

			hex [index ++] =
				HEX_CHAR_TABLE [intValue >>> 4];

			hex [index ++] =
				HEX_CHAR_TABLE [intValue & 0xf];

		}

		return bytesToString (
			hex,
			"ASCII");

	}

	// base 64 encoding

	public static
	byte[] bytesFromBase64 (
			@NonNull String string) {

		return Base64.decodeBase64 (
			string);

	}

	public static
	String bytesToBase64 (
			@NonNull byte[] bytes) {

		return Base64.encodeBase64String (
			bytes);

	}

}
