package wbs.sms.message.core.console;

import static wbs.utils.etc.Misc.successResult;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.message.core.model.MessageRec;
import wbs.utils.string.StringFormatWriter;

import fj.data.Either;

@PrototypeComponent ("messageContentHtmlFormFieldInterfaceMapping")
public
class MessageContentHtmlFormFieldInterfaceMapping
	implements FormFieldInterfaceMapping <MessageRec, MessageRec, String> {

	// singleton dependencies

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	// implementation

	@Override
	public
	Either <Optional <MessageRec>, String> interfaceToGeneric (
			@NonNull MessageRec container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull MessageRec container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <MessageRec> genericValue) {

		StringFormatWriter formatWriter =
			new StringFormatWriter ();

		messageConsoleLogic.writeMessageContentHtml (
			formatWriter,
			container);

		return successResult (
			Optional.of (
				formatWriter.toString ()));

	}

}
