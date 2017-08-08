package wbs.utils.etc;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.NonNull;

import fj.data.Either;

public
class ResultUtils {

	// result factories

	public static <LeftType, RightType>
	Either <LeftType, RightType> successResult (
			@NonNull LeftType left) {

		return Either.<LeftType, RightType> left (
			left);

	}

	public static <LeftType, RightType>
	Either <Optional <LeftType>, RightType> successResultAbsent () {

		return Either.<Optional <LeftType>, RightType> left (
			Optional.absent ());

	}

	public static <LeftType, RightType>
	Either <Optional <LeftType>, RightType> successResultPresent (
			@NonNull LeftType left) {

		return Either.<Optional <LeftType>, RightType> left (
			Optional.of (
				left));

	}

	public static <LeftType, RightType>
	Either <LeftType, RightType> errorResult (
			@NonNull RightType right) {

		return Either.<LeftType, RightType>right (
			right);

	}

	public static <LeftType>
	Either <LeftType, String> errorResultFormat (
			@NonNull String ... arguments) {

		return Either.<LeftType, String> right (
			stringFormatArray (
				arguments));

	}

	// success result accessors

	public static
	boolean isSuccess (
			@NonNull Either<?,?> either) {

		return either.isLeft ();

	}

	public static <Type>
	Optional <Type> resultValue (
			@NonNull Either <Type, ?> either) {

		return either.isLeft ()
			? optionalOf (
				either.left ().value ())
			: optionalAbsent ();

	}

	public static <Type>
	Type resultValueRequired (
			@NonNull Either <Type, ?> either) {

		return either.left ().value ();

	}

	public static <OldValueType, NewValueType, ErrorType>
	Either <NewValueType, ErrorType> mapSuccess (
			@NonNull Either <OldValueType, ErrorType> result,
			@NonNull Function <
				? super OldValueType,
				? extends NewValueType
			> mapping) {

		if (result.isLeft ()) {

			return Either.left (
				mapping.apply (
					result.left ().value ()));

		} else {

			return errorResult (
				result.right ().value ());

		}

	}

	// error result accessors

	public static <Type>
	Type getError (
			@NonNull Either<?,Type> either) {

		return either.right ().value ();

	}

	public static
	boolean isError (
			@NonNull Either<?,?> either) {

		return either.isRight ();

	}

}
