package wbs.sms.smpp.daemon;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import lombok.extern.log4j.Log4j;

@Log4j
public
class SmppConnection {

	private
	Socket socket;

	private
	SmppInputStream inputStream;

	private
	SmppOutputStream outputStream;

	private
	int nextSequenceNumber = 1;

	private
	SmppInboundHandler inboundHandler;

	private
	Map<Integer,SmppResponseHandler> responseHandlers =
		new HashMap<Integer,SmppResponseHandler> ();

	private
	Thread inboundThread;

	public
	SmppConnection (
			Socket newSocket,
			SmppInboundHandler newInboundHandler)
		throws IOException {

		socket =
			newSocket;

		inputStream =
			new SmppInputStream (
				socket.getInputStream ());

		outputStream =
			new SmppOutputStream (
				socket.getOutputStream ());

		inboundHandler =
			newInboundHandler;

		inboundThread =
			new Thread (
				new InboundThread ());

		inboundThread.start ();

	}

	public
	void send (
			SmppPdu pdu,
			SmppResponseHandler responseHandler)
		throws IOException {

		// allocate sequence number

		pdu.setSequenceNumber (
			nextSequenceNumber ++);

		if (responseHandler != null) {

			responseHandlers.put (
				pdu.getSequenceNumber (),
				responseHandler);

		}

		// send

		log.debug (
			"Sending " + pdu);

		synchronized (this) {

			pdu.write (
				outputStream);

		}

	}

	public
	SmppPdu sendWait (
			SmppPdu pdu)
		throws
			IOException,
			InterruptedException {

		final
		ArrayBlockingQueue<SmppPdu> responseQueue =
			new ArrayBlockingQueue<SmppPdu> (
				1);

		send (
			pdu,
			new SmppResponseHandler () {

			@Override
			public
			void handle (
					SmppPdu pdu) {

				try {

					responseQueue.put (
						pdu);

				} catch (InterruptedException exception) {

					throw new RuntimeException (
						exception);

				}

			}

		});

		return responseQueue.take ();

	}

	public
	void close()
		throws IOException {

		inboundThread.interrupt ();

		socket.close ();

	}

	public
	class InboundThread
		implements Runnable {

		@Override
		public
		void run() {

			try {
				while (true) {

					// read a pdu

					SmppPdu pdu =
						SmppPdu.read (
							inputStream);

					log.debug (
						"Received " + pdu);

					if (pdu.isResponse ()) {

						// if it's a response hand it off to the registered
						// handler (if any)
						SmppResponseHandler responseHandler = responseHandlers
								.remove(pdu.getSequenceNumber());
						if (responseHandler != null)
							responseHandler.handle(pdu);

					} else {

						// otherwise hand it off to the inbound handler
						SmppPdu respPdu = inboundHandler.handle(pdu);

						// if the inbound handler returned a response pdu, send
						// it back

						if (respPdu != null) {

							if (!respPdu.isResponse()) {

								log.warn (
									"InboundHandler returned non-response PDU "
									+ SmppCommandId.getName (
										respPdu.getCommandId()));

								socket.close ();

								return;

							}

							respPdu.setSequenceNumber (
								pdu.getSequenceNumber ());

							log.debug (
								"Sending " + respPdu);

							synchronized (this) {

								respPdu.write (
									outputStream);

							}

						}

					}

				}

			} catch (IOException exception) {

				if (Thread.interrupted())
					return;

				log.warn("Inbound thread died: " + exception);

				try {

					socket.close ();

				} catch (IOException e1) {
				}

			}

		}

	}

}
