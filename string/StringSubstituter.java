package wbs.utils.string;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

public
class StringSubstituter
	extends AbstractStringSubstituter {

	private final
	Map<String,String> params =
		new HashMap<String,String> ();

	public
	StringSubstituter param (
			@NonNull String name,
			@NonNull String value) {

		params.put (
			name,
			value);

		return this;

	}

	@Override
	protected
	String getSubstitute (
			@NonNull String name) {

		return params.get (
			name);

	}

}
