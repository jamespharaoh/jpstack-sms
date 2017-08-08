package wbs.utils.etc;

import static wbs.utils.collection.ArrayUtils.arrayStream;
import static wbs.utils.collection.IterableUtils.iterableFilterMap;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;
import static wbs.utils.etc.TypeUtils.isInstanceOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import wbs.utils.data.Pair;

public
class OptionalUtils {

	public static <Type>
	Optional <Type> optionalFromJava (
			java.util.Optional <? extends Type> javaOptional) {

		if (javaOptional.isPresent ()) {

			return Optional.of (
				javaOptional.get ());

		} else {

			return Optional.absent ();

		}

	}

	public static
	boolean optionalEqualAndPresentSafe (
			Optional <?> optional0,
			Optional <?> optional1) {

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return false;
		}

		Object value0 =
			optionalGetRequired (
				optional0);

		Object value1 =
			optionalGetRequired (
				optional1);

		if (value0.getClass () != value1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					value0.getClass ().getSimpleName (),
					value1.getClass ().getSimpleName ()));

		}

		return value0.equals (
			value1);

	}

	public static
	boolean optionalNotEqualAndPresentSafe (
			Optional <?> optional0,
			Optional <?> optional1) {

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return true;
		}

		Object value0 =
			optionalGetRequired (
				optional0);

		Object value1 =
			optionalGetRequired (
				optional1);

		if (value0.getClass () != value1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					value0.getClass ().getSimpleName (),
					value1.getClass ().getSimpleName ()));

		}

		return ! value0.equals (
			value1);

	}

	public static <Type>
	boolean optionalEqualAndPresentWithClass (
			Class <Type> valueClass,
			Optional <Type> optional0,
			Optional <Type> optional1) {

		if (
			optional0.isPresent ()
			&& ! valueClass.isInstance (
				optional0.get ())
		) {

			Object value0 =
				optionalGetRequired (
					optional0);

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					value0.getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			optional1.isPresent ()
			&& ! valueClass.isInstance (
				optional1.get ())
		) {

			Object value1 =
				optionalGetRequired (
					optional1);

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					value1.getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return false;
		}

		Object value0 =
			optionalGetRequired (
				optional0);

		Object value1 =
			optionalGetRequired (
				optional1);

		return value0.equals (
			value1);

	}

	public static <Type>
	boolean optionalNotEqualAndPresentWithClass (
			Class <Type> valueClass,
			Optional <Type> optional0,
			Optional <Type> optional1) {

		if (
			optional0.isPresent ()
			&& ! valueClass.isInstance (
				optional0.get ())
		) {

			Object value0 =
				optionalGetRequired (
					optional0);

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					value0.getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			optional1.isPresent ()
			&& ! valueClass.isInstance (
				optional1.get ())
		) {

			Object value1 =
				optionalGetRequired (
					optional1);

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					value1.getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return true;
		}

		Object value0 =
			optionalGetRequired (
				optional0);

		Object value1 =
			optionalGetRequired (
				optional1);

		return ! value0.equals (
			value1);

	}

	public static
	boolean optionalEqualOrNotPresentSafe (
			Optional <?> optional0,
			Optional <?> optional1) {

		if (
			! optional0.isPresent ()
			&& ! optional1.isPresent ()
		) {
			return true;
		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return false;
		}

		Object value0 =
			optionalGetRequired (
				optional0);

		Object value1 =
			optionalGetRequired (
				optional1);

		if (value0.getClass () != value1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					value0.getClass ().getSimpleName (),
					value1.getClass ().getSimpleName ()));

		}

		return value0.equals (
			value1);

	}

	public static
	boolean optionalNotEqualOrNotPresentSafe (
			Optional <?> optional0,
			Optional <?> optional1) {

		if (
			! optional0.isPresent ()
			&& ! optional1.isPresent ()
		) {
			return false;
		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return true;
		}

		Object value0 =
			optionalGetRequired (
				optional0);

		Object value1 =
			optionalGetRequired (
				optional1);

		if (value0.getClass () != value1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					value0.getClass ().getSimpleName (),
					value1.getClass ().getSimpleName ()));

		}

		return ! value0.equals (
			value1);

	}

	public static <Type>
	boolean optionalEqualOrNotPresentWithClass (
			Class <Type> valueClass,
			Optional <? extends Type> optional0,
			Optional <? extends Type> optional1) {

		// verify class instances

		if (
			optional0.isPresent ()
			&& ! valueClass.isInstance (
				optional0.get ())
		) {

			Object value0 =
				optionalGetRequired (
					optional0);

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					value0.getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			optional1.isPresent ()
			&& ! valueClass.isInstance (
				optional1.get ())
		) {

			Object value1 =
				optionalGetRequired (
					optional1);

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					value1.getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		// handle not present

		if (
			! optional0.isPresent ()
			&& ! optional1.isPresent ()
		) {
			return true;
		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return false;
		}

		// regular equals

		Object value0 =
			optionalGetRequired (
				optional0);

		Object value1 =
			optionalGetRequired (
				optional1);

		return value0.equals (
			value1);

	}

	public static <Type>
	boolean optionalNotEqualOrNotPresentWithClass (
			Class <Type> valueClass,
			Optional <Type> optional0,
			Optional <Type> optional1) {

		// verify class instances

		if (
			optional0.isPresent ()
			&& ! valueClass.isInstance (
				optional0.get ())
		) {

			Object value0 =
				optionalGetRequired (
					optional0);

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					value0.getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			optional1.isPresent ()
			&& ! valueClass.isInstance (
				optional1.get ())
		) {

			Object value1 =
				optionalGetRequired (
					optional1);

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					value1.getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		// handle not present

		if (
			! optional0.isPresent ()
			&& ! optional1.isPresent ()
		) {
			return false;
		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return true;
		}

		// regular equals

		Object value0 =
			optionalGetRequired (
				optional0);

		Object value1 =
			optionalGetRequired (
				optional1);

		return ! value0.equals (
			value1);

	}

	public static <Type>
	boolean optionalValueEqualSafe (
			Optional <Type> optional,
			Type value) {

		if (! optional.isPresent ()) {
			return false;
		}

		if (optional.get ().getClass () != value.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional.get ().getClass ().getSimpleName (),
					value.getClass ().getSimpleName ()));

		}

		return optional.get ().equals (
			value);

	}

	public static <Type>
	boolean optionalValueNotEqualSafe (
			Optional <Type> optional,
			Type value) {

		if (! optional.isPresent ()) {
			return true;
		}

		if (optional.get ().getClass () != value.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional.get ().getClass ().getSimpleName (),
					value.getClass ().getSimpleName ()));

		}

		return ! optional.get ().equals (
			value);

	}

	public static <Type>
	boolean optionalValueEqualWithClass (
			Class <Type> valueClass,
			Optional <Type> optional,
			Type value) {

		// verify class instances

		if (
			optional.isPresent ()
			&& ! valueClass.isInstance (
				optional.get ())
		) {
			throw new ClassCastException ();
		}

		if (
			! valueClass.isInstance (
				value)
		) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional.get ().getClass ().getSimpleName (),
					value.getClass ().getSimpleName ()));

		}

		// handle not present

		if (! optional.isPresent ()) {
			return false;
		}

		// regular equals

		return optional.get ().equals (
			value);

	}

	public static <Type>
	boolean optionalValueNotEqualWithClass (
			Class <Type> valueClass,
			Optional <Type> optional,
			Type value) {

		// verify class instances

		if (
			optional.isPresent ()
			&& ! valueClass.isInstance (
				optional.get ())
		) {
			throw new ClassCastException ();
		}

		if (
			! valueClass.isInstance (
				value)
		) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional.get ().getClass ().getSimpleName (),
					value.getClass ().getSimpleName ()));

		}

		// handle not present

		if (! optional.isPresent ()) {
			return true;
		}

		// regular equals

		return ! optional.get ().equals (
			value);

	}

	public static
	boolean optionalIsPresent (
			Optional <?> optional) {

		return optional.isPresent ();

	}

	public static
	boolean optionalIsNotPresent (
			Optional <?> optional) {

		return ! optional.isPresent ();

	}

	@NonNull
	public static <Type>
	Type optionalGetRequired (
			Optional <Type> optional) {

		Type value =
			optional.orNull ();

		if (value == null) {
			throw new NullPointerException ();
		}

		return value;

	}

	public static <Type>
	Optional <Type> optionalGetOrAbsent (
			Optional <Optional <Type>> optional) {

		return optional.isPresent ()
			? optional.get ()
			: Optional.absent ();

	}

	public static <Type>
	Type optionalOrNull (
			Optional <Type> optional) {

		return optional.orNull ();

	}

	public static <Type>
	Type optionalOrElseRequired (
			Optional <Type> optional,
			Supplier <Type> orElse) {

		if (optional.isPresent ()) {

			return optional.get ();

		} else {

			return orElse.get ();

		}

	}

	public static <Type>
	Optional <Type> optionalOrElseOptional (
			Optional <Type> optional,
			Supplier <Optional <Type>> orElse) {

		if (optional.isPresent ()) {

			return optional;

		} else {

			return orElse.get ();

		}

	}

	public static <Type>
	Type optionalOrThrow (
			Optional <Type> optional,
			Supplier <RuntimeException> exceptionSupplier) {

		if (optional.isPresent ()) {

			return optional.get ();

		} else {

			throw exceptionSupplier.get ();

		}

	}

	public static <Type>
	Optional <Type> requiredOptional (
			Optional <Type> optional) {

		if (! optional.isPresent ()) {
			throw new RuntimeException ();
		}

		return optional;

	}

	public static <Type>
	Iterable <Type> presentInstances (
			Iterable <Optional <Type>> collection) {

		return Optional.presentInstances (
			collection);

	}

	public static <KeyType, ValueType>
	Iterator <? extends Map.Entry <KeyType, ValueType>> presentInstances (
			Map <KeyType, Optional <ValueType>> map) {

		return map.entrySet ().stream ()

			.filter (
				entry ->
					entry.getValue ().isPresent ())

			.map (
				entry ->
					new AbstractMap.SimpleEntry <KeyType, ValueType> (
						entry.getKey (),
						entry.getValue ().get ()))

			.iterator ();

	}

	public static <Type>
	Iterable <Type> presentInstances () {

		return ImmutableList.of ();

	}

	public static <Type>
	Iterable <Type> presentInstances (
			Optional <Type> argument) {

		return presentInstances (
			ImmutableList.of (
				argument));

	}

	public static <Type>
	Iterable <Type> presentInstances (
			Optional <Type> argument0,
			Optional <Type> argument1) {

		return presentInstances (
			ImmutableList.of (
				argument0,
				argument1));

	}

	public static <Type>
	Iterable <Type> presentInstances (
			Optional <Type> argument0,
			Optional <Type> argument1,
			Optional <Type> argument2) {

		return presentInstances (
			ImmutableList.of (
				argument0,
				argument1,
				argument2));

	}

	public static <Type>
	Iterable <Type> presentInstances (
			Optional <Type> argument0,
			Optional <Type> argument1,
			Optional <Type> argument2,
			Optional <Type> argument3) {

		return presentInstances (
			ImmutableList.of (
				argument0,
				argument1,
				argument2,
				argument3));

	}

	@SafeVarargs
	public static <Type>
	Iterable <Type> presentInstances (
			Optional <Type> ... arguments) {

		return Optional.presentInstances (
			Arrays.asList (
				arguments));

	}

	@SafeVarargs
	public static <Type>
	Iterable <Type> presentInstances (
			Supplier <Optional <? extends Type>> ... suppliers) {

		return () ->
			arrayStream (
				suppliers)

			.map (
				Supplier::get)

			.filter (
				Optional::isPresent)

			.map (
				valueOptional ->
					(Type)
					optionalGetRequired (
						valueOptional))

			.iterator ()

		;

	}

	public static <Type>
	List <Type> presentInstancesList () {

		return ImmutableList.of ();

	}

	public static <Type>
	List <Type> presentInstancesList (
			Optional <Type> argument) {

		return ImmutableList.copyOf (
			presentInstances (
				ImmutableList.of (
					argument)));

	}

	public static <Type>
	List <Type> presentInstancesList (
			Optional <Type> argument0,
			Optional <Type> argument1) {

		return ImmutableList.copyOf (
			presentInstances (
				ImmutableList.of (
					argument0,
					argument1)));

	}

	public static <Type>
	List <Type> presentInstancesList (
			Optional <Type> argument0,
			Optional <Type> argument1,
			Optional <Type> argument2) {

		return ImmutableList.copyOf (
			presentInstances (
				ImmutableList.of (
					argument0,
					argument1,
					argument2)));

	}

	public static <Type>
	List <Type> presentInstancesList (
			Optional <Type> argument0,
			Optional <Type> argument1,
			Optional <Type> argument2,
			Optional <Type> argument3) {

		return ImmutableList.copyOf (
			presentInstances (
				ImmutableList.of (
					argument0,
					argument1,
					argument2,
					argument3)));

	}

	@SafeVarargs
	public static <Type>
	List <Type> presentInstancesList (
			Optional <Type> ... arguments) {

		return ImmutableList.copyOf (
			Optional.presentInstances (
				Arrays.asList (
					arguments)));

	}

	public static <Type>
	List <Type> presentInstancesList (
			Iterable <Optional <Type>> arguments) {

		return ImmutableList.copyOf (
			Optional.presentInstances (
				arguments));

	}

	public static <Type>
	Set <Type> presentInstancesSet (
			Iterable <Optional <Type>> arguments) {

		return ImmutableSet.copyOf (
			Optional.presentInstances (
				arguments));

	}

	@SafeVarargs
	public static <Key, Value>
	Map <Key, Value> presentInstancesMap (
			Pair <Key, Optional <Value>> ... arguments) {

		return ImmutableMap.copyOf (
			iterableFilterMap (
				Arrays.asList (
					arguments),
				(left, right) ->
					right.isPresent (),
				(left, right) ->
					new AbstractMap.SimpleEntry <Key, Value> (
						left,
						right.get ())));

	}

	@SafeVarargs
	public static <Type>
	Type [] presentInstancesArray (
			Class <Type> itemClass,
			Optional <Type> ... arguments) {

		return Iterables.toArray (
			Optional.presentInstances (
				Arrays.asList (
					arguments)),
			itemClass);

	}

	public static <Type>
	Optional <Type> optionalIf (
			Boolean present,
			Supplier <Type> valueSupplier) {

		return present
			? Optional.of (
				valueSupplier.get ())
			: Optional.absent ();

	}

	public static <Type>
	Optional <Type> optionalIfPresent (
			Optional <?> optional,
			Supplier <Type> valueSupplier) {

		if (optional.isPresent ()) {

			return optionalOf (
				valueSupplier.get ());

		} else {

			return optionalAbsent ();

		}

	}

	public static <T>
	T optionalOr (
			Optional <T> optional,
			T instead) {

		return optional.or (
			instead);

	}

	public static
	String optionalOrEmptyString (
			Optional <String> optional) {

		return optional.or (
			"");

	}

	@SafeVarargs
	public static <Type>
	Type ifNotPresent (
			Optional <Type>... optionalValues) {

		for (
			Optional <Type> optionalValue
				: optionalValues
		) {

			if (
				optionalIsPresent (
					optionalValue)
			) {

				return optionalValue.get ();

			}

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			Optional<? extends Type> optionalValueOne) {

		if (
			optionalIsPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			Optional <? extends Type> optionalValueOne,
			Optional <? extends Type> optionalValueTwo) {

		if (
			optionalIsPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		if (
			optionalIsPresent (
				optionalValueTwo)
		) {

			return optionalValueTwo.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			Optional <? extends Type> optionalValueOne,
			Optional <? extends Type> optionalValueTwo,
			Optional <? extends Type> optionalValueThree) {

		if (
			optionalIsPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		if (
			optionalIsPresent (
				optionalValueTwo)
		) {

			return optionalValueTwo.get ();

		}

		if (
			optionalIsPresent (
				optionalValueThree)
		) {

			return optionalValueThree.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Optional <Type> optionalCast (
			Class <Type> classToCastTo,
			Optional <?> optionalValue) {

		if (
			optionalIsPresent (
				optionalValue)
		) {

			Object value =
				optionalGetRequired (
					optionalValue);

			if (
				isInstanceOf (
					classToCastTo,
					value)
			) {

				return Optional.of (
					dynamicCastRequired (
						classToCastTo,
						optionalValue.get ()));

			} else {

				throw new ClassCastException (
					stringFormat (
						"Cannot cast %s to %s",
						classNameFull (
							value.getClass ()),
						classNameFull (
							classToCastTo)));

			}

		} else {

			return Optional.absent ();

		}

	}

	public static <From, To>
	Optional <To> optionalMapRequired (
			Optional <From> optionalValue,
			Function <? super From, To> mappingFunction) {

		if (
			optionalIsPresent (
				optionalValue)
		) {

			return Optional.of (
				mappingFunction.apply (
					optionalValue.get ()));

		} else {

			return Optional.absent ();

		}

	}

	public static <From, @Nullable To>
	To optionalMapRequiredOrNull (
			Optional <From> optionalValue,
			Function <? super From, To> mappingFunction) {

		if (
			optionalIsPresent (
				optionalValue)
		) {

			return mappingFunction.apply (
				optionalValue.get ());

		} else {

			return null;

		}

	}

	public static <From,To>
	Optional <To> optionalMapOptional (
			Optional<From> optionalValue,
			Function<? super From,Optional<To>> mappingFunction) {

		if (
			optionalIsPresent (
				optionalValue)
		) {

			return mappingFunction.apply (
				optionalValue.get ());

		} else {

			return Optional.absent ();

		}

	}

	public static <FromType, ToType>
	ToType optionalMapRequiredOrDefault (
			Function <? super FromType, ? extends ToType> mappingFunction,
			Optional <FromType> optionalValue,
			ToType defaultValue) {

		if (optionalValue.isPresent ()) {

			return mappingFunction.apply (
				optionalValue.get ());

		} else {

			return defaultValue;

		}

	}

	public static <From, To>
	To optionalMapRequiredOrThrow (
			Optional <From> optionalValue,
			Function <? super From, ? extends To> mappingFunction,
			Supplier <? extends RuntimeException> orThrow) {

		if (optionalValue.isPresent ()) {

			return mappingFunction.apply (
				optionalValue.get ());

		} else {

			throw orThrow.get ();

		}

	}

	public static <Type>
	Optional <Type> optionalAbsent () {

		return Optional.absent ();

	}

	public static <Type>
	Optional <Type> optionalOf (
			Type value) {

		return Optional.of (
			value);

	}

	public static
	Optional <String> optionalOfFormat (
			String ... arguments) {

		return Optional.of (
			stringFormatArray (
				arguments));

	}

	public static <Type>
	Optional <@NonNull Type> optionalFromNullable (
			Type value) {

		if (value == null) {

			return Optional.absent ();

		} else {

			return Optional.of (
				value);

		}

	}

	public static <Type>
	Type ifPresentThenElse (
			Optional <?> optional,
			Supplier <Type> trueSupplier,
			Supplier <Type> falseSupplier) {

		if (optional.isPresent ()) {

			return trueSupplier.get ();

		} else {

			return falseSupplier.get ();

		}

	}

	public static <Type>
	void optionalDo (
			Optional <Type> optional,
			Consumer <Type> consumer) {

		if (optional.isPresent ()) {

			consumer.accept (
				optional.get ());

		}

	}

	public static
	Optional <Exception> optionalCatchException (
			@NonNull Runnable runnable) {

		try {

			runnable.run ();

			return optionalAbsent ();

		} catch (Exception exception) {

			return optionalOf (
				exception);

		}

	}

}
