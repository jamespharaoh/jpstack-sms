package wbs.sms.smpp.daemon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public
class SmppRawPdu
	extends SmppPdu {

	byte[] body;

	public
	SmppRawPdu (
			int newCommandId) {

		super (
			newCommandId);

	}

	@Override
	public
	void readBody (
			SmppInputStream in)
		throws IOException {

		ByteArrayOutputStream baos =
			new ByteArrayOutputStream ();

		byte[] buf = new byte[1024];
		int numread;
		while ((numread = in.read(buf)) > 0)
			baos.write(buf, 0, numread);
		body = baos.toByteArray();
	}

	@Override
	public
	void writeBody (
			SmppOutputStream out)
		throws IOException {

		out.write(body);

	}

	@Override
	public
	byte[] getBody () {

		return body;

	}

	public
	void setBody (
			byte[] body) {

		this.body =
			body;

	}

}
