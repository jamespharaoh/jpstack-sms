package wbs.utils.time.core;

import lombok.NonNull;

import org.joda.time.format.DateTimeFormat;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("defaultTimeFormatter")
public
class DefaultTimeFormatterFactory
	implements ComponentFactory <DefaultTimeFormatter> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	DefaultTimeFormatter makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			TimeFormatterPlugin plugin =
				new TimeFormatterPluginImplementation ()

				.name (
					"default")

				// timestamp

				.timestampSecondFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH:mm:ss"))

				.timestampMinuteFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH:mm"))

				.timestampHourFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH"))

				// timestamp timezone

				.timestampTimezoneSecondFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH:mm:ss ZZ"))

				.timestampTimezoneMinuteFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH:mm ZZ"))

				.timestampTimezoneHourFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH ZZ"))

				// timestamp timezone short

				.timestampTimezoneSecondShortFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH:mm:ss zzz"))

				.timestampTimezoneMinuteShortFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH:mm zzz"))

				.timestampTimezoneHourShortFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH zzz"))

				// timestamp timezone

				.timestampTimezoneSecondLongFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH:mm:ss zzzz"))

				.timestampTimezoneMinuteLongFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH:mm zzzz"))

				.timestampTimezoneHourLongFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd HH zzzz"))

				// date

				.longDateFormat (
					DateTimeFormat.forPattern (
						"EEEE, d MMMM yyyy"))

				.shortDateFormat (
					DateTimeFormat.forPattern (
						"yyyy-MM-dd"))

				// time

				.timeFormat (
					DateTimeFormat.forPattern (
						"HH:mm:SS"))

				// timezone

				.timezoneLongFormat (
					DateTimeFormat.forPattern (
						"ZZZZ"))

				.timezoneShortFormat (
					DateTimeFormat.forPattern (
						"ZZZ"))

			;

			return new DefaultTimeFormatter () {

				@Override
				public
				TimeFormatterPlugin plugin () {
					return plugin;
				}

			};

		}

	}

}
