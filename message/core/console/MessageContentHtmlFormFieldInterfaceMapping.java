package wbs.sms.message.core.console;

import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.successResult;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;

import wbs.utils.string.StringFormatWriter;

import fj.data.Either;

@PrototypeComponent ("messageContentHtmlFormFieldInterfaceMapping")
public
class MessageContentHtmlFormFieldInterfaceMapping
	implements FormFieldInterfaceMapping <MessageRec, MessageRec, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <MessageRec> genericValue) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"genericToInterface");

		StringFormatWriter formatWriter =
			new StringFormatWriter ();

		messageConsoleLogic.writeMessageContentHtml (
			taskLogger,
			formatWriter,
			container);

		return successResult (
			optionalOf (
				formatWriter.toString ()));

	}

}
