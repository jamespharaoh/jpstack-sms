package wbs.utils.function;

import static wbs.utils.etc.Misc.todo;

import lombok.NonNull;

@FunctionalInterface
public
interface QuadPredicate <First, Second, Third, Fourth> {

	boolean test (
			First first,
			Second second,
			Third third,
			Fourth fourth);

	default
	QuadPredicate <First, Second, Third, Fourth> and (
			@NonNull QuadPredicate <
				? super First,
				? super Second,
				? super Third,
				? super Fourth
			> other) {

		throw todo ();

	}

	default
	QuadPredicate <First, Second, Third, Fourth> or (
			@NonNull QuadPredicate <
				? super First,
				? super Second,
				? super Third,
				? super Fourth
			> other) {

		throw todo ();

	}

	default
	QuadPredicate <First, Second, Third, Fourth> negate () {

		throw todo ();

	}

	static <First, Second, Third, Fourth>
	boolean allTrue (
			@NonNull Iterable <QuadPredicate <First, Second, Third, Fourth>>
				predicates,
			@NonNull First first,
			@NonNull Second second,
			@NonNull Third third,
			@NonNull Fourth fourth) {

		for (
			QuadPredicate <First, Second, Third, Fourth> predicate
				: predicates
		) {

			if (
				! predicate.test (
					first,
					second,
					third,
					fourth)
			) {
				return false;
			}

		}

		return true;

	}

	static <First, Second, Third, Fourth>
	boolean anyFalse (
			@NonNull Iterable <QuadPredicate <First, Second, Third, Fourth>>
				predicates,
			@NonNull First first,
			@NonNull Second second,
			@NonNull Third third,
			@NonNull Fourth fourth) {

		for (
			QuadPredicate <First, Second, Third, Fourth> predicate
				: predicates
		) {

			if (
				! predicate.test (
					first,
					second,
					third,
					fourth)
			) {
				return true;
			}

		}

		return false;

	}

}
