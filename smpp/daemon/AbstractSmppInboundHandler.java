package wbs.sms.smpp.daemon;

public
class AbstractSmppInboundHandler
	implements SmppInboundHandler {

	private
	SmppInboundHandler delegate;

	protected
	AbstractSmppInboundHandler (
			SmppInboundHandler newDelegate) {

		delegate =
			newDelegate;

	}

	protected
	AbstractSmppInboundHandler () {

		this (
			null);

	}

	@Override
	public
	SmppPdu handle (
			SmppPdu pdu) {

		switch (pdu.getCommandId ()) {

		case SmppCommandId.bindTransmitter:

			return handleBindTransmitterPdu (
				(SmppBindPdu) pdu);

		case SmppCommandId.enquireLink:

			return handleEnquireLink (
				pdu);

		case SmppCommandId.deliverSm:

			return handleDeliverSm (
				(SmppSmPdu) pdu);

		}

		return handleUnknown (
			pdu);

	}

	/**
	 * Called if we can't handle this PDU. Delegates if possible, otherwise
	 * returns a generic nack.
	 */
	protected
	SmppPdu handleUnknown (
			SmppPdu pdu) {

		// delegate if appropriate

		if (delegate != null) {

			return delegate.handle (
				pdu);

		}

		// otherwise just return generic nack

		return new SmppPdu (
			SmppCommandId.genericNack,
			SmppCommandStatus.invCmdId);

	}

	protected
	SmppPdu delegate (
			SmppPdu pdu) {

		if (delegate == null)
			return null;

		return delegate.handle (
			pdu);

	}

	protected
	SmppPdu handleBindTransmitterPdu (
			SmppBindPdu pdu) {

		return handleUnknown (
			pdu);

	}

	protected
	SmppPdu handleDeliverSm (
			SmppSmPdu pdu) {

		return handleUnknown (
			pdu);

	}

	protected
	SmppPdu handleEnquireLink (
			SmppPdu pdu) {

		return handleUnknown (
			pdu);

	}

}
