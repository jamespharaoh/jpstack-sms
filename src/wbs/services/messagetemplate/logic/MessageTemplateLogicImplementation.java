package wbs.services.messagetemplate.logic;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.substring;
import static wbs.utils.string.StringUtils.substringFrom;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

import wbs.services.messagetemplate.fixture.MessageTemplateDatabaseSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateEntryTypeSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateFieldTypeSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateParameterSpec;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldValueObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateFieldValueRec;
import wbs.services.messagetemplate.model.MessageTemplateParameterObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.services.messagetemplate.model.MessageTemplateTypeCharset;

@SingletonComponent ("messageTemplateLogic")
public
class MessageTemplateLogicImplementation
	implements MessageTemplateLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateDatabaseObjectHelper messageTemplateDatabaseHelper;

	@SingletonDependency
	MessageTemplateDatabaseLoader messageTemplateDatabaseLoader;

	@SingletonDependency
	MessageTemplateEntryTypeObjectHelper messageTemplateEntryTypeHelper;

	@SingletonDependency
	MessageTemplateEntryValueObjectHelper messageTemplateEntryValueHelper;

	@SingletonDependency
	MessageTemplateFieldTypeObjectHelper messageTemplateFieldTypeHelper;

	@SingletonDependency
	MessageTemplateFieldValueObjectHelper messageTemplateFieldValueHelper;

	@SingletonDependency
	MessageTemplateParameterObjectHelper messageTemplateParameterHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	MessageTemplateDatabaseRec readMessageTemplateDatabaseFromClasspath (
			@NonNull Transaction parentTransaction,
			@NonNull SliceRec slice,
			@NonNull String resourceName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"readMessageTemplateDatabaseFromClasspath");

		) {

			// load data

			MessageTemplateDatabaseSpec databaseSpec =
				messageTemplateDatabaseLoader.loadFromClasspath (
					transaction,
					resourceName);

			// create database

			MessageTemplateDatabaseRec mtDatabase =
				messageTemplateDatabaseHelper.insert (
					transaction,
					messageTemplateDatabaseHelper.createInstance ()

				.setSlice (
					slice)

				.setCode (
					simplifyToCodeRequired (
						databaseSpec.name ()))

				.setName (
					databaseSpec.name ())

				.setDescription (
					emptyStringIfNull (
						databaseSpec.description ()))

			);

			// create entry types

			for (
				MessageTemplateEntryTypeSpec entryTypeSpec
					: databaseSpec.entryTypes ()
			) {

				MessageTemplateEntryTypeRec entryType =
					messageTemplateEntryTypeHelper.insert (
						transaction,
						messageTemplateEntryTypeHelper.createInstance ()

					.setMessageTemplateDatabase (
						mtDatabase)

					.setCode (
						simplifyToCodeRequired (
							entryTypeSpec.name ()))

					.setName (
						entryTypeSpec.name ())

					.setDescription (
						emptyStringIfNull (
							entryTypeSpec.description ()))

				);

				// create field types

				for (
					MessageTemplateFieldTypeSpec fieldTypeSpec
						: entryTypeSpec.fieldTypes ()
				) {

					messageTemplateFieldTypeHelper.insert (
						transaction,
						messageTemplateFieldTypeHelper.createInstance ()

						.setMessageTemplateEntryType (
							entryType)

						.setCode (
							simplifyToCodeRequired (
								fieldTypeSpec.name ()))

						.setName (
							fieldTypeSpec.name ())

						.setDescription (
							emptyStringIfNull (
								fieldTypeSpec.description ()))

						.setDefaultValue (
							fieldTypeSpec.value ())

						.setHelpText (
							"")

						.setCharset (
							MessageTemplateTypeCharset.unicode)

					);

				}

				// create parameter types

				for (
					MessageTemplateParameterSpec parameterSpec
						: entryTypeSpec.parameters ()
				) {

					messageTemplateParameterHelper.insert (
						transaction,
						messageTemplateParameterHelper.createInstance ()

						.setMessageTemplateEntryType (
							entryType)

						.setCode (
							simplifyToCodeRequired (
								parameterSpec.name ()))

						.setName (
							parameterSpec.name ())

						.setDescription (
							emptyStringIfNull (
								parameterSpec.description ()))

						.setMaximumLength (
							parameterSpec.maximumLength ())

					);

				}

			}

			return mtDatabase;

		}

	}

	@Override
	public
	Optional <String> lookupFieldValue (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateSetRec messageTemplateSet,
			@NonNull String entryCode,
			@NonNull String fieldCode,
			@NonNull Map <String, String> mappings) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"lookupFieldValue");

		) {

			// lookup value

			MessageTemplateDatabaseRec messageTemplateDatabase =
				messageTemplateSet.getMessageTemplateDatabase ();

			MessageTemplateEntryTypeRec entryType =
				messageTemplateEntryTypeHelper.findByCodeRequired (
					transaction,
					messageTemplateDatabase,
					entryCode);

			MessageTemplateFieldTypeRec fieldType =
				messageTemplateFieldTypeHelper.findByCodeRequired (
					transaction,
					entryType,
					fieldCode);

			Optional <MessageTemplateEntryValueRec> entryValueOptional =
				mapItemForKey (
					messageTemplateSet.getMessageTemplateEntryValues (),
					entryType.getId ());

			Optional <MessageTemplateFieldValueRec> fieldValueOptional;

			if (
				optionalIsPresent (
					entryValueOptional)
			) {

				MessageTemplateEntryValueRec entryValue =
					optionalGetRequired (
						entryValueOptional);

				fieldValueOptional =
					mapItemForKey (
						entryValue.getFields (),
						fieldType.getId ());

			} else {

				fieldValueOptional =
					optionalAbsent ();

			}

			String value;

			if (
				optionalIsPresent (
					fieldValueOptional)
			) {

				MessageTemplateFieldValueRec fieldValue =
					optionalGetRequired (
						fieldValueOptional);

				value =
					fieldValue.getStringValue ();

			} else {

				value =
					fieldType.getDefaultValue ();

			}

			// replace placeholders

			StringBuilder stringBuilder =
				new StringBuilder ();

			Matcher matcher =
				placeholderPattern.matcher (
					value);

			int position = 0;

			while (matcher.find ()) {

				stringBuilder.append (
					substring (
						value,
						position,
						matcher.start ()));

				Optional <String> replacementOptional =
					mapItemForKey (
						mappings,
						matcher.group (1));

				if (
					optionalIsNotPresent (
						replacementOptional)
				) {
					return optionalAbsent ();
				}

				stringBuilder.append (
					optionalGetRequired (
						replacementOptional));

				position =
					matcher.end ();

			}

			stringBuilder.append (
				substringFrom (
					value,
					position));

			// return

			return optionalOfFormat (
				stringBuilder.toString ());

		}

	}

	// data

	Pattern placeholderPattern =
		Pattern.compile (
			"\\$\\{([^}]+)\\}");

}
