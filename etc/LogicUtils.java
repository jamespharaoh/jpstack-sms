package wbs.utils.etc;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;

public
class LogicUtils {

	@SafeVarargs
	public static
	boolean allOf (
			@NonNull Supplier <Boolean>... conditions) {

		for (
			Supplier <Boolean> condition
				: conditions
		) {

			if (! condition.get ()) {
				return false;
			}

		}

		return true;

	}

	public static
	boolean allOf (
			@NonNull Iterable <Supplier <Boolean>> conditions) {

		for (
			Supplier <Boolean> condition
				: conditions
		) {

			if (! condition.get ()) {
				return false;
			}

		}

		return true;

	}

	public static <Type>
	Predicate <Type> predicatesCombineAll (
			@NonNull Iterable <Predicate <Type>> predicates) {

		return value -> {

			for (
				Predicate <Type> predicate
					: predicates
			) {

				if (
					! predicate.test (
						value)
				) {
					return false;
				}

			}

			return true;

		};

	}

	@SafeVarargs
	public static <Type>
	Predicate <Type> predicatesCombineAll (
			@NonNull Predicate <Type> ... predicates) {

		return value -> {

			for (
				Predicate <Type> predicate
					: predicates
			) {

				if (
					! predicate.test (
						value)
				) {
					return false;
				}

			}

			return true;

		};

	}

	public static <Type>
	Predicate <Type> predicatesCombineAny (
			@NonNull Iterable <Predicate <Type>> predicates) {

		return value -> {

			for (
				Predicate <Type> predicate
					: predicates
			) {

				if (
					predicate.test (
						value)
				) {
					return true;
				}

			}

			return false;

		};

	}

	public static
	boolean anyOf (
			@NonNull Iterable <Supplier <Boolean>> conditions) {

		for (
			Supplier <Boolean> condition
				: conditions
		) {

			if (condition.get ()) {
				return true;
			}

		}

		return false;

	}

	@SafeVarargs
	public static
	boolean anyOf (
			@NonNull Supplier <Boolean> ... conditions) {

		for (
			Supplier <Boolean> condition
				: conditions
		) {

			if (condition.get ()) {
				return true;
			}

		}

		return false;

	}

	@SafeVarargs
	public static
	boolean noneOf (
			@NonNull Supplier <Boolean>... conditions) {

		for (
			Supplier <Boolean> condition
				: conditions
		) {

			if (condition.get ()) {
				return false;
			}

		}

		return true;

	}

	public static
	boolean allAreTrue (
			List <Boolean> values) {

		for (
			Boolean value
				: values
		) {

			if (! value)
				return false;

		}

		return true;

	}

	public static
	boolean anyIsTrue (
			List <Boolean> values) {

		for (
			Boolean value
				: values
		) {

			if (value)
				return true;

		}

		return false;

	}

	public static
	boolean not (
			boolean value) {

		return ! value;

	}

	public static <Type>
	Type ifThenElse (
			boolean condition,
			@NonNull Supplier <Type> trueValue,
			@NonNull Supplier <Type> falseValue) {

		if (condition) {

			return trueValue.get ();

		} else {

			return falseValue.get ();

		}

	}

	public static <Type>
	Type ifThenElse (
			@NonNull Supplier <Boolean> condition0,
			@NonNull Supplier <Type> condition0Value,
			@NonNull Supplier <Boolean> condition1,
			@NonNull Supplier <Type> condition1Value,
			@NonNull Supplier <Type> elseValue) {

		if (condition0.get ()) {

			return condition0Value.get ();

		} else if (condition1.get ()) {

			return condition1Value.get ();

		} else {

			return elseValue.get ();

		}

	}

	public static <ReturnType>
	ReturnType ifNotNullThenElse (
			Object notNullObject,
			@NonNull Supplier <ReturnType> trueValue,
			@NonNull Supplier <ReturnType> falseValue) {

		if (
			isNotNull (
				notNullObject)
		) {

			return trueValue.get ();

		} else {

			return falseValue.get ();

		}

	}

	public static
	void ifNotNullThenElse (
			Object notNullObject,
			@NonNull Runnable trueStatement,
			@NonNull Runnable falseStatement) {

		if (
			isNotNull (
				notNullObject)
		) {

			trueStatement.run ();

		} else {

			falseStatement.run ();

		}

	}

	public static <ReturnType>
	ReturnType ifNotNullThenElseNull (
			Object notNullObject,
			@NonNull Supplier <ReturnType> trueValue) {

		if (
			isNotNull (
				notNullObject)
		) {

			return trueValue.get ();

		} else {

			return null;

		}

	}

	public static
	String ifThenElseEmDash (
			@NonNull Boolean condition,
			@NonNull Supplier <String> trueValue) {

		if (condition) {

			return trueValue.get ();

		} else {

			return "—";

		}

	}

	public static
	String ifNotNullThenElseEmDash (
			Object notNullObject,
			@NonNull Supplier <String> trueValue) {

		if (
			isNotNull (
				notNullObject)
		) {

			return trueValue.get ();

		} else {

			return "—";

		}

	}

	public static
	String ifNullThenEmDash (
			String notNullString) {

		if (
			isNotNull (
				notNullString)
		) {

			return notNullString;

		} else {

			return "—";

		}

	}

	public static <ReturnType>
	ReturnType ifNotEmptyThenElse (
			Collection <?> notEmptyCollection,
			@NonNull Supplier <ReturnType> trueSupplier,
			@NonNull Supplier <ReturnType> falseSupplier) {

		if (
			collectionIsEmpty (
				notEmptyCollection)
		) {

			return trueSupplier.get ();

		} else {

			return falseSupplier.get ();

		}

	}

	public static
	Runnable ifNotEmptyThenElse (
			Collection <?> notEmptyCollection,
			@NonNull Runnable trueStatement,
			@NonNull Runnable falseStatement) {

		if (
			collectionIsNotEmpty (
				notEmptyCollection)
		) {

			return trueStatement;

		} else {

			return falseStatement;

		}

	}

	public static
	String ifNotEmptyThenElseEmDash (
			Collection <?> notEmptyCollection,
			@NonNull Supplier <String> trueValue) {

		if (
			collectionIsEmpty (
				notEmptyCollection)
		) {

			return trueValue.get ();

		} else {

			return "—";

		}

	}

	public static
	boolean noneIsTrue (
			List<Boolean> values) {

		for (
			Boolean value
				: values
		) {

			if (value)
				return false;

		}

		return true;

	}

	public static
	boolean equalSafe (
			@NonNull Object object0,
			@NonNull Object object1) {

		if (object0.getClass () != object1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					object0.getClass ().getSimpleName (),
					object1.getClass ().getSimpleName ()));

		}

		return object0.equals (
			object1);

	}

	public static
	boolean notEqualSafe (
			@NonNull Object object0,
			@NonNull Object object1) {

		if (object0.getClass () != object1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					object0.getClass ().getSimpleName (),
					object1.getClass ().getSimpleName ()));

		}

		return ! object0.equals (
			object1);

	}

	public static <Type>
	boolean equalWithClass (
			@NonNull Class <Type> objectClass,
			@NonNull Type object0,
			@NonNull Type object1) {

		return object0.equals (
			object1);

	}

	public static <Type>
	boolean notEqualWithClass (
			@NonNull Class <Type> objectClass,
			@NonNull Type object0,
			@NonNull Type object1) {

		return ! object0.equals (
			object1);

	}

	public static <Type>
	boolean referenceEqualUnsafe (
			@NonNull Type object1,
			@NonNull Type object2) {

		return object1 == object2;

	}

	public static <Type>
	boolean referenceNotEqualUnsafe (
			@NonNull Type object1,
			@NonNull Type object2) {

		return object1 != object2;

	}

	public static <Type>
	boolean referenceEqualSafe (
			@NonNull Object object0,
			@NonNull Object object1) {

		if (object0.getClass () != object1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					object0.getClass ().getSimpleName (),
					object1.getClass ().getSimpleName ()));

		}

		return object0 == object1;

	}

	public static
	boolean referenceNotEqualSafe (
			@NonNull Object object0,
			@NonNull Object object1) {

		if (object0.getClass () != object1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					object0.getClass ().getSimpleName (),
					object1.getClass ().getSimpleName ()));

		}

		return object0 != object1;

	}

	public static <Type>
	boolean referenceEqualWithClass (
			@NonNull Class <?> objectClass,
			@NonNull Type object1,
			@NonNull Type object2) {

		return object1 == object2;

	}

	public static <Type>
	boolean referenceNotEqualWithClass (
			@NonNull Class <?> objectClass,
			@NonNull Type object1,
			@NonNull Type object2) {

		return object1 != object2;

	}

	public static
	boolean booleanEqual (
			boolean boolean0,
			boolean boolean1) {

		return boolean0 == boolean1;

	}

	public static
	boolean booleanNotEqual (
			boolean boolean0,
			boolean boolean1) {

		return boolean0 != boolean1;

	}

	public static
	Optional <Boolean> parseBooleanYesNoEmpty (
			@NonNull String string) {

		if (string.isEmpty ()) {
			return Optional.absent ();
		}

		if (
			string.equals (
				"yes")
		) {

			return Optional.of (
				true);

		}

		if (
			string.equals (
				"no")
		) {

			return Optional.of (
				false);

		}

		throw new IllegalArgumentException (
			stringFormat (
				"Unexpected value for boolean: \"%s\"",
				string));

	}

	public static
	Optional <Boolean> parseBooleanYesNoNone (
			@NonNull String string) {

		if (
			string.equals (
				"none")
		) {
			return Optional.absent ();
		}

		if (
			string.equals (
				"yes")
		) {
			return Optional.of (
				true);
		}

		if (
			string.equals (
				"no")
		) {

			return Optional.of (
				false);

		}

		throw new IllegalArgumentException (
			stringFormat (
				"Unexpected value for boolean: \"%s\"",
				string));

	}

	public static
	Boolean parseBooleanYesNoRequired (
			@NonNull String string) {

		if (
			string.equals (
				"yes")
		) {
			return true;
		}

		if (
			string.equals (
				"no")
		) {
			return false;
		}

		throw new IllegalArgumentException ();

	}

	public static
	Optional <Boolean> parseBooleanTrueFalse (
			@NonNull String string) {

		if (string.isEmpty ()) {
			return Optional.absent ();
		}

		if (
			string.equals (
				"true")
		) {

			return Optional.of (
				true);

		}

		if (
			string.equals (
				"false")
		) {

			return Optional.of (
				false);

		}

		throw new IllegalArgumentException ();

	}

	public static
	boolean parseBooleanTrueFalseRequired (
			@NonNull String string) {

		if (
			string.equals (
				"true")
		) {
			return true;
		}

		if (
			string.equals (
				"false")
		) {
			return false;
		}

		throw new IllegalArgumentException ();

	}

	public static
	String booleanToString (
			@NonNull Optional <Boolean> valueOptional,
			@NonNull String yesString,
			@NonNull String noString,
			@NonNull String notPresentString) {

		if (
			optionalIsPresent (
				valueOptional)
		) {

			return ifThenElse (
				valueOptional.get (),
				() -> yesString,
				() -> noString);

		} else {

			return notPresentString;

		}

	}

	public static
	String booleanToString (
			@NonNull Boolean value,
			@NonNull String yesString,
			@NonNull String noString) {

		return ifThenElse (
			value,
			() -> yesString,
			() -> noString);

	}

	public static
	String booleanToYesNo (
			@NonNull Boolean value) {

		return booleanToString (
			value,
			"yes",
			"no");

	}

	public static
	String booleanToYesNoNone (
			@NonNull Optional <Boolean> valueOptional) {

		return booleanToString (
			valueOptional,
			"yes",
			"no",
			"none");

	}

	public static
	String booleanToTrueFalse (
			@NonNull Boolean value) {

		return booleanToString (
			value,
			"true",
			"false");

	}

	public static
	String booleanToTrueFalseNone (
			@NonNull Optional <Boolean> valueOptional) {

		return booleanToString (
			valueOptional,
			"true",
			"false",
			"none");

	}

	public static
	String booleanToOneZero (
			@NonNull Boolean value) {

		return booleanToString (
			value,
			"1",
			"0");

	}

	public static
	enum Ordering {
		less,
		equal,
		more;
	}

	public static <ComparableType extends Comparable <ComparableType>>
	Ordering comparableCompare (
			@NonNull ComparableType left,
			@NonNull ComparableType right) {

		int rawComparison =
			left.compareTo (
				right);

		if (rawComparison < 0) {
			return Ordering.less;
		} else if (rawComparison > 0) {
			return Ordering.more;
		} else {
			return Ordering.equal;
		}

	}

	public static <ComparableType extends Comparable <ComparableType>>
	boolean comparableLessThan (
			@NonNull ComparableType left,
			@NonNull ComparableType right) {

		return enumEqualSafe (
			comparableCompare (
				left,
				right),
			Ordering.less);

	}

	public static <ComparableType extends Comparable <ComparableType>>
	boolean comparableMoreThan (
			@NonNull ComparableType left,
			@NonNull ComparableType right) {

		return enumEqualSafe (
			comparableCompare (
				left,
				right),
			Ordering.more);

	}

	public static <ComparableType extends Comparable <ComparableType>>
	boolean comparableNotLessThan (
			@NonNull ComparableType left,
			@NonNull ComparableType right) {

		return enumNotEqualSafe (
			comparableCompare (
				left,
				right),
			Ordering.less);

	}

	public static <ComparableType extends Comparable <ComparableType>>
	boolean comparableNotMoreThan (
			@NonNull ComparableType left,
			@NonNull ComparableType right) {

		return enumNotEqualSafe (
			comparableCompare (
				left,
				right),
			Ordering.more);

	}

	public static
	Function <Boolean, Boolean> booleanInverseFunction () {
		return value -> ! value;
	}

	public static <Type>
	Type attemptWithRetries (
			@NonNull Long maxAttempts,
			@NonNull Duration backoffTime,
			@NonNull Supplier <Type> task,
			@NonNull BiConsumer <Long, Exception> retryExceptionHandler,
			@NonNull BiConsumer <Long, Exception> finalExceptionHandler)
		throws InterruptedException {

		for (
			long attempt = 0l;
			attempt < maxAttempts;
			attempt ++
		) {

			if (attempt < maxAttempts - 1) {

				try {

					return task.get ();

				} catch (Exception exception) {

					retryExceptionHandler.accept (
						attempt,
						exception);
				}

				Duration pause =
					backoffTime.multipliedBy (
						attempt);

				Thread.sleep (
					pause.getMillis ());

			} else {

				try {

					return task.get ();

				} catch (RuntimeException exception) {

					finalExceptionHandler.accept (
						attempt,
						exception);

					throw exception;

				} catch (Exception exception) {

					finalExceptionHandler.accept (
						attempt,
						exception);

					throw new RuntimeException (
						exception);

				}

			}

		}

		throw shouldNeverHappen ();

	}

	public static
	void attemptWithRetriesVoid (
			@NonNull Long maxAttempts,
			@NonNull Duration backoffTime,
			@NonNull Runnable task,
			@NonNull BiConsumer <Long, Exception> retryExceptionHandler,
			@NonNull BiConsumer <Long, Exception> finalExceptionHandler)
		throws InterruptedException {

		for (
			long attempt = 0l;
			attempt < maxAttempts;
			attempt ++
		) {

			if (attempt < maxAttempts - 1) {

				try {

					task.run ();

					return;

				} catch (Exception exception) {

					retryExceptionHandler.accept (
						attempt,
						exception);
				}

				Duration pause =
					backoffTime.multipliedBy (
						attempt);

				Thread.sleep (
					pause.getMillis ());

			} else {

				try {

					task.run ();

					return;

				} catch (RuntimeException exception) {

					finalExceptionHandler.accept (
						attempt,
						exception);

					throw exception;

				} catch (Exception exception) {

					finalExceptionHandler.accept (
						attempt,
						exception);

					throw new RuntimeException (
						exception);

				}

			}

		}

		throw shouldNeverHappen ();

	}

}
