package wbs.sms.number.core.console;

import java.util.Date;
import java.util.List;

import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

public
interface NumberPlugin {

	String getName ();

	List<Link> findLinks (
			NumberRec number,
			boolean active);

	interface Link {

		NumberPlugin getProvider ();
		NumberRec getNumber ();
		Boolean getActive ();
		Date getStartTime ();
		Date getEndTime ();
		Record<?> getParentObject ();
		Record<?> getSubscriptionObject ();
		String getType ();

		boolean canView ();

	}

}
