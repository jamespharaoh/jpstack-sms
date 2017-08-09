package wbs.sms.smpp.daemon;

public
class SmppTypeOfNumber {

	public final static
	int unknown = 0x00,
		international = 0x01,
		national = 0x02,
		networkSpecific = 0x03,
		subscriberNumber = 0x04,
		alphanumeric = 0x05,
		abbreviated = 0x06;

}
