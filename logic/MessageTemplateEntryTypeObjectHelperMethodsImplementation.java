package wbs.services.messagetemplate.logic;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.function.Consumer;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeObjectHelperMethods;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;

public
class MessageTemplateEntryTypeObjectHelperMethodsImplementation
	implements MessageTemplateEntryTypeObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	MessageTemplateEntryTypeObjectHelper messageTemplateEntryTypeHelper;

	// implementation

	@Override
	public
	MessageTemplateEntryTypeRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateDatabaseRec messageTemplateDatabase,
			@NonNull String code,
			@NonNull Consumer <MessageTemplateEntryTypeRec> consumer) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			Optional <MessageTemplateEntryTypeRec>
				existingMessageTemplateEntryType =
					messageTemplateEntryTypeHelper.findByCode (
						transaction,
						messageTemplateDatabase,
						code);

			if (
				optionalIsPresent (
					existingMessageTemplateEntryType)
			) {

				return optionalGetRequired (
					existingMessageTemplateEntryType);

			}

			MessageTemplateEntryTypeRec newMessageTemplateEntry =
				messageTemplateEntryTypeHelper.createInstance ()

				.setMessageTemplateDatabase (
					messageTemplateDatabase)

				.setCode (
					code);

			consumer.accept (
				newMessageTemplateEntry);

			return messageTemplateEntryTypeHelper.insert (
				transaction,
				newMessageTemplateEntry);

		}

	}

}
