package wbs.sms.smpp.daemon;

import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode (
	callSuper = true)
public
class SmppSmPdu
	extends SmppPdu {

	public
	SmppSmPdu (
			int commandId) {

		super (
			commandId);

	}

	String serviceType;
	Integer sourceAddrTon;
	Integer sourceAddrNpi;
	String sourceAddr;
	Integer destAddrTon;
	Integer destAddrNpi;
	String destAddr;
	Integer esmClass;
	Integer protocolId;
	Integer priorityFlag;
	String scheduleDeliveryTime;
	String validityPeriod;
	Integer registeredDelivery;
	Integer replaceIfPresentFlag;
	Integer dataCoding;
	Integer smDefaultMsgId;

	byte[] shortMessage;

	@Override
	public
	void writeBody (
			SmppOutputStream out)
		throws IOException {

		out.writeCOctetString(serviceType, 6);
		out.writeInteger(sourceAddrTon, 1);
		out.writeInteger(sourceAddrNpi, 1);
		out.writeCOctetString(sourceAddr, 21);
		out.writeInteger(destAddrTon, 1);
		out.writeInteger(destAddrNpi, 1);
		out.writeCOctetString(destAddr, 21);
		out.writeInteger(esmClass, 1);
		out.writeInteger(protocolId, 1);
		out.writeInteger(priorityFlag, 1);
		out.writeCOctetString(scheduleDeliveryTime, 17);
		out.writeCOctetString(validityPeriod, 17);
		out.writeInteger(registeredDelivery, 1);
		out.writeInteger(replaceIfPresentFlag, 1);
		out.writeInteger(dataCoding, 1);
		out.writeInteger(smDefaultMsgId, 1);
		out.writeInteger(shortMessage.length, 1);
		out.write(shortMessage);

	}

	@Override
	public
	void readBody (
			SmppInputStream in)
		throws IOException {

		serviceType = in.readCOctetString(6);
		sourceAddrTon = in.readInteger(1);
		sourceAddrNpi = in.readInteger(1);
		sourceAddr = in.readCOctetString(21);
		destAddrTon = in.readInteger(1);
		destAddrNpi = in.readInteger(1);
		destAddr = in.readCOctetString(21);
		esmClass = in.readInteger(1);
		protocolId = in.readInteger(1);
		priorityFlag = in.readInteger(1);
		scheduleDeliveryTime = in.readCOctetString(17);
		validityPeriod = in.readCOctetString(17);
		registeredDelivery = in.readInteger(1);
		replaceIfPresentFlag = in.readInteger(1);
		dataCoding = in.readInteger(1);
		smDefaultMsgId = in.readInteger(1);
		shortMessage = new byte[in.readInteger(1)];

		in.read(shortMessage);

	}

}
