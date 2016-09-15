package wbs.sms.route.router.logic;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@SingletonComponent ("routerHelperManager")
public
class RouterHelperManager {

	// collection dependencies

	@SingletonDependency
	Map <String, RouterHelper> routerHelpersByBeanName;

	// state

	Map <String, RouterHelper> byParentObjectTypeCode;

	// implementation

	@NormalLifecycleSetup
	public
	void init () {

		int errorCount = 0;

		Map <String, String> beanNamesByTypeCode =
			new HashMap<> ();

		ImmutableMap.Builder <String, RouterHelper>
		byParentObjectTypeCodeBuilder =
			ImmutableMap.builder ();

		for (
			Map.Entry <String, RouterHelper> routerHelperEntry
				: routerHelpersByBeanName.entrySet ()
		) {

			String beanName =
				routerHelperEntry.getKey ();

			RouterHelper routerHelper =
				routerHelperEntry.getValue ();

			String routerTypeCode =
				routerHelper.routerTypeCode ();

			// make sure they're unique

			if (beanNamesByTypeCode.containsKey (
					routerTypeCode)) {

				log.error (
					stringFormat (
						"Router type helper for %s from both %s and %s",
						routerTypeCode,
						beanNamesByTypeCode.get (
							routerTypeCode),
						beanName));

				errorCount ++;

				continue;

			}

			// add it to the list

			byParentObjectTypeCodeBuilder.put (
				routerTypeCode,
				routerHelper);

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
	RouterHelper forParentObjectTypeCode (
			String routerTypeCode,
			boolean required) {

		RouterHelper routerHelper =
			byParentObjectTypeCode.get (
				routerTypeCode);

		if (
			required
			&& routerHelper == null
		) {

			throw new RuntimeException (
				stringFormat (
					"No router helper for router type %s",
					routerTypeCode));

		}

		return routerHelper;

	}

}
