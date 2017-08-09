package wbs.sms.gsm;

public abstract
class InformationElement {

	public abstract
	int getId ();

	public abstract
	int length ();

	public static
	InformationElement decode (
			int id,
			byte[] data)
		throws PduDecodeException {

		switch (id) {

		case 0:

			return ConcatenatedInformationElement.decode (
				data);

		default:

			return new GenericInformationElement (
				id,
				data);

		}

	}

}
