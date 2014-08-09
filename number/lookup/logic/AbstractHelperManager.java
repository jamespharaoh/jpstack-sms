package wbs.sms.number.lookup.logic;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.extern.log4j.Log4j;

import com.google.common.collect.ImmutableMap;

@Log4j
public abstract
class AbstractHelperManager<HelperType extends Helper> {

	// state

	Map<String,HelperType> byParentObjectTypeCode;

	// extension points

	public abstract
	String friendlyName ();

	public abstract
	Map<String,HelperType> helpersByBeanName ();

	// implementation

	@PostConstruct
	public
	void init () {

		int errorCount = 0;

		Map<String,String> beanNamesByParentTypeCode =
			new HashMap<String,String> ();

		ImmutableMap.Builder<String,HelperType>
		byParentObjectTypeCodeBuilder =
			ImmutableMap.<String,HelperType>builder ();

		for (Map.Entry<String,HelperType> helperEntry
				: helpersByBeanName ().entrySet ()) {

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
						capitalise (friendlyName ()),
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
					errorCount));

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
