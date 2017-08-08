package wbs.utils.string;

import static wbs.utils.etc.Misc.doNothing;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class NullFormatWriter
	implements FormatWriter {

	// properties

	@Getter @Setter
	String indentString = "";

	@Getter @Setter
	long indentSize = 0l;

	// implementation

	@Override
	public
	void writeString (
			@NonNull LazyString lazyString) {

		doNothing ();

	}

	@Override
	public
	void close () {

		doNothing ();

	}

}
