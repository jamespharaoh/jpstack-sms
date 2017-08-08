package wbs.services.messagetemplate.console;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.notEqualToZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.web.responder.WebResponder;

@SingletonComponent ("messageTemplateSetSettingsCreateMissingFormActionHelper")
public
class MessageTemplateSetSettingsCreateMissingFormActionHelper
	implements ConsoleFormActionHelper <Object, Object> {

	// singleton dependencies

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateSetConsoleHelper messageTemplateSetHelper;

	@SingletonDependency
	MessageTemplateEntryValueConsoleHelper messageTemplateEntryValueHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	Optional <WebResponder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull Object state) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			MessageTemplateSetRec set =
				messageTemplateSetHelper.findFromContextRequired (
					transaction);

			MessageTemplateDatabaseRec database =
				set.getMessageTemplateDatabase ();

			long numCreated = 0;

			for (
				MessageTemplateEntryTypeRec entryType
					: database.getMessageTemplateEntryTypes ()
			) {

				Optional <MessageTemplateEntryValueRec> existingEntryOptional =
					mapItemForKey (
						set.getMessageTemplateEntryValues (),
						entryType.getId ());

				if (
					optionalIsPresent (
						existingEntryOptional)
				) {

					MessageTemplateEntryValueRec existingEntry =
						optionalGetRequired (
							existingEntryOptional);

					if (existingEntry.getDeleted ()) {

						existingEntry

							.setDeleted (
								false)

						;

						eventLogic.createEvent (
							transaction,
							"admin_object_field_updated",
							userConsoleLogic.userRequired (
								transaction),
							"deleted",
							existingEntry,
							false);

						numCreated ++;

					}

				} else {

					MessageTemplateEntryValueRec newEntry =
						messageTemplateEntryValueHelper.insert (
							transaction,
							messageTemplateEntryValueHelper.createInstance ()

						.setMessageTemplateSet (
							set)

						.setMessageTemplateEntryType (
							entryType)

						.setDeleted (
							false)

					);

					eventLogic.createEvent (
						transaction,
						"admin_object_created",
						userConsoleLogic.userRequired (
							transaction),
						newEntry,
						set);

					numCreated ++;

				}

			}

			transaction.commit ();

			if (
				notEqualToZero (
					numCreated)
			) {

				requestContext.addNoticeFormat (
					"Created %s missing entries",
					integerToDecimalString (
						numCreated));

			} else {

				requestContext.addNoticeFormat (
					"No missing entries to create");

			}

			return optionalAbsent ();

		}

	}

}
