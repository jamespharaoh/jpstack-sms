package wbs.sms.smpp.daemon;

import java.io.IOException;

public
class SmppSmRespPdu
	extends SmppPdu {

	public
	SmppSmRespPdu (
			int commandId) {

		super (
			commandId);

	}

	String messageId;

	@Override
	public
	void writeBody (
			SmppOutputStream out)
		throws IOException {

		out.writeCOctetString (
			messageId,
			65);

	}

	@Override
	public
	void readBody (
			SmppInputStream in)
		throws IOException {

		messageId =
			in.readCOctetString (
				65);

	}

	public
	String getMessageId () {

		return messageId;

	}

	public
	void setMessageId (
			String messageId) {

		this.messageId =
			messageId;

	}
}
