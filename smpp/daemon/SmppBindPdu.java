package wbs.sms.smpp.daemon;

import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode (
	callSuper = true)
@ToString
public
class SmppBindPdu
	extends SmppPdu {

	public
	SmppBindPdu (
			int commandId) {

		super (
			commandId);

	}

	String systemId;
	String password;
	String systemType;
	Integer interfaceVersion;
	Integer addrTon;
	Integer addrNpi;
	String addressRange;

	@Override
	public
	void writeBody (
			SmppOutputStream out)
		throws IOException {

		out.writeCOctetString (
			systemId,
			16);

		out.writeCOctetString (
			password,
			9);

		out.writeCOctetString (
			systemType,
			13);

		out.writeInteger (
			interfaceVersion,
			1);

		out.writeInteger (
			addrTon,
			1);

		out.writeInteger (
			addrNpi,
			1);

		out.writeCOctetString (
			addressRange,
			41);

	}

	@Override
	public
	void readBody (
			SmppInputStream in)
		throws IOException {

		systemId =
			in.readCOctetString (
				16);

		password =
			in.readCOctetString (
				9);

		systemType =
			in.readCOctetString (
				13);

		interfaceVersion =
			in.readInteger (
				1);

		addrTon =
			in.readInteger (
				1);

		addrNpi =
			in.readInteger (
				1);

		addressRange =
			in.readCOctetString (
				41);

	}

}
