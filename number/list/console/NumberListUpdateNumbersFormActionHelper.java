package wbs.sms.number.list.console;

import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.list.model.NumberListUpdateRec;

import wbs.web.responder.TextResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("numberListUpdateNumbersFormActionHelper")
public
class NumberListUpdateNumbersFormActionHelper
	implements ConsoleFormActionHelper <Object, Object> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberListUpdateConsoleHelper numberListUpdateHelper;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TextResponder> textResponderProvider;

	// public implementation

	@Override
	public
	Optional <WebResponder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull Object formState) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			NumberListUpdateRec update =
				numberListUpdateHelper.findFromContextRequired (
					transaction);

			StringBuilder stringBuilder =
				new StringBuilder ();

			update.getNumbers ().stream ().sorted ().forEach (
				number -> {

				stringBuilder.append (
					number.getNumber ());

				stringBuilder.append (
					"\n");

			});

			return optionalOf (
				textResponderProvider.provide (
					transaction,
					textResponder ->
						textResponder

				.filename (
					"numbers.txt")

				.text (
					stringBuilder.toString ())

			));

		}

	}

}
