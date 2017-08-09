package wbs.sms.number.core.console;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import wbs.sms.number.core.model.NumberRec;

public
interface NumberPlugin {

	String getName ();

	List <Link> findLinks (
			Transaction parentTransaction,
			NumberRec number,
			boolean active);

	public
	interface Link {

		NumberPlugin getProvider ();
		NumberRec getNumber ();
		Boolean getActive ();
		Instant getStartTime ();
		Instant getEndTime ();
		Record <?> getParentObject ();
		Record <?> getSubscriptionObject ();
		String getType ();

		boolean canView (
				Transaction parentTransaction);

	}

}
