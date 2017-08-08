package wbs.utils.collection;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.objectToString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.utils.data.Pair;

public
class MapUtils {

	// constructors

	public static <KeyType, ValueType>
	Map <KeyType, ValueType> immutableMap (
			@NonNull Iterator <
				? extends Map.Entry <
					? extends KeyType,
					? extends ValueType
				>
			> iterator) {

		ImmutableMap.Builder <KeyType, ValueType> builder =
			ImmutableMap.builder ();

		while (iterator.hasNext ()) {

			Map.Entry <
				? extends KeyType,
				? extends ValueType
			> entry =
				iterator.next ();

			builder.put (
				new AbstractMap.SimpleEntry <KeyType, ValueType> (
					entry.getKey (),
					entry.getValue ()));

		}

		return builder.build ();

	}

	// empty

	public static
	boolean mapIsEmpty (
			@NonNull Map <?, ?> map) {

		return map.isEmpty ();

	}

	public static
	boolean mapIsNotEmpty (
			@NonNull Map <?, ?> map) {

		return ! map.isEmpty ();

	}

	public static <KeyType, ValueType>
	Map <KeyType, ValueType> emptyMap () {

		return ImmutableMap.of ();

	}

	// key lookup

	public static <KeyType>
	boolean mapContainsKey (
			@NonNull Map <KeyType, ?> map,
			@NonNull KeyType key) {

		return map.containsKey (
			key);

	}

	public static <KeyType>
	boolean mapDoesNotContainKey (
			@NonNull Map <KeyType, ?> map,
			@NonNull KeyType key) {

		return ! map.containsKey (
			key);

	}

	// item lookup

	public static <KeyType, ValueType>
	Optional <ValueType> mapItemForKey (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key) {

		ValueType value =
			map.get (
				key);

		if (value != null) {

			return optionalOf (
				value);

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			return optionalAbsent ();

		}

	}

	public static <KeyType, ValueType>
	ValueType mapItemForKeyOrDefault (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key,
			@NonNull ValueType defaultValue) {

		ValueType value =
			map.get (
				key);

		if (value != null) {

			return value;

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			return defaultValue;

		}

	}

	public static <KeyType, ValueType>
	ValueType mapItemForKeyOrElse (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key,
			@NonNull Supplier <ValueType> elseSupplier) {

		ValueType value =
			map.get (
				key);

		if (value != null) {

			return value;

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			return elseSupplier.get ();

		}

	}

	public static <KeyType, ValueType>
	ValueType mapItemForKeyOrElseSet (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key,
			@NonNull Supplier <ValueType> defaultValueSupplier) {

		ValueType value =
			map.get (
				key);

		if (value != null) {

			return value;

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			value =
				defaultValueSupplier.get ();

			map.put (
				key,
				value);

			return value;

		}

	}

	public static <KeyType, ValueType>
	ValueType mapItemForKeyRequired (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key) {

		ValueType value =
			map.get (
				key);

		if (value != null) {

			return value;

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else if (
			key instanceof CharSequence
			|| key instanceof Number
			|| key instanceof Boolean
		) {

			throw new NoSuchElementException (
				stringFormat (
					"No such element: %s",
					objectToString (
						key)));

		} else {

			throw new NoSuchElementException ();

		}

	}

	public static <Type>
	Type mapItemForKeyOrKey (
			@NonNull Map <Type, Type> map,
			@NonNull Type key) {

		Type value =
			map.get (
				key);

		if (value != null) {

			return value;

		} else if (map.containsKey (key)) {

			throw new NullPointerException ();

		} else {

			return key;

		}

	}

	public static <KeyType, ValueType>
	ValueType mapItemForKeyOrThrow (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key,
			@NonNull Supplier <RuntimeException> exceptionSupplier) {

		ValueType value =
			map.get (
				key);


		if (value != null) {

			return value;

		} else if (
			map.containsKey (
				key)
		) {

			throw new NullPointerException ();

		} else {

			throw exceptionSupplier.get ();

		}

	}

	public static <KeyType, ValueType>
	Map <KeyType, Optional <ValueType>> mapItemsForKeys (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull Iterable <KeyType> keys) {

		ImmutableMap.Builder <KeyType, Optional <ValueType>> builder =
			ImmutableMap.builder ();

		for (
			KeyType key
				: keys
		) {

			builder.put (
				key,
				mapItemForKey (
					map,
					key));

		}

		return builder.build ();

	}

	public static <Key, Value>
	Map <Key, Value> mapItemsForKeysRequired (
			@NonNull Map <Key, Value> map,
			@NonNull Iterable <Key> keys) {

		ImmutableMap.Builder <Key, Value> builder =
			ImmutableMap.builder ();

		for (
			Key key
				: keys
		) {

			builder.put (
				key,
				mapItemForKeyRequired (
					map,
					key));

		}

		return builder.build ();

	}

	public static <KeyType, ValueType>
	Map <KeyType, ValueType> mapWithDerivedKey (
			@NonNull Iterable <ValueType> values,
			@NonNull Function <
				? super ValueType,
				? extends KeyType
			> keyFunction) {

		ImmutableMap.Builder <KeyType, ValueType> builder =
			ImmutableMap.builder ();

		for (
			ValueType value
				: values
		) {

			builder.put (
				keyFunction.apply (
					value),
				value);

		}

		return builder.build ();

	}

	public static <KeyType, ValueType>
	Map <KeyType, ValueType> mapWithDerivedKey (
			@NonNull ValueType [] values,
			@NonNull Function <
				? super ValueType,
				? extends KeyType
			> keyFunction) {

		return mapWithDerivedKey (
			Arrays.asList (
				values),
			keyFunction);

	}

	public static <KeyType, InValueType, OutValueType>
	Map <KeyType, OutValueType> mapWithDerivedKeyAndValue (
			@NonNull Iterable <? extends InValueType> values,
			@NonNull Function <
				? super InValueType,
				? extends KeyType
			> keyFunction,
			@NonNull Function <
				? super InValueType,
				? extends OutValueType
			> valueFunction) {

		ImmutableMap.Builder <KeyType, OutValueType> builder =
			ImmutableMap.builder ();

		for (
			InValueType value
				: values
		) {

			builder.put (
				keyFunction.apply (
					value),
				valueFunction.apply (
					value));

		}

		return builder.build ();

	}

	public static <KeyType, ValueType>
	void mapPutOrThrowIllegalStateException (
			@NonNull Map <KeyType, ValueType> map,
			@NonNull KeyType key,
			@NonNull ValueType value) {

		if (
			map.containsKey (
				key)
		) {
			throw new IllegalStateException ();
		}

		map.put (
			key,
			value);

	}

	public static <Key, Value>
	Map <Key, Value> mapFilterByKeyToMap (
			@NonNull Map <Key, Value> input,
			@NonNull Predicate <Key> predicate) {

		ImmutableMap.Builder <Key, Value> builder =
			ImmutableMap.builder ();

		for (
			Map.Entry <Key, Value> entry
				: input.entrySet ()
		) {

			if (
				! predicate.test (
					entry.getKey ())
			) {
				continue;
			}

			builder.put (
				entry);

		}

		return builder.build ();

	}

	public static <Key, Value>
	List <Value> mapFilterByKeyToList (
			@NonNull Map <Key, Value> input,
			@NonNull Predicate <Key> predicate) {

		ImmutableList.Builder <Value> builder =
			ImmutableList.builder ();

		for (
			Map.Entry <Key, Value> entry
				: input.entrySet ()
		) {

			if (
				! predicate.test (
					entry.getKey ())
			) {
				continue;
			}

			builder.add (
				entry.getValue ());

		}

		return builder.build ();

	}

	public static <KeyLeft, KeyRight, Value>
	List <Value> mapFilterByKeyToList (
			@NonNull Map <Pair <KeyLeft, KeyRight>, Value> input,
			@NonNull BiPredicate <KeyLeft, KeyRight> predicate) {

		ImmutableList.Builder <Value> builder =
			ImmutableList.builder ();

		for (
			Map.Entry <Pair <KeyLeft, KeyRight>, Value> entry
				: input.entrySet ()
		) {

			if (
				! predicate.test (
					entry.getKey ().left (),
					entry.getKey ().right ())
			) {
				continue;
			}

			builder.add (
				entry.getValue ());

		}

		return builder.build ();

	}

	// transform

	public static <InKey, InValue, OutKey, OutValue>
	Map <OutKey, OutValue> mapTransformToMap (
			@NonNull Map <InKey, InValue> inMap,
			@NonNull BiFunction <InKey, InValue, OutKey> keyFunction,
			@NonNull BiFunction <InKey, InValue, OutValue> valueFunction) {

		ImmutableMap.Builder <OutKey, OutValue> outMapBuilder =
			ImmutableMap.builder ();

		for (
			Map.Entry <InKey, InValue> inEntry
				: inMap.entrySet ()
		) {

			outMapBuilder.put (
				keyFunction.apply (
					inEntry.getKey (),
					inEntry.getValue ()),
				valueFunction.apply (
					inEntry.getKey (),
					inEntry.getValue ()));

		}

		return outMapBuilder.build ();

	}

	public static <InItem, OutKey, OutValue>
	Map <OutKey, OutValue> iterableTransformToMap (
			@NonNull Iterable <InItem> inIterable,
			@NonNull Function <InItem, OutKey> keyFunction,
			@NonNull Function <InItem, OutValue> valueFunction) {

		ImmutableMap.Builder <OutKey, OutValue> outMapBuilder =
			ImmutableMap.builder ();

		for (
			InItem inItem
				: inIterable
		) {

			outMapBuilder.put (
				keyFunction.apply (
					inItem),
				valueFunction.apply (
					inItem));

		}

		return outMapBuilder.build ();

	}

	public static <InLeft, InRight, OutKey, OutValue>
	Map <OutKey, OutValue> iterableTransformToMap (
			@NonNull Iterable <Pair <InLeft, InRight>> inIterable,
			@NonNull BiFunction <InLeft, InRight, OutKey> keyFunction,
			@NonNull BiFunction <InLeft, InRight, OutValue> valueFunction) {

		ImmutableMap.Builder <OutKey, OutValue> outMapBuilder =
			ImmutableMap.builder ();

		for (
			Pair <InLeft, InRight> inPair
				: inIterable
		) {

			outMapBuilder.put (
				keyFunction.apply (
					inPair.left (),
					inPair.right ()),
				valueFunction.apply (
					inPair.left (),
					inPair.right ()));

		}

		return outMapBuilder.build ();

	}

	// properties

	public static
	Properties mapToProperties (
			@NonNull Map <String, String> propertiesMap) {

		Properties properties =
			new Properties ();

		properties.putAll (
			propertiesMap);

		return properties;

	}

}
