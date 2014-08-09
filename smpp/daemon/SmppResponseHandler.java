package wbs.sms.smpp.daemon;

public interface SmppResponseHandler {
	void handle(SmppPdu pdu);
}
