package wbs.sms.messageset.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.sms.messageset.model.MessageSetRec;

@Accessors (fluent = true)
@PrototypeComponent ("simpleMessageSetFinder")
public
class SimpleMessageSetFinder
	implements MessageSetFinder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageSetConsoleHelper messageSetHelper;

	@SingletonDependency
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	ObjectLookup<?> objectLookup;

	@Getter @Setter
	String code;

	// implementation

	@Override
	public
	MessageSetRec findMessageSet (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleRequestContext requestContext) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findMessageSet");

		) {

			Record <?> object =
				genericCastUnchecked (
					objectLookup.lookupObject (
						transaction,
						requestContext.consoleContextStuffRequired ()));

			return messageSetHelper.findByCodeRequired (
				transaction,
				object,
				code);

		}

	}

}
