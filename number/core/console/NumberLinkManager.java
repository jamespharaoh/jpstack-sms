package wbs.sms.number.core.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("numberLinkManager")
public
class NumberLinkManager {

	@Inject
	List<NumberPlugin> numberPlugins =
		Collections.emptyList ();

	/**
	 * Calls findAdvicesByNumberId() on all available modules and aggregates the
	 * result.
	 *
	 * @param numberId
	 *            the number id to search for
	 * @param active
	 *            if true only return advice for active subscriptions
	 * @return the aggregates results
	 */
	public
	List<NumberPlugin.Link> findLinks (
			NumberRec number,
			boolean active) {

		List<NumberPlugin.Link> links =
			new ArrayList<NumberPlugin.Link> ();

		for (
			NumberPlugin numberPlugin
				: numberPlugins
		) {

			links.addAll (
				numberPlugin.findLinks (
					number,
					active));

		}

		return links;

	}

}
