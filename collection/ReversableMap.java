package wbs.utils.collection;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Reversable map which exists in pairs, each mapping the opposite direction.
 * Duplicated inserts are not allowed (although I can think of better semantics
 * here). Also entrySet (), keys () and values () all return unmodifiable set
 * (because I'm too lazy to implement this).
 */
public
class ReversableMap<KeyType,ValueType>
	implements Map<KeyType,ValueType> {

	private
	Map<KeyType,ValueType> map;

	private
	ReversableMap<ValueType,KeyType> reverseMap;

	private
	ReversableMap () {
	}

	public static <KeyType,ValueType>
	ReversableMap<KeyType,ValueType> make (
			Map<KeyType,ValueType> map1,
			Map<ValueType,KeyType> map2) {

		ReversableMap<KeyType,ValueType> forwardMap =
			new ReversableMap<KeyType,ValueType> ();

		ReversableMap<ValueType,KeyType> reverseMap =
			new ReversableMap<ValueType,KeyType> ();

		forwardMap.map = map1;
		forwardMap.reverseMap = reverseMap;

		reverseMap.map = map2;
		reverseMap.reverseMap = forwardMap;

		return forwardMap;

	}

	public static <KeyType,ValueType>
	ReversableMap<KeyType,ValueType> makeHashed () {

		return make (
			new HashMap<KeyType,ValueType> (),
			new HashMap<ValueType,KeyType> ());

	}

	@Override
	public
	void clear () {

		map.clear ();
		reverseMap.map.clear ();

	}

	@Override
	public
	boolean containsKey (
			Object key) {

		return map.containsKey (
			key);

	}

	@Override
	public
	boolean containsValue (
			Object value) {

		return map.containsValue (
			value);

	}

	@Override
	public
	Set<Map.Entry<KeyType,ValueType>> entrySet () {

		return Collections.unmodifiableSet (
			map.entrySet ());

	}

	@Override
	public
	boolean equals (
			Object object) {

		return map.equals (
			object);

	}

	@Override
	public
	ValueType get (
			Object key) {

		return map.get (
			key);

	}

	public
	KeyType getKey (
			Object value) {

		return reverseMap.map.get (
			value);

	}

	@Override
	public
	int hashCode () {

		return map.hashCode ();

	}

	@Override
	public
	boolean isEmpty () {

		return map.isEmpty ();

	}

	@Override
	public
	Set<KeyType> keySet () {

		return Collections.unmodifiableSet (
			map.keySet ());

	}

	@Override
	public
	ValueType put (
			KeyType key,
			ValueType value) {

		if (map.containsKey (value)) {

			throw new IllegalArgumentException (
				stringFormat (
					"Attempt to insert duplicate key: %s",
					key.toString ()));

		}

		if (reverseMap.map.containsKey (value)) {

			throw new IllegalArgumentException (
				stringFormat (
					"Attempt to insert duplicate value: %s",
					value.toString ()));

		}

		map.put (
			key,
			value);

		reverseMap.map.put (
			value,
			key);

		return value;

	}

	@Override
	public
	void putAll (
			Map<? extends KeyType,? extends ValueType> source) {

		// check for dupes first

		for (
			Map.Entry<? extends KeyType, ? extends ValueType> entry
				: source.entrySet ()
		) {

			if (map.containsKey (
					entry.getKey ())) {

				throw new IllegalArgumentException (
					stringFormat (
						"Attempt to insert duplicate key: %s",
						entry.getKey ().toString ()));

			}

			if (reverseMap.map.containsKey (
					entry.getValue ())) {

				throw new IllegalArgumentException (
					stringFormat (
						"Attempt to insert duplicate value: %s",
						entry.getValue ().toString ()));

			}

		}

		// then insert

		for (Map.Entry<? extends KeyType,? extends ValueType> entry
				: source.entrySet ()) {

			map.put (
				entry.getKey (),
				entry.getValue ());

			reverseMap.map.put (
				entry.getValue (),
				entry.getKey ());

		}

	}

	@Override
	public
	ValueType remove (
			Object key) {

		ValueType value =
			map.remove (key);

		reverseMap.map.remove (value);

		return value;

	}

	@Override
	public
	int size () {

		return map.size ();

	}

	@Override
	public
	Collection<ValueType> values () {

		return Collections.unmodifiableCollection (
			map.values ());

	}

	public
	Map<ValueType,KeyType> reverse () {

		return reverseMap;

	}

	public
	Set<ValueType> valueSet () {

		return reverseMap.keySet ();

	}

}
