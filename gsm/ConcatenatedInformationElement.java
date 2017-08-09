package wbs.sms.gsm;

public
class ConcatenatedInformationElement
		extends InformationElement {

	int ref, seqMax, seqNum;

	public
	ConcatenatedInformationElement (
			int newRef,
			int newSeqMax,
			int newSeqNum) {

		if (newRef > 255 || newSeqMax > 255 || newSeqNum > 255)
			throw new IllegalArgumentException();

		ref = newRef;
		seqMax = newSeqMax;
		seqNum = newSeqNum;

	}

	@Override
	public
	int getId () {
		return 0;
	}

	public
	int getRef () {
		return ref;
	}

	public
	int getSeqMax () {
		return seqMax;
	}

	public
	int getSeqNum () {
		return seqNum;
	}

	@Override
	public
	int length () {
		return 3;
	}

	public static
	ConcatenatedInformationElement decode (
			byte[] data)
		throws PduDecodeException {

		if (data.length != 3) {

			throw new PduDecodeException (
				"SMS concat IE must be 3 bytes long");

		}

		return new ConcatenatedInformationElement (
			data [0] & 0xff,
			data [1] & 0xff,
			data [2] & 0xff);

	}

}
