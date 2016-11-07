package wbs.sms.number.lookup.logic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.NormalLifecycleSetup;

@Log4j
public abstract
class AbstractHelperManager <HelperType extends Helper> {

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
	void init () {

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
