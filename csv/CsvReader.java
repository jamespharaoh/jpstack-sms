package wbs.utils.csv;

import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.IterableUtils.iterableMapWithIndexToMap;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class CsvReader {

	// properties

	@Getter @Setter
	Boolean skipHeader = false;

	@Getter @Setter
	Boolean trim = true;

	// public implementation

	public
	Iterable <List <String>> readAsList (
			@NonNull BufferedReader reader) {

		return () ->
			reader.lines ()

			.skip (
				skipHeader ? 1 : 0)

			.filter (
				line ->
					stringIsNotEmpty (
						line))

			.map (
				line ->
					readLineAsList (
						line))

			.iterator ();

	}

	public
	Iterable <Map <String, String>> readAsMap (
			@NonNull List <String> columnNames,
			@NonNull BufferedReader reader) {

		return iterableMap (
			readAsList (
				reader),
			lineList ->
				iterableMapWithIndexToMap (
					lineList,
					(index, value) ->
						listItemAtIndexRequired (
							columnNames,
							index),
					(index, value) ->
						value));

	}

	public
	List <String> readLineAsList (
			@NonNull String line) {

		ImmutableList.Builder <String> columns =
			ImmutableList.builder ();

		StringBuilder column =
			new StringBuilder ();

		boolean quoted = false;

		int lastCharacter = -1;

		for (
			int position = 0;
			position < line.length ();
			position += Character.charCount (lastCharacter)
		) {

			int character =
				line.codePointAt (
					position);

			if (! quoted) {

				if (character == '"') {

					quoted = true;

				} else if (character == ',') {

					if (trim) {

						columns.add (
							stringTrim (
								column.toString ()));

					} else {

						columns.add (
							column.toString ());

					}

					column =
						new StringBuilder ();

				} else {

					column.appendCodePoint (
						character);

				}

			} else {

				if (character == '"') {

					quoted = false;

					if (lastCharacter == '"') {

						column.appendCodePoint (
							'"');

					}

				} else {

					column.appendCodePoint (
						character);
				}

			}

			lastCharacter =
				character;

		}

		if (quoted) {
			throw new IllegalArgumentException ();
		}

		if (trim) {

			columns.add (
				stringTrim (
					column.toString ()));

		} else {

			columns.add (
				column.toString ());

		}

		return columns.build ();

	}

	public static
	Iterable <Integer> stringCharactersIterable (
			@NonNull String string) {

		return () ->
			string.chars ().iterator ();

	}

}
