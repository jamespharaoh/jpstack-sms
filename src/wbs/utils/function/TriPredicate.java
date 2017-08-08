package wbs.utils.function;

import static wbs.utils.etc.Misc.todo;

import lombok.NonNull;

@FunctionalInterface
public
interface TriPredicate <First, Second, Third> {

	boolean test (
			First first,
			Second second,
			Third third);

	default
	TriPredicate <First, Second, Third> and (
			@NonNull TriPredicate <
				? super First,
				? super Second,
				? super Third
			> other) {

		throw todo ();

	}

	default
	TriPredicate <First, Second, Third> or (
			@NonNull TriPredicate <
				? super First,
				? super Second,
				? super Third
			> other) {

		throw todo ();

	}

	default
	TriPredicate <First, Second, Third> negate () {

		throw todo ();

	}

}
