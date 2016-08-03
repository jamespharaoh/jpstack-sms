package wbs.sms.gsm;

import java.nio.ByteBuffer;

public
class SmsDeliverPdu
	extends Pdu {

	private final
	boolean moreMessagesToSend, statusReportIndication, replyPath;

	private final
	Address originatingAddress;

	private final
	int protocolIdentifier;

	private final
	TimeStamp serviceCentreTimeStamp;

	private final
	UserDataHeader userDataHeader;

	private final
	String message;

	public
	SmsDeliverPdu (
			boolean newMoreMessagesToSend,
			boolean newStatusReportIndication,
			boolean newReplyPath,
			Address newOriginatingAddress,
			int newProtocolIdentifier,
			TimeStamp newServiceCentreTimeStamp,
			UserDataHeader newUserDataHeader,
			String newMessage) {

		moreMessagesToSend = newMoreMessagesToSend;
		statusReportIndication = newStatusReportIndication;
		replyPath = newReplyPath;
		originatingAddress = newOriginatingAddress;
		protocolIdentifier = newProtocolIdentifier;
		serviceCentreTimeStamp = newServiceCentreTimeStamp;
		userDataHeader = newUserDataHeader;
		message = newMessage;

	}

	public
	boolean getMoreMessagesToSend () {
		return moreMessagesToSend;
	}

	public
	boolean getStatusReportIndication () {
		return statusReportIndication;
	}

	public
	boolean getReplyPath () {
		return replyPath;
	}

	public
	Address getOriginatingAddress () {
		return originatingAddress;
	}

	public
	int getProtocolIdentifier () {
		return protocolIdentifier;
	}

	public
	TimeStamp getServiceCentreTimeStamp () {
		return serviceCentreTimeStamp;
	}

	public
	UserDataHeader getUserDataHeader () {
		return userDataHeader;
	}

	public
	String getMessage () {
		return message;
	}

	public static
	SmsDeliverPdu decode (
			ByteBuffer byteBuffer)
		throws PduDecodeException {

		int byteValue =
			byteBuffer.get () & 0xff;

		int messageTypeIndicator =
			byteValue & 0x03;

		if (messageTypeIndicator != 0x00) {

			throw new PduDecodeException (
				"Not an SMS-DELIVER");

		}

		boolean moreMessagesToSend =
			(byteValue & 0x04) == 0x04;

		boolean statusReportIndication =
			(byteValue & 0x20) == 0x20;

		boolean userDataHeaderIndicator =
			(byteValue & 0x40) == 0x40;

		boolean replyPath =
			(byteValue & 0x80) == 0x80;

		Address originatingAddress =
			Address.decode(byteBuffer);

		int protocolIdentifier =
			byteBuffer.get () & 0xff;

		int dataCodingScheme =
			byteBuffer.get () & 0xff;

		if (
			dataCodingScheme != 0x00
			&& dataCodingScheme != 0x11
			&& dataCodingScheme != 0xf1
		) {

			throw new PduDecodeException (
				"Unsupported data coding scheme: " + dataCodingScheme);

		}

		TimeStamp serviceCentreTimeStamp =
			TimeStamp.decode (
				byteBuffer);

		int userDataLength =
			byteBuffer.get () & 0xff;

		UserDataHeader userDataHeader =
			userDataHeaderIndicator
				? UserDataHeader.decode (byteBuffer)
				: null;

		int messageLength =
			userDataLength;

		if (userDataHeader != null)
			messageLength -= userDataHeader.length() - 1;

		String message =
			GsmUtils.decode (
				GsmUtils.unpack7bit (
					byteBuffer,
					messageLength));

		if (byteBuffer.hasRemaining ()) {

			throw new PduDecodeException (
				"Extra data at end");

		}

		// create the damned thing at last

		return new SmsDeliverPdu (
			moreMessagesToSend,
			statusReportIndication,
			replyPath,
			originatingAddress,
			protocolIdentifier,
			serviceCentreTimeStamp,
			userDataHeader,
			message);

	}

}