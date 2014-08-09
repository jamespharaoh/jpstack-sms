package wbs.sms.smpp.daemon;

import java.io.IOException;
import java.net.Socket;

import lombok.extern.log4j.Log4j;

@Log4j
public
class SmppClient {

	SmppConnection conn;

	public static
	enum SessionState {

		open,
		boundTx,
		boundRx,
		boundTrx,
		closed;

	}

	SessionState sessionState =
		SessionState.open;

	SmppInboundHandler inboundHandler;

	public
	SmppClient (
			Socket newSocket,
			SmppInboundHandler newInboundHandler)
		throws IOException {

		inboundHandler =
			newInboundHandler;

		conn =
			new SmppConnection (
				newSocket,
				new MyInboundHandler(
					newInboundHandler));

	}

	public
	void send (
			SmppPdu pdu,
			SmppResponseHandler responseHandler)
		throws IOException {

		conn.send (
			pdu,
			responseHandler);

	}

	public synchronized
	SmppBindRespPdu bind (
			SmppPdu pdu)
		throws
			IOException,
			InterruptedException {

		if (pdu.getCommandId() != SmppCommandId.bindTransmitter
				&& pdu.getCommandId() != SmppCommandId.bindReceiver
				&& pdu.getCommandId() != SmppCommandId.bindTransceiver)
			throw new SmppException("Invalid bind PDU");

		if (sessionState != SessionState.open)
			throw new SmppException("Session is closed");

		// send it

		SmppBindRespPdu respPdu =
			(SmppBindRespPdu)
			conn.sendWait (pdu);

		// check command was successful
		if (respPdu.getCommandStatus() != SmppCommandStatus.ok) {
			try {
				conn.close();
			} catch (IOException e) {
			}
			sessionState = SessionState.closed;
			throw new SmppException(respPdu.getCommandStatus());
		}

		// check version number (if provided)

		SmppOptParam ifvParam =
			respPdu.getOptParam (
				SmppOptParam.SC_interface_version);

		if (ifvParam != null && ifvParam.getInteger(1) < 0x34) {
			try {
				conn.close();
			} catch (IOException e) {
			}
			sessionState = SessionState.closed;
			throw new SmppException("Unsupported PDU version: "
					+ ifvParam.getInteger(1));
		}

		// update state

		switch (respPdu.getCommandId()) {
		case SmppCommandId.bindTransmitterResp:
			sessionState = SessionState.boundTx;
		case SmppCommandId.bindReceiverResp:
			sessionState = SessionState.boundRx;
		case SmppCommandId.bindTransceiverResp:
			sessionState = SessionState.boundTrx;
		}

		// return

		return respPdu;

	}

	public
	SmppBindRespPdu bind (
			int commandId,
			String systemId,
			String password,
			String systemType)
		throws
			IOException,
			InterruptedException {

		SmppBindPdu pdu =
			new SmppBindPdu (commandId);

		pdu.setSystemId(systemId);
		pdu.setPassword(password);
		pdu.setSystemType(systemType);
		pdu.setInterfaceVersion(0x34);

		return bind (
			pdu);

	}

	public
	SmppBindRespPdu bindTx (
			String systemId,
			String password,
			String systemType)
		throws
			IOException,
			InterruptedException {

		return bind (
			SmppCommandId.bindTransmitter,
			systemId,
			password,
			systemType);

	}

	public
	SmppBindRespPdu bindRx (
			String systemId,
			String password,
			String systemType)
		throws
			IOException,
			InterruptedException {

		return bind (
			SmppCommandId.bindReceiver,
			systemId,
			password,
			systemType);

	}

	public
	SmppBindRespPdu bindTrx (
			String systemId,
			String password,
			String systemType)
		throws
			IOException,
			InterruptedException {

		return bind (
			SmppCommandId.bindTransceiver,
			systemId,
			password,
			systemType);

	}

	class MyInboundHandler
		extends AbstractSmppInboundHandler {

		MyInboundHandler (
				SmppInboundHandler delegate) {

			super (
				delegate);

		}

		SmppPdu handleEnquireLinkPdu (
				SmppPdu pdu) {

			log.debug (
				"Responding to enquire_link");

			return new SmppPdu (
				SmppCommandId.enquireLinkResp);

		}

	}

}
