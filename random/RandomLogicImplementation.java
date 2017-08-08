package wbs.utils.random;

import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("randomLogic")
public
class RandomLogicImplementation
	implements RandomLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// state

	Random random;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			random =
				new Random ();

		}

	}

	// implementation

	@Override
	public
	int randomJavaInteger (
			int limit) {

		return random.nextInt (
			limit);

	}

	@Override
	public
	long randomInteger (
			long limit) {

		if (limit < 0) {

			throw new IllegalArgumentException ();

		} else if (limit == Long.MAX_VALUE) {

			return random.nextLong ();

		} else if (limit <= Integer.MAX_VALUE) {

			return random.nextInt (
				toJavaIntegerRequired (
					limit));

		} else {

			throw new RuntimeException (
				"TODO");

		}

	}

	@Override
	public
	long randomInteger () {

		return random.nextLong ();

	}

	@Override
	public
	boolean randomBoolean (
			long numerator,
			long denominator) {

		return randomInteger (denominator) < numerator;

	}

	@Override
	public
	Duration randomDuration (
			@NonNull Duration limit) {

		return Duration.millis (
			randomInteger (
				limit.getMillis ()));

	}

	@Override
	public
	Duration randomDuration (
			@NonNull Duration limit,
			@NonNull Duration variance) {

		return Duration.millis (
			sum (
				+ limit.getMillis (),
				- variance.getMillis (),
				+ randomInteger (
					2 * variance.getMillis ())));

	}

	@Override
	public
	String generateString (
			String chars,
			int length) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (int i = 0; i < length; i ++) {

			stringBuilder.append (
				chars.charAt (
					randomJavaInteger (
						chars.length ())));

		}

		return stringBuilder.toString ();

	}

	@Override
	public
	String generateLowercase (
			int length) {

		return generateString (
			"abcdefghijklmnopqrstuvwxyz",
			length);

	}

	@Override
	public
	String generateUppercase (
			int length) {

		return generateString (
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
			length);

	}

	@Override
	public
	String generateNumeric (
			int length) {

		return generateString (
			"0123456789",
			length);

	}

	@Override
	public
	String generateNumericNoZero (
			int length) {

		return joinWithoutSeparator (
			generateString ("123456789", 1),
			generateNumeric (length - 1));

	}

	@Override
	public <Type>
	Type sample (
			Type[] options) {

		int index =
			randomJavaInteger (
				options.length);

		return options [
			index];

	}

	@Override
	public <Type>
	Type sample (
			List<Type> options) {

		if (options.isEmpty ()) {

			throw new IllegalArgumentException (
				"Options cannot be empty");

		}

		int index =
			randomJavaInteger (
				options.size ());

		return options.get (
			index);

	}

	@Override
	public <Type>
	List <Type> shuffleToList (
			@NonNull Iterable <Type> input) {

		List <Type> items =
			new ArrayList <Type> ();

		for (
			Type item
				: input
		) {

			items.add (
				item);

		}

		Collections.shuffle (
			items);

		return items;

	}

}
