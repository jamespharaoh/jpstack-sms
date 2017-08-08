package wbs.utils.string;

import java.util.List;
import java.util.function.Supplier;

import lombok.NonNull;

public
interface LazyString
	extends CharSequence {

	// interface

	List <String> toParts ();

	// default char sequence implementation

	@Override
	default
	int length () {

		return toString ().length ();

	}

	@Override
	default
	char charAt (
			int index) {

		return toString ().charAt (
			index);

	}

	@Override
	default
	CharSequence subSequence (
			int start,
			int end) {

		return toString ().subSequence (
			start,
			end);

	}

	// static factories

	static
	LazyString singleton (
			@NonNull String value) {

		return new LazyStringSimple (
			value);

	}

	static
	LazyString singleton (
			@NonNull CharSequence value) {

		return new LazyStringSingleton (
			() -> value.toString ());

	}

	static
	LazyString singleton (
			@NonNull Supplier <String> value) {

		return new LazyStringSingleton (
			value);

	}

}
