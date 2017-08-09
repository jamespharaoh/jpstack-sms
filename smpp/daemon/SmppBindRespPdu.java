package wbs.sms.smpp.daemon;

import java.io.IOException;

public
class SmppBindRespPdu
	extends SmppPdu {

	public
	SmppBindRespPdu (
			int commandId) {

		super (
			commandId);

		if (commandId != SmppCommandId.bindTransmitterResp
				&& commandId != SmppCommandId.bindReceiverResp
				&& commandId != SmppCommandId.bindTransceiverResp)
			throw new IllegalArgumentException ();

	}

	String systemId;

	@Override
	public
	void writeBody (
			SmppOutputStream out)
		throws IOException {

		out.writeCOctetString (
			systemId,
			16);

	}

	@Override
	public
	void readBody (
			SmppInputStream in)
		throws IOException {

		systemId =
			in.readCOctetString (
				16);

	}

	public
	String getSystemId () {

		return systemId;

	}

	public
	void setSystemId (
			String systemId) {

		this.systemId = systemId;

	}

}
