package wbs.sms.number.core.console;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("numberLinkManager")
public
class NumberLinkManager {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	List <NumberPlugin> numberPlugins;

	// implementation

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
	List <NumberPlugin.Link> findLinks (
			@NonNull Transaction parentTransaction,
			@NonNull NumberRec number,
			boolean active) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLinks");

		) {

			List <NumberPlugin.Link> links =
				new ArrayList<> ();

			for (
				NumberPlugin numberPlugin
					: numberPlugins
			) {

				links.addAll (
					numberPlugin.findLinks (
						transaction,
						number,
						active));

			}

			return links;

		}

	}

}
