package wbs.sms.message.core.logic;

import lombok.NonNull;

import org.joda.time.DateTime;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.sms.message.core.model.MessageRec;

public
class MessageHooks
	implements ObjectHooks<MessageRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull MessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			// set date

			if (message.getDate () == null) {

				if (message.getCreatedTime () == null)
					throw new RuntimeException ();

				message.setDate (
					new DateTime (
						message.getCreatedTime ()
					).toLocalDate ());

			}

		}

	}

}