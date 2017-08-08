package wbs.utils.etc;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.genericCastUncheckedNullSafe;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import org.joda.time.Duration;

import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

import fj.data.Either;

// TODO lots to deprecate here
public
class Misc {

	private final static
	Pattern intPattern =
		Pattern.compile ("[0-9]+");

	@Deprecated
	public static
	boolean isInt (
			String string) {

		if (string == null)
			return false;

		return intPattern
			.matcher (string)
			.matches ();

	}

	public static <Type extends Enum <Type>>
	Optional <Type> toEnum (
			Class <Type> enumType,
			String name) {

		if (name.length () == 0) {
			return optionalAbsent ();
		}

		if (
			stringEqualSafe (
				name,
				"null")
		) {
			return null;
		}

		return optionalOf (
			Enum.valueOf (
				enumType,
				name));

	}

	public static <Type extends Enum <Type>>
	Type toEnumRequired (
			Class <Type> enumType,
			String name) {

		return optionalGetRequired (
			toEnum (
				enumType,
				name));

	}

	public static <Type extends Enum <Type>>
	Type toEnumOrNull (
			Class <Type> enumType,
			String name) {

		return optionalOrNull (
			toEnum (
				enumType,
				name));

	}

	public static
	Optional <Enum <?>> toEnumGeneric (
			Class <?> uncastEnumType,
			String name) {

		return optionalFromNullable (
			genericCastUncheckedNullSafe (
				Enum.valueOf (
					genericCastUnchecked (
						uncastEnumType),
					name)));

	}

	public static
	RuntimeException rethrow (
			Throwable throwable) {

		if (throwable instanceof Error) {
			throw (Error) throwable;
		}

		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		}

		throw new RuntimeException (
			throwable);

	}

	public static
	int min (
			int ... params) {

		int ret =
			params [0];

		for (int param : params) {

			if (param < ret)
				ret = param;

		}

		return ret;

	}

	public static
	long min (
			long ... params) {

		long ret =
			params [0];

		for (
			long param
				: params
		) {

			if (param < ret)
				ret = param;

		}

		return ret;

	}

	public static
	long max (
			long ... params) {

		long ret =
			params [0];

		for (
			long param
				: params
		) {

			if (param > ret)
				ret = param;

		}

		return ret;

	}

	public static
	long max (
			Iterable <Integer> params) {

		Iterator <Integer> iterator =
			params.iterator ();

		long value =
			iterator.next ();

		while (iterator.hasNext ()) {

			Integer param =
				iterator.next ();

			if (param > value) {
				value = param;
			}

		}

		return value;

	}

	public static
	long max (
			Integer ... params) {

		long ret =
			params [0];

		for (
			long param
				: params
		) {

			if (param > ret) {
				ret = param;
			}

		}

		return ret;

	}

	public static
	String prettyHour (
			long hour) {

		if (hour < 0 || hour > 23)
			throw new IllegalArgumentException ();

		if (hour == 0)
			return "12am";

		if (hour < 12)
			return "" + hour + "am";

		if (hour == 12)
			return "12pm";

		return "" + (hour - 12) + "pm";

	}

	public static <T>
	Iterable<T> iterable (
			final Iterator<T> iterator) {

		return new Iterable<T> () {

			@Override
			public
			Iterator<T> iterator () {
				return iterator;
			}

		};

	}

	static
	public class TempFile
		implements Closeable {

		private final
		File file;

		private
		TempFile (
				@NonNull String prefix,
				@NonNull String extension) {

			try {

				this.file =
					File.createTempFile (
						"wbs-temp-",
						extension);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		public
		File file () {
			return file;
		}

		public
		String path () {
			return file.getPath ();
		}

		@Override
		public
		void close () {

			file.delete ();

		}

	}

	public static
	TempFile createTempFile (
			@NonNull String extension) {

		return new TempFile (
			"wbs-temp-",
			extension);

	}

	public static
	TempFile createTempFileWithData (
			String extension,
			byte[] data) {

		TempFile tempFile =
			new TempFile (
				"wbs-temp-",
				extension);

		boolean success = false;

		try (

			OutputStream outputStream =
				new FileOutputStream (
					tempFile.file ());

		) {

			success = true;

			return tempFile;

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		} finally {

			if (! success) {
				tempFile.close ();
			}

		}

	}

	public static
	int runCommand (
			TaskLogger taskLogger,
			List <String> command)
		throws InterruptedException {

		taskLogger.noticeFormat (
			"Executing %s",
			joinWithSpace (
				command));

		try {

			Process process =
				Runtime.getRuntime ().exec (
					command.toArray (
						new String [] {}));

			// debug log any error output

			try (

				BufferedReader bufferedReader =
					new BufferedReader (
						new InputStreamReader (
							process.getErrorStream (),
							"utf-8"));

			) {

				String line;

				while ((line = bufferedReader.readLine ()) != null) {

					taskLogger.debugFormat (
						"%s",
						line);

				}

			}

			try {

				return process.waitFor ();

			} catch (InterruptedException exception) {

				process.destroy ();

				throw exception;

			}

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	byte[] runFilter (
			TaskLogger taskLogger,
			byte[] data,
			String inExt,
			String outExt,
			String ... command)
		throws InterruptedException {

		return runFilterAdvanced (
			taskLogger,
			data,
			inExt,
			outExt,
			ImmutableList.of (
				ImmutableList.copyOf (
					command)));

	}

	public static
	byte[] runFilterAdvanced (
			TaskLogger taskLogger,
			byte[] data,
			String inExtension,
			String outExtension,
			List <List <String>> commands)
		throws InterruptedException {

		// create the input and output files

		try (

			TempFile inFile =
				createTempFileWithData (
					inExtension,
					data);

			TempFile outFile =
				createTempFile (
					outExtension);

		) {

			// stick the filenames into the command

			for (
				List <String> command
					: commands
			) {

				List <String> newCommand =
					command.stream ()

					.map (
						commandItem ->
							ifThenElse (
								stringEqualSafe (
									commandItem,
									"<in>"),
								() -> inFile.path (),
								() -> commandItem))

					.map (
						commandItem ->
							ifThenElse (
								stringEqualSafe (
									commandItem,
									"<out>"),
								() -> outFile.path (),
								() -> commandItem))

					.collect (
						Collectors.toList ());

				// run the command

				int status =
					runCommand (
						taskLogger,
						newCommand);

				if (status != 0)
					throw new RuntimeException ("Command returned " + status);

			}

			// read the output file

			try {

				return FileUtils.readFileToByteArray (
					outFile.file ());

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

	}

	private static final
	char[] lowercaseLetters =
		new char [26];

	private static final
	char[] digits =
		new char [10];

	static {

		for (
			int index = 0;
			index < 26;
			index ++
		) {

			lowercaseLetters [index] =
				(char) ('a' + index);

		}

		for (
			int index = 0;
			index < 10;
			index ++
		) {

			digits [index] =
				(char) ('0' + index);

		}

	}

	public static
	String prettySize (
			int bytes) {

		int limit = 2;

		if (bytes < limit * 1024)
			return "" + bytes + " bytes";

		if (bytes < limit * 1024 * 1024)
			return "" + (bytes / 1024) + " kilobytes";

		if (bytes < limit * 1024 * 1024 * 1024)
			return "" + (bytes / 1024 / 1024) + " megabytes";

		return "" + (bytes / 1024 / 1024 / 1024) + " gigabytes";

	}

	public static
	RuntimeException todo () {
		return new RuntimeException ("TODO");
	}

	public static
	RuntimeException todo (
			@NonNull CharSequence message) {

		return new RuntimeException (
			stringFormat (
				"TODO %s",
				message));

	}

	public static
	RuntimeException todoFormat (
			@NonNull CharSequence ... messageArguments) {

		return new RuntimeException (
			stringFormat (
				"TODO %s",
				stringFormatArray (
					messageArguments)));

	}

	public static
	String hashSha1Base64 (
			String string) {

		try {

			MessageDigest messageDigest =
				MessageDigest.getInstance (
					"SHA-1");

			messageDigest.update (
				string.getBytes ());

			return Base64.encodeBase64String (
				messageDigest.digest ());

		} catch (NoSuchAlgorithmException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	@Deprecated
	public static
	Boolean stringToBoolean (
			@NonNull String string,
			@NonNull String yesString,
			@NonNull String noString,
			@NonNull String nullString) {

		if (
			stringEqualSafe (
				string,
				yesString)
		) {
			return true;
		}

		if (
			stringEqualSafe (
				string,
				noString)
		) {
			return false;
		}

		if (
			stringEqualSafe (
				string,
				nullString)
		) {
			return null;
		}

		throw new RuntimeException (
			stringFormat (
				"Invalid boolean string: \"%s\"",
				string));

	}

	public static
	void doNothing () {

	}

	public static <Type>
	List <Type> maybeList (
			List<Type> value) {

		return value != null
			? value
			: Collections.<Type>emptyList ();

	}

	public static <Type>
	List <Type> maybeList (
			boolean condition,
			Type value) {

		return condition

			? Collections.<Type>singletonList (
				value)

			: Collections.<Type>emptyList ();

	}

	public static
	boolean lessThan (
			int left,
			int right) {

		return left < right;

	}

	public static
	boolean lessThan (
			long left,
			long right) {

		return left < right;

	}

	public static
	int sum (
			int value0,
			int value1) {

		return value0 + value1;

	}

	public static
	long sum (
			long value0,
			long value1) {

		return value0 + value1;

	}

	public static
	long minus (
			long left,
			long right) {

		return left - right;

	}

	public static
	long sum (
			long value0,
			long value1,
			long value2) {

		return value0 + value1 + value2;

	}

	public static
	long sum (
			long value0,
			long value1,
			long value2,
			long value3) {

		return value0 + value1 + value2 + value3;

	}

	public static
	long sum (
			long value0,
			long value1,
			long value2,
			long value3,
			long value4) {

		return value0 + value1 + value2 + value3 + value4;

	}

	public static
	URL stringToUrl (
			String urlString) {

		try {

			return new URL (
				urlString);

		} catch (MalformedURLException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	boolean doesNotImplement (
			@NonNull Class<?> subjectClass,
			@NonNull Class<?> implementedClass) {

		return ! implementedClass.isAssignableFrom (
			subjectClass);

	}

	public static <Type>
	boolean contains (
			@NonNull Collection <Type> collection,
			@NonNull Type value) {

		return collection.contains (
			value);

	}

	public static <KeyType>
	boolean contains (
			@NonNull Map <KeyType, ?> map,
			@NonNull KeyType value) {

		return map.keySet ().contains (
			value);

	}

	public static <Type>
	boolean doesNotContain (
			@NonNull Collection <Type> collection,
			@NonNull Type value) {

		return ! collection.contains (
			value);

	}

	public static <KeyType>
	boolean doesNotContain (
			@NonNull Map <KeyType, ?> map,
			@NonNull KeyType value) {

		return ! map.containsKey (
			value);

	}

	public static <Type>
	Optional<Integer> indexOf (
			@NonNull List<Type> list,
			@NonNull Type value) {

		int index =
			list.indexOf (
				value);

		if (index < 0) {

			return Optional.<Integer>absent ();

		} else {

			return Optional.of (
				index);

		}

	}

	public static
	String fullClassName (
			@NonNull Class<?> theClass) {

		return theClass.getName ();

	}

	public static
	Optional<String> fullClassName (
			@NonNull Optional<Class<?>> theClass) {

		return theClass.isPresent ()
			? Optional.<String>of (
				theClass.get ().getName ())
			: Optional.<String>absent ();

	}

	public static <Type>
	Type requiredValue (
			@NonNull Type value) {

		return value;

	}

	public static
	String stringTrim (
			@NonNull String source) {

		return source.trim ();

	}

	public static <Type>
	Type eitherGetLeft (
			@NonNull Either<Type,?> either) {

		return either.left ().value ();

	}

	public static <Type>
	Type eitherGetRight (
			@NonNull Either<?,Type> either) {

		return either.right ().value ();

	}

	public static
	boolean isLeft (
			@NonNull Either<?,?> either) {

		return either.isLeft ();

	}

	public static
	boolean isRight (
			@NonNull Either<?,?> either) {

		return either.isRight ();

	}

	public static <ValueType, ErrorType>
	ValueType successOrElse (
			@NonNull Either <ValueType, ErrorType> result,
			@NonNull Function <ErrorType, ValueType> orElse) {

		if (result.isLeft ()) {

			return result.left ().value ();

		} else {

			return orElse.apply (
				result.right ().value ());

		}

	}

	public static <ValueType, ErrorType>
	ValueType successOrThrow (
			@NonNull Either <ValueType, ErrorType> result,
			@NonNull Function <ErrorType, RuntimeException> orThrow) {

		if (result.isLeft ()) {

			return result.left ().value ();

		} else {

			throw orThrow.apply (
				result.right ().value ());

		}

	}

	public static <ValueType>
	ValueType successOrThrowRuntimeException (
			@NonNull Either <ValueType, String> result) {

		if (result.isLeft ()) {

			return result.left ().value ();

		} else {

			throw new RuntimeException (
				result.right ().value ());

		}

	}

	public static <Key,Value>
	Map.Entry<Key,Value> mapEntry (
			@NonNull Key key,
			@NonNull Value value) {

		return new AbstractMap.SimpleEntry<Key,Value> (
			key,
			value);

	}

	public static
	RuntimeException shouldNeverHappen () {

		throw new RuntimeException (
			"Should never happen");

	}

	public static
	RuntimeException shouldNeverHappenFormat (
			@NonNull String ... arguments) {

		throw new RuntimeException (
			stringFormatArray (
				arguments));

	}

	public static
	boolean disabled () {
		return true;
	}

	public static <First, Second>
	BiConsumer <First, Second> castToConsumer (
			@NonNull BiFunction <First, Second, ?> function) {

		return (first, second) -> {

			function.apply (
				first,
				second);

		};

	}

	public static <Argument>
	Consumer <Argument> castToConsumer (
			@NonNull Function <Argument, ?> function) {

		return argument -> {

			function.apply (
				argument);

		};

	}

	public static
	void castToVoid (
			Object object) {

		doNothing ();

	}

	public static
	void sleepForDuration (
			@NonNull Duration duration)
		throws InterruptedException {

		Thread.sleep (
			duration.getMillis ());

	}

	public static
	UnsupportedOperationException unsupportedOperation (
			@NonNull Object object,
			@NonNull String methodName) {

		return new UnsupportedOperationException (
			stringFormat (
				"%s.%s",
				classNameFull (
					object.getClass ()),
				methodName));

	}

}
