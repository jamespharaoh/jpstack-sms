package wbs.sms.smpp.daemon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public
class SmppPdu {

	protected SmppPdu(int newCommandId) {
		commandId = newCommandId;
		commandStatus = SmppCommandStatus.ok;
	}

	protected SmppPdu(int newCommandId, int newCommandStatus) {
		commandId = newCommandId;
		commandStatus = newCommandStatus;
	}

	Integer commandStatus;
	Integer sequenceNumber;

	private int commandId;
	private List<SmppOptParam> optParams = new ArrayList<SmppOptParam>();

	public int getCommandId() {
		return commandId;
	}

	public List<SmppOptParam> getOptParams() {
		return optParams;
	}

	public SmppOptParam getOptParam(int tag) {
		for (SmppOptParam optParam : optParams)
			if (optParam.getTag() == tag)
				return optParam;
		return null;
	}

	public boolean isResponse() {
		return (commandId & 0x80000000) == 0x80000000;
	}

	public boolean isError() {
		return commandStatus != SmppCommandStatus.ok;
	}

	public byte[] getBody() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			SmppOutputStream out = new SmppOutputStream(baos);
			writeBody(out);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public
	void write (
			SmppOutputStream out)
		throws IOException {

		byte[] body =
			getBody ();

		out.writeInteger(16 + body.length, 4);
		out.writeInteger(commandId, 4);
		out.writeInteger(commandStatus, 4);
		out.writeInteger(sequenceNumber, 4);

		if (commandStatus == SmppCommandStatus.ok) {

			out.write (
				body);

			writeOptParams (
				out);

		}

	}

	public
	void writeBody (
			SmppOutputStream out)
		throws IOException {

	}

	public
	void readBody (
			SmppInputStream in)
		throws IOException {

	}

	public
	void readOptParams (
			SmppInputStream in)
		throws IOException {

		while (in.available () > 0) {

			optParams.add (
				SmppOptParam.read (in));

		}

	}

	public
	void writeOptParams (
			SmppOutputStream out)
		throws IOException {

		for (SmppOptParam optParam : optParams) {

			optParam.write(out);

		}

	}

	@Override
	public
	String toString () {

		if (isError())
			return SmppCommandId.getName(commandId) + " [" + sequenceNumber
					+ "] ERROR: " + SmppCommandStatus.getName(commandStatus);

		return SmppCommandId.getName(commandId) + " [" + sequenceNumber + "]";

	}

	public static
	SmppPdu read (
			SmppInputStream in)
		throws IOException {

		// read header

		int length = in.readInteger(4);
		int commandId = in.readInteger(4);
		int commandStatus = in.readInteger(4);
		int sequenceNumber = in.readInteger(4);

		// read body
		byte[] data = new byte[length - 16];
		if (in.read(data) < data.length)
			throw new IOException("Incomplete packet read");

		// construct pdu
		SmppPdu pdu = createPdu(commandId);

		// set main params
		pdu.setCommandStatus(commandStatus);
		pdu.setSequenceNumber(sequenceNumber);

		// read body and optional params
		if (commandStatus == SmppCommandStatus.ok) {
			SmppInputStream dataIn = new SmppInputStream(
					new ByteArrayInputStream(data));
			pdu.readBody(dataIn);
			pdu.readOptParams(dataIn);
		}

		// and return
		return pdu;
	}

	public static
	SmppPdu createPdu (
			int commandId) {

		switch (commandId) {

		case SmppCommandId.bindTransmitter:
		case SmppCommandId.bindReceiver:
		case SmppCommandId.bindTransceiver:
			return new SmppBindPdu(commandId);

		case SmppCommandId.bindTransmitterResp:
		case SmppCommandId.bindReceiverResp:
		case SmppCommandId.bindTransceiverResp:
			return new SmppBindRespPdu(commandId);

		case SmppCommandId.submitSm:
		case SmppCommandId.deliverSm:
			return new SmppSmPdu(commandId);

		case SmppCommandId.submitSmResp:
		case SmppCommandId.deliverSmResp:
			return new SmppSmRespPdu(commandId);
		}

		return new SmppRawPdu (
			commandId);

	}

	public
	Integer getCommandStatus () {

		return commandStatus;

	}

	public
	void setCommandStatus (
			Integer commandStatus) {

		this.commandStatus =
			commandStatus;

	}

	public
	Integer getSequenceNumber () {
		return sequenceNumber;
	}

	public
	void setSequenceNumber (
			Integer sequenceNumber) {

		this.sequenceNumber =
			sequenceNumber;

	}
}
