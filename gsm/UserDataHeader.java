package wbs.sms.gsm;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public
class UserDataHeader {

	List<InformationElement> informationElements;

	private
	UserDataHeader (
			List<InformationElement> informationElements) {

		this.informationElements =
			informationElements;

	}

	public <T extends InformationElement>
	T find (
			Class<T> informationElementClass) {

		for (InformationElement informationElement
				: informationElements) {

			if (! informationElementClass.isInstance (
					informationElement))
				continue;

			return informationElementClass.cast (
				informationElement);

		}

		return null;

	}

	public
	int length () {

		int length = 0;

		for (InformationElement informationElement
				: informationElements) {

			length +=
				+ 1 // id
				+ 1 // length
				+ informationElement.length (); // data

		}

		return length;

	}

	public static
	UserDataHeader decode (
			ByteBuffer byteBuffer) {

		int dataLength;

		try {

			dataLength =
				byteBuffer.get () & 0xff;

		} catch (BufferUnderflowException exception) {

			throw new PduDecodeException (
				"No data supplied in UserDataHeader.decode (...)");

		}

		byte[] data;

		try {

			data =
				new byte [dataLength];

			} catch (BufferUnderflowException exception) {

			throw new PduDecodeException (
				"Ran out of data in UserDataHeader.decode (...)");

		}

		byteBuffer.get (data);

		List<InformationElement> list =
			new ArrayList<InformationElement> ();

		int index = 0;

		while (index < data.length) {

			// read off the information element identifier and data length

			if (data.length - index - 2 < 0) {

				throw new PduDecodeException (
					"Data length 1");

			}

			int informationElementId =
				data [index++] & 0xFF;

			int informationElementDataLength =
				data [index++] & 0xFF;

			// then get the data

			if (data.length - index - informationElementDataLength < 0) {

				throw new PduDecodeException (
					"Data length 2");

			}

			byte[] informationElementData =
				new byte [informationElementDataLength];

			System.arraycopy (
				data,
				index,
				informationElementData,
				0,
				informationElementDataLength);

			index +=
				informationElementDataLength;

			InformationElement informationElement =
				InformationElement.decode (
					informationElementId,
					informationElementData);

			list.add (
				informationElement);

		}

		return new UserDataHeader (
			list);

	}

}
