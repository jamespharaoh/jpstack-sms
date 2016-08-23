package wbs.sms.message.stats.console;

import static wbs.framework.utils.etc.LogicUtils.equalSafe;

import lombok.NonNull;

/**
 * A SmsStatsGroup is a decorator for any object which uses a string to control the
 * sort order.. It delegates equals (...) and hashCode () to the object, but
 * delegates compareTo (...) to the name.
 */
public
class SmsStatsGroup<T>
	implements Comparable<SmsStatsGroup<T>> {

	private final
	T object;

	private final
	String name;

	public
	SmsStatsGroup (
			@NonNull T newObject,
			@NonNull String newName) {

		object = newObject;
		name = newName;

	}

	public static
	SmsStatsGroup<String> forString (
			String source) {

		String canon =
			source.intern ();

		return new SmsStatsGroup<String> (
			canon,
			canon);

	}

	public
	T getObject () {
		return object;
	}

	public
	String getName () {
		return name;
	}

	@Override
	public
	int compareTo (
			SmsStatsGroup<T> other) {

		return name.compareTo (
			other.name);

	}

	@Override
	public
	int hashCode () {

		return object
			.hashCode ();

	}

	@Override
	public
	boolean equals (
			Object otherObject) {

		SmsStatsGroup <?> other =
			(SmsStatsGroup <?>) otherObject;

		return equalSafe (
			object,
			other.object);

	}

}
