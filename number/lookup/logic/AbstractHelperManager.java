package wbs.sms.number.lookup.logic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.InterfaceHelper;

@Log4j
public abstract
class AbstractHelperManager <HelperType extends InterfaceHelper> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// state

	Map <String, HelperType> byParentObjectTypeCode;

	// extension points

	public abstract
	String friendlyName ();

	public abstract
	Map <String, HelperType> helpersByBeanName ();

	// implementation

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

			int errorCount = 0;

			Map <String, String> beanNamesByParentTypeCode =
				new HashMap<> ();

			ImmutableMap.Builder <String, HelperType>
			byParentObjectTypeCodeBuilder =
				ImmutableMap.builder ();

			for (
				Map.Entry <String, HelperType> helperEntry
					: helpersByBeanName ().entrySet ()
			) {

				String beanName =
					helperEntry.getKey ();

				HelperType helper =
					helperEntry.getValue ();

				String parentObjectTypeCode =
					helper.parentObjectTypeCode ();

				// make sure they're unique

				if (beanNamesByParentTypeCode.containsKey (
						parentObjectTypeCode)) {

					log.error (
						stringFormat (
							"%s helper for %s from both %s and %s",
							capitalise (
								friendlyName ()),
							parentObjectTypeCode,
							beanNamesByParentTypeCode.get (
								parentObjectTypeCode),
							beanName));

					errorCount ++;

					continue;

				}

				// add it to the list

				byParentObjectTypeCodeBuilder.put (
					parentObjectTypeCode,
					helper);

			}

			// abort if there were errors

			if (errorCount > 0) {

				throw new RuntimeException (
					stringFormat (
						"Aborting due to %s errors",
						integerToDecimalString (
							errorCount)));

			}

			byParentObjectTypeCode =
				byParentObjectTypeCodeBuilder.build ();

		}

	}

	public
	HelperType forParentObjectTypeCode (
			String parentObjectTypeCode,
			boolean required) {

		HelperType helper =
			byParentObjectTypeCode.get (
				parentObjectTypeCode);

		if (
			required
			&& helper == null
		) {

			throw new RuntimeException (
				stringFormat (
					"No %s helper for parent object type %s",
					parentObjectTypeCode));

		}

		return helper;

	}

}
