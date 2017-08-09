package wbs.sms.smpp.daemon;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public
class SmppCommandId {

	private
	SmppCommandId () {
		// never instantiated
	}

	public final static
	int
		genericNack = 0x80000000,
		bindReceiver = 0x00000001,
		bindReceiverResp = 0x80000001,
		bindTransmitter = 0x00000002,
		bindTransmitterResp = 0x80000002,
		querySm = 0x00000003,
		querySmResp = 0x80000003,
		submitSm = 0x00000004,
		submitSmResp = 0x80000004,
		deliverSm = 0x00000005,
		deliverSmResp = 0x80000005,
		unbind = 0x00000006,
		unbindResp = 0x80000006,
		replaceSm = 0x00000007,
		replaceSmResp = 0x80000007,
		cancelSm = 0x00000008,
		cancelSmResp = 0x80000008,
		bindTransceiver = 0x00000009,
		bindTransceiverResp = 0x80000009,
		outbind = 0x0000000B,
		enquireLink = 0x00000015,
		enquireLinkResp = 0x80000015,
		submitMulti = 0x00000021,
		submitMultiResp = 0x80000021,
		alertNotification = 0x00000102,
		dataSm = 0x00000103,
		dataSmResp = 0x80000103;

	private final static
	Map<Integer,String> names =
		ImmutableMap.<Integer,String>builder ()
			.put (genericNack, "generic_nack")
			.put (bindReceiver, "bind_receiver")
			.put (bindReceiverResp, "bind_receiver_resp")
			.put (bindTransmitter, "bind_transmitter")
			.put (bindTransmitterResp, "bind_transmitter_resp")
			.put (querySm, "query_sm")
			.put (querySmResp, "query_sm_resp")
			.put (submitSm, "submit_sm")
			.put (submitSmResp, "submit_sm_resp")
			.put (deliverSm, "deliver_sm")
			.put (deliverSmResp, "deliver_sm_resp")
			.put (unbind, "unbind")
			.put (unbindResp, "unbind_sm_resp")
			.put (replaceSm, "replace_sm")
			.put (replaceSmResp, "replace_sm_resp")
			.put (cancelSm, "cancel_sm")
			.put (cancelSmResp, "cancel_sm_resp")
			.put (bindTransceiver, "bind_transceiver")
			.put (bindTransceiverResp, "bind_transceiver_resp")
			.put (outbind, "outbind")
			.put (enquireLink, "enquire_link")
			.put (enquireLinkResp, "enquire_link_resp")
			.put (submitMulti, "submit_multi")
			.put (submitMultiResp, "submit_multi_resp")
			.put (alertNotification, "alert_notification")
			.put (dataSm, "data_sm")
			.put (dataSmResp, "data_sm_resp")
			.build ();

	public static
	String getName (
			int commandId) {

		String name =
			names.get (
				commandId);

		if (name != null)
			return name;

		return String.format (
			"0x%08x",
			commandId);

	}

}
