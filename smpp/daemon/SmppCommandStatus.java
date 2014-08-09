package wbs.sms.smpp.daemon;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public
class SmppCommandStatus {

	public final static
	int
		ok = 0x00000000,
		invMsgLen = 0x00000001,
		invCmdLen = 0x00000002,
		invCmdId = 0x00000003,
		invBndSts = 0x00000004,
		alyBnd = 0x00000005,
		invPrtFlg = 0x00000006,
		invRegDlvFlg = 0x00000007,
		sysErr = 0x00000008,
		invSrcAdr = 0x00000009,
		invDstAdr = 0x00000010,
		invMsgId = 0x0000000c,
		bindFail = 0x0000000d,
		invPasswd = 0x0000000e,
		invSysId = 0x0000000f,
		cancelFail = 0x00000011,
		replaceFail = 0x00000013,
		msgQFul = 0x00000014,
		invSerTyp = 0x00000015,
		invNumDests = 0x00000033,
		invDLName = 0x00000034,
		invDestFlag = 0x00000040,
		invSubRep = 0x00000042,
		invEsmClass = 0x00000043,
		cntSubDL = 0x00000044,
		submitFail = 0x00000045,
		invSrcTon = 0x00000048,
		invSrcNpi = 0x00000049,
		invDstTon = 0x00000050,
		invDstNpi = 0x00000051,
		invSysTyp = 0x00000053,
		invRepFlag = 0x00000054,
		invNumMsgs = 0x00000055,
		throttled = 0x00000058,
		invSched = 0x00000061,
		invExpiry = 0x00000062,
		invDftMsgId = 0x00000063,
		rxTAppn = 0x00000064,
		rxPAppn = 0x00000065,
		rxRAppn = 0x00000066,
		queryFail = 0x00000067,
		invOptParStream = 0x000000c0,
		optParNotAllwd = 0x000000c1,
		invParLen = 0x000000c2,
		missingOptParam = 0x000000c3,
		invOptParamVal = 0x000000c4,
		deliveryFailure = 0x000000fe,
		unknownErr = 0x000000ff;

	private SmppCommandStatus() {
		// never instantiated
	}

	private static Map<Integer,String> names =
		ImmutableMap.<Integer,String>builder ()
			.put (ok, "No error")
			.put (invMsgLen, "Invalid message length")
			.put (invCmdLen, "Invalid command length")
			.put (invCmdId, "Invalid command ID")
			.put (invBndSts, "Incorrect bind status for given command")
			.put (alyBnd, "ESME already in bound state")
			.put (invPrtFlg, "Invalid priority flag")
			.put (invRegDlvFlg, "Invalid registered delivery flag")
			.put (sysErr, "System error")
			.put (invSrcAdr, "Invalid source address")
			.put (invDstAdr, "Invalid destination address")
			.put (invMsgId, "Invalid message ID")
			.put (bindFail, "Bind failed")
			.put (invPasswd, "Invalid password")
			.put (invSysId, "Invalid system ID")
			.put (cancelFail, "Cancel SM failed")
			.put (replaceFail, "Replace SM failed")
			.put (msgQFul, "Message queue full")
			.put (invSerTyp, "Invalid service type")
			.put (invNumDests, "Invalid number of destinations")
			.put (invDLName, "Invalid distribution list name")
			.put (invDestFlag, "Invalid destination flag")
			.put (invSubRep, "Invalid 'submit with replace' request")
			.put (invEsmClass, "Invalid esm_class field data")
			.put (cntSubDL, "Cannot submit to distribution list")
			.put (submitFail, "Submit failed")
			.put (invSrcTon, "Invalid source address TON")
			.put (invSrcNpi, "Invalid source address NPI")
			.put (invDstTon, "Invalid destination address TON")
			.put (invDstNpi, "Invalid destination address NPI")
			.put (invSysTyp, "Invalid system_type field")
			.put (invRepFlag, "Invalid replace_if_present field")
			.put (invNumMsgs, "Invalid number of messages")
			.put (throttled, "Throttling error (ESME has exceeded allowed"
				+ " message limits)")
			.put (invSched, "Invalid scheduled delivery time")
			.put (invExpiry, "Invalid message validity period")
			.put (invDftMsgId, "Predefined message invalid or not found")
			.put (rxTAppn, "ESME receiver temporary app error code")
			.put (rxPAppn, "ESME receiver permanent app error code")
			.put (rxRAppn, "ESME receiver reject message error code")
			.put (queryFail, "Query failed")
			.put (invOptParStream, "Error in the optional part of the PDU body")
			.put (optParNotAllwd, "Optional parameter not allowed")
			.put (invParLen, "Invalid parameter length")
			.put (missingOptParam, "Expected optional parameter missing")
			.put (invOptParamVal, "Invalid optional parameter value")
			.put (deliveryFailure, "Delivery failure")
			.put (unknownErr, "Unknown error")
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