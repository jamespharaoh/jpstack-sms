package wbs.services.messagetemplate.logic;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.TransientObjectException;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldValueObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateFieldValueRec;

public
class MessageTemplateEntryValueHooks
	implements ObjectHooks <MessageTemplateEntryValueRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	MessageTemplateFieldTypeObjectHelper messageTemplateFieldTypeHelper;

	@WeakSingletonDependency
	MessageTemplateFieldValueObjectHelper messageTemplateFieldValueHelper;

	// implementation

	@Override
	public
	Object getDynamic (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateEntryValueRec entryValue,
			@NonNull String name) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getDynamic");

		) {

			MessageTemplateEntryTypeRec entryType =
				entryValue.getMessageTemplateEntryType ();

			// find the ticket field type

			MessageTemplateFieldTypeRec fieldType =
				messageTemplateFieldTypeHelper.findByCodeRequired (
					transaction,
					entryType,
					name);

			try {

				// find the message template value

				MessageTemplateFieldValueRec fieldValue =
					entryValue.getFields ().get (
						fieldType.getId ());

				if (fieldValue == null) {

					return fieldType.getDefaultValue ();

				} else {

					return fieldValue.getStringValue ();

				}

			} catch (TransientObjectException exception) {

				// object not yet saved so fields will all be null

				return null;

			}

		}

	}

	@Override
	public
	Optional <String> setDynamic (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateEntryValueRec entryValue,
			@NonNull String name,
			@NonNull Optional <?> valueOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setDynamic");

		) {

			MessageTemplateEntryTypeRec entryType =
				entryValue.getMessageTemplateEntryType ();

			// find the ticket field type

			MessageTemplateFieldTypeRec fieldType =
				messageTemplateFieldTypeHelper.findByCodeRequired (
					transaction,
					entryType,
					name);

			/*
			List<String> messageTemplateUsedParameters =
				new ArrayList<String> ();

			String message =
				(String) value;

			// length of non variable parts

			Integer messageLength = 0;

			String[] parts =
				message.split (
					"\\{(.*?)\\}");

			for (int i = 0; i < parts.length; i++) {

				// length of special chars if gsm encoding

				if (
					equal (
						messageTemplateType.getCharset (),
						MessageTemplateTypeCharset.gsm)
				) {

					if (! Gsm.isGsm (parts [i])) {

						throw new RuntimeException (
							"Message text is invalid");

					}

					messageLength +=
						Gsm.length (parts [i]);

				} else {

					messageLength +=
						parts [i].length ();

				}

			}

			// length of the parameters

			Pattern regExp =
				Pattern.compile ("\\{(.*?)\\}");

			Matcher matcher =
				regExp.matcher (message);

			while (matcher.find ()) {

				String parameterName =
					matcher.group (1);

				MessageTemplateParameterRec messageTemplateParameter =
					messageTemplateParameterHelper.get ().findByCode (
						messageTemplateType,
						parameterName);

				if (messageTemplateParameter == null) {

					throw new RuntimeException (
						stringFormat (
							"The parameter %s does not exist!",
							parameterName));

				}

				if (messageTemplateParameter.getLength () != null) {

					messageLength +=
						messageTemplateParameter.getLength ();

				}

				messageTemplateUsedParameters.add (
					messageTemplateParameter.getName ());

			}

			// check if the rest of parameters which are not present were
			// required

			for (
				MessageTemplateParameterRec messageTemplateParameter
					: messageTemplateType.getMessageTemplateParameters ()
			) {

				if (
					doesNotContain (
						messageTemplateUsedParameters,
						messageTemplateParameter.getName ())
					&& messageTemplateParameter.getRequired ()
				) {

					throw new RuntimeException (
						stringFormat (
							"Parameter %s required but not present",
							messageTemplateParameter.getName ()));

				}

			}

			// check if the length is correct

			if (
				messageLength < messageTemplateType.getMinLength () ||
				messageLength > messageTemplateType.getMaxLength ())
			{

				throw new RuntimeException (
					"The message length is out of its template type bounds!");

			}
			*/

			// find or create value

			MessageTemplateFieldValueRec fieldValue =
				entryValue.getFields ().get (
					fieldType.getId ());

			if (

				isNull (
					fieldValue)

				&& optionalIsPresent (
					valueOptional)

			) {

				fieldValue =
					messageTemplateFieldValueHelper.insert (
						transaction,
						messageTemplateFieldValueHelper.createInstance ()

					.setMessageTemplateEntryValue (
						entryValue)

					.setMessageTemplateFieldType (
						fieldType)

					.setStringValue (
						(String)
						optionalGetRequired (
							valueOptional))

				);

				entryValue.getFields ().put (
					fieldType.getId (),
					fieldValue);

			} else if (
				optionalIsPresent (
					valueOptional)
			) {

				fieldValue

					.setStringValue (
						(String)
						optionalGetRequired (
							valueOptional));

			} else if (

				isNotNull (
					fieldValue)

				&& optionalIsNotPresent (
					valueOptional)

			) {

				messageTemplateFieldValueHelper.remove (
					transaction,
					fieldValue);

				entryValue.getFields ().remove (
					fieldType.getId ());

			}

			return optionalAbsent ();

		}

	}

}