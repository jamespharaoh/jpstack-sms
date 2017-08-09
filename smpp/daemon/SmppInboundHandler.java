package wbs.sms.smpp.daemon;

public
interface SmppInboundHandler {

	SmppPdu handle (
			SmppPdu pdu);

}
