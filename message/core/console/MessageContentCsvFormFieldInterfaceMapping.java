package wbs.sms.message.core.console;

import static wbs.utils.etc.ResultUtils.successResultPresent;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;

import wbs.utils.string.StringFormatWriter;

import fj.data.Either;

@PrototypeComponent ("messageContentCsvFormFieldInterfaceMapping")
public
class MessageContentCsvFormFieldInterfaceMapping
	implements FormFieldInterfaceMapping <MessageRec, MessageRec, String> {

	// singleton dependencies

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

		StringFormatWriter formatWriter =
			new StringFormatWriter ();

		messageConsoleLogic.writeMessageContentText (
			formatWriter,
			container);

		return successResultPresent (
			formatWriter.toString ());

	}

}
