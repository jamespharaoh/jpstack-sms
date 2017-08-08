package wbs.services.messagetemplate.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

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

import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.web.responder.WebResponder;

@SingletonComponent ("messageTemplateDatabaseSettingsCopyFormActionHelper")
public
class MessageTemplateDatabaseSettingsCopyFormHelper
	implements ConsoleFormActionHelper <
		MessageTemplateCopyForm,
		Object
	> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateDatabaseConsoleHelper messageTemplateDatabaseHelper;

	@SingletonDependency
	MessageTemplateEntryTypeConsoleHelper messageTemplateEntryTypeHelper;

	@SingletonDependency
	MessageTemplateFieldTypeConsoleHelper messageTemplateFieldTypeHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	Optional <WebResponder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateCopyForm state) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			MessageTemplateDatabaseRec targetDatabase =
				messageTemplateDatabaseHelper.findFromContextRequired (
					transaction);

			MessageTemplateDatabaseRec sourceDatabase =
				messageTemplateDatabaseHelper.findRequired (
					transaction,
					state.sourceMessageTemplateDatabaseId ());

			for (
				MessageTemplateEntryTypeRec sourceEntryType
					: sourceDatabase.getMessageTemplateEntryTypes ()
			) {

				MessageTemplateEntryTypeRec targetEntryType =
					messageTemplateEntryTypeHelper.findOrCreate (
						transaction,
						targetDatabase,
						sourceEntryType.getCode (),
						newEntryType ->
							newEntryType

					.setName (
						sourceEntryType.getName ())

					.setDescription (
						sourceEntryType.getDescription ())

				);

				for (
					MessageTemplateFieldTypeRec sourceFieldType
						: sourceEntryType.getMessageTemplateFieldTypes ()
				) {

					messageTemplateFieldTypeHelper.findOrCreate (
						transaction,
						targetEntryType,
						sourceFieldType.getCode (),
						targetFieldType ->
							targetFieldType

						.setName (
							sourceFieldType.getName ())

						.setDescription (
							sourceFieldType.getDescription ())

						.setDefaultValue (
							sourceFieldType.getDefaultValue ())

						.setHelpText (
							sourceFieldType.getHelpText ())

						.setMinLength (
							sourceFieldType.getMinLength ())

						.setMaxLength (
							sourceFieldType.getMaxLength ())

						.setCharset (
							sourceFieldType.getCharset ())

					);

				}

			}

			transaction.commit ();

			requestContext.addNoticeFormat (
				"Message database types copied from %s.%s",
				sourceDatabase.getSlice ().getCode (),
				sourceDatabase.getCode ());

			return optionalAbsent ();

		}

	}

}
