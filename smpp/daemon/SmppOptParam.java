package wbs.sms.smpp.daemon;

import java.io.IOException;

public
class SmppOptParam {

	public final static
	int
		dest_addr_subunit = 0x0005,
		dest_network_type = 0x0006,
		dest_bearer_type = 0x0007,
		dest_telematics_id = 0x0008,
		source_addr_subunit = 0x000d,
		source_network_type = 0x000e,
		source_bearer_type = 0x000f,
		source_telematics_id = 0x0010,
		qos_time_to_live = 0x0017,
		payload_type = 0x0019,
		additional_status_info_text = 0x001d,
		receipted_message_id = 0x001e,
		ms_msg_wait_facilities = 0x0030,
		privacy_indicator = 0x0201,
		source_subaddress = 0x0202,
		dest_subaddress = 0x0203,
		user_message_reference = 0x0204,
		user_response_code = 0x0205,
		source_port = 0x020a,
		destination_port = 0x020b,
		sar_msg_ref_num = 0x020c,
		language_indicator = 0x020d,
		sar_total_segments = 0x020e,
		sar_segment_seqnum = 0x020f,
		SC_interface_version = 0x0210,
		callback_num_pres_ind = 0x0302,
		callback_num_atag = 0x0303,
		number_of_messages = 0x0304,
		callback_num = 0x0381,
		dpf_result = 0x0420,
		set_dpf = 0x0421,
		ms_availability_status = 0x0422,
		network_error_code = 0x0423,
		message_payload = 0x0424,
		delivery_failure_reason = 0x0425,
		more_messages_to_send = 0x0426,
		message_state = 0x0427,
		ussd_service_op = 0x0501,
		display_time = 0x1201,
		sms_signal = 0x1203,
		ms_validity = 0x1204,
		alert_on_message_delivery = 0x130c,
		its_reply_type = 0x1380,
		its_session_info = 0x1383;

	private
	int tag;

	private
	byte[] value;

	public
	SmppOptParam (
			int newTag,
			byte[] newValue) {

		tag =
			newTag;

		value =
			newValue.clone ();

	}

	public
	int getTag () {
		return tag;
	}

	public static
	SmppOptParam read (
			SmppInputStream in)
		throws IOException {

		int tag =
			in.readInteger (
				2);

		int length =
			in.readInteger (
				2);

		byte[] value =
			new byte [length];

		in.read (
			value);

		return new SmppOptParam (
			tag,
			value);

	}

	public
	void write(
			SmppOutputStream out)
		throws IOException {

		out.writeInteger (
			tag,
			2);

		out.writeInteger (
			value.length,
			2);

		out.write (
			value);

	}

	public
	int getInteger (
			int length) {

		// check length is ok

		switch (value.length) {

		case 2:
		case 4:

			if (length != value.length) {

				throw new IllegalStateException (
					"Data is not correct length");

			}

			break;

		default:

			throw new IllegalStateException (
				"Data is wrong length for integer value");

		}

		// construct integer

		int returnValue = 0;

		for (
			int index = 0;
			index < value.length;
			index ++
		) {

			returnValue <<= 8;

			int currentByte =
				value [index];

			returnValue |=
				currentByte;

		}

		// return

		return returnValue;

	}

}
