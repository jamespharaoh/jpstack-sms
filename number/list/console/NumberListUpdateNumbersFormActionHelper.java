package wbs.sms.number.list.console;

import static wbs.utils.etc.OptionalUtils.optionalOf;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.web.TextResponder;

import wbs.sms.number.list.model.NumberListUpdateRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("numberListUpdateNumbersFormActionHelper")
public
class NumberListUpdateNumbersFormActionHelper
	implements ConsoleFormActionHelper <Object, Object> {

	// singleton dependencies

	@SingletonDependency
	NumberListUpdateConsoleHelper numberListUpdateHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// public implementation

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull Object formState) {

		NumberListUpdateRec update =
			numberListUpdateHelper.findFromContextRequired ();

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
			textResponderProvider.get ()

			.filename (
				"numbers.txt")

			.text (
				stringBuilder.toString ())

		);

	}

}
