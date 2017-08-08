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

import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeObjectHelperMethods;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;

public
class MessageTemplateFieldTypeObjectHelperMethodsImplementation
	implements MessageTemplateFieldTypeObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	MessageTemplateFieldTypeObjectHelper messageTemplateFieldTypeHelper;

	// implementation

	@Override
	public
	MessageTemplateFieldTypeRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateEntryTypeRec messageTemplateEntryType,
			@NonNull String code,
			@NonNull Consumer <MessageTemplateFieldTypeRec> consumer) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			Optional <MessageTemplateFieldTypeRec>
				existingMessageTemplateFieldType =
					messageTemplateFieldTypeHelper.findByCode (
						transaction,
						messageTemplateEntryType,
						code);

			if (
				optionalIsPresent (
					existingMessageTemplateFieldType)
			) {

				return optionalGetRequired (
					existingMessageTemplateFieldType);

			}

			MessageTemplateFieldTypeRec newMessageTemplateField =
				messageTemplateFieldTypeHelper.createInstance ()

				.setMessageTemplateEntryType (
					messageTemplateEntryType)

				.setCode (
					code)

			;

			consumer.accept (
				newMessageTemplateField);

			messageTemplateFieldTypeHelper.insert (
				transaction,
				newMessageTemplateField);

			return newMessageTemplateField;

		}

	}

}
