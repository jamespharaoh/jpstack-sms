package wbs.services.messagetemplate.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Ordering;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleFormBuilder;
import wbs.console.forms.object.ObjectFieldsProvider;
import wbs.console.forms.scriptref.ScriptRefFormFieldSpec;
import wbs.console.forms.text.TextAreaFormFieldSpec;
import wbs.console.forms.types.ConsoleFormFieldSpec;
import wbs.console.forms.types.ConsoleNamedFormFieldSpec;
import wbs.console.forms.types.FieldsProvider.FormFieldSetPair;
import wbs.console.module.ConsoleModule;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryValueRec;
import wbs.services.messagetemplate.model.MessageTemplateFieldTypeRec;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;

@Accessors (fluent = true)
@PrototypeComponent ("messageTemplateEntryValueObjectFieldsProvider")
public
class MessageTemplateEntryValueObjectFieldsProvider
	implements ObjectFieldsProvider <
		MessageTemplateEntryValueRec,
		MessageTemplateSetRec
	> {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@WeakSingletonDependency
	@NamedDependency ("messageTemplateEntryValueConsoleModule")
	ConsoleModule consoleModule;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateEntryValueConsoleHelper messageTemplateEntryValueHelper;

	// details

	@Override
	public
	Class <MessageTemplateEntryValueRec> containerClass () {
		return MessageTemplateEntryValueRec.class;
	}

	@Override
	public
	Class <MessageTemplateSetRec> parentClass () {
		return MessageTemplateSetRec.class;
	}

	// implementation

	@Override
	public
	FormFieldSetPair <MessageTemplateEntryValueRec> getListFields (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateEntryValueRec object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getListFields");

		) {

			List <ConsoleFormFieldSpec> columnFields =
				new ArrayList<> ();

			List <ConsoleFormFieldSpec> rowFields =
				new ArrayList<> ();

			return new FormFieldSetPair <MessageTemplateEntryValueRec> ()

				.columnFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						messageTemplateEntryValueHelper,
						"list",
						columnFields))

				.rowFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						messageTemplateEntryValueHelper,
						"list",
						rowFields))

			;

		}

	}

	@Override
	public
	FormFieldSetPair <MessageTemplateEntryValueRec> getCreateFields (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateSetRec parent) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getCreateFields");

		) {

			List <ConsoleFormFieldSpec> columnFields =
				new ArrayList<> ();

			List <ConsoleFormFieldSpec> rowFields =
				new ArrayList<> ();

			return new FormFieldSetPair <MessageTemplateEntryValueRec> ()

				.columnFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						messageTemplateEntryValueHelper,
						"create",
						columnFields))

				.rowFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						messageTemplateEntryValueHelper,
						"list",
						rowFields))

			;

		}

	}

	@Override
	public
	FormFieldSetPair <MessageTemplateEntryValueRec> getSummaryFields (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateEntryValueRec entryValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSummaryFields");

		) {

			List <ConsoleFormFieldSpec> columnFields =
				new ArrayList<> ();

			List <ConsoleFormFieldSpec> rowFields =
				new ArrayList<> ();

			addDynamicFields (
				transaction,
				columnFields,
				entryValue);

			return new FormFieldSetPair <MessageTemplateEntryValueRec> ()

				.columnFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						messageTemplateEntryValueHelper,
						"summary",
						columnFields))

				.rowFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						messageTemplateEntryValueHelper,
						"list",
						rowFields))

			;

		}

	}

	@Override
	public
	FormFieldSetPair <MessageTemplateEntryValueRec> getSettingsFields (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateEntryValueRec entryValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSettingsFields");

		) {

			List <ConsoleFormFieldSpec> columnFields =
				new ArrayList<> ();

			List <ConsoleFormFieldSpec> rowFields =
				new ArrayList<> ();

			addScriptRefFields (
				transaction,
				columnFields);

			addDynamicFields (
				transaction,
				columnFields,
				entryValue);

			return new FormFieldSetPair <MessageTemplateEntryValueRec> ()

				.columnFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						messageTemplateEntryValueHelper,
						"create",
						columnFields))

				.rowFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						messageTemplateEntryValueHelper,
						"list",
						rowFields))

			;

		}

	}

	// private implementation

	private
	void addScriptRefFields (
			@NonNull Transaction parentTransaction,
			@NonNull List <ConsoleFormFieldSpec> specs) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getFieldsForObject");

		) {

			specs.add (
				new ScriptRefFormFieldSpec ()

				.path (
					"/js/jquery-1.7.1.js")

			);

			specs.add (
				new ScriptRefFormFieldSpec ()

				.path (
					"/js/message-template.js")

			);

			specs.add (
				new ScriptRefFormFieldSpec ()

				.path (
					"/js/gsm.js")

			);

		}

	}

	private
	void addDynamicFields (
			@NonNull Transaction parentTransaction,
			@NonNull List <ConsoleFormFieldSpec> columnSpecs,
			@NonNull MessageTemplateEntryValueRec entryValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addDynamicFields");

		) {

			MessageTemplateEntryTypeRec entryType =
				entryValue.getMessageTemplateEntryType ();

			List <ConsoleNamedFormFieldSpec> dynamicFieldSpecs =
				new ArrayList<> ();

			for (
				MessageTemplateFieldTypeRec fieldType
					: entryType.getMessageTemplateFieldTypes ()
			) {

				columnSpecs.add (
					new TextAreaFormFieldSpec ()

					.name (
						fieldType.getCode ())

					.label (
						fieldType.getName ())

					.parent (
						fieldType)

					.dynamic (
						true)

				);

			}

			Collections.sort (
				dynamicFieldSpecs,
				Ordering.natural ().onResultOf (
					ConsoleNamedFormFieldSpec::name));

			columnSpecs.addAll (
				dynamicFieldSpecs);

		}

	}

}
