package wbs.sms.gsm;

public
class GenericInformationElement
	extends InformationElement {

	private final
	int id;

	private final
	byte[] data;

	public
	GenericInformationElement (
			int newId,
			byte[] newData) {

		id =
			newId;

		data =
			newData.clone ();

	}

	@Override
	public
	int getId () {
		return id;
	}

	public
	byte[] getData() {
		return data.clone ();
	}

	@Override
	public
	int length () {
		return data.length;
	}

}
