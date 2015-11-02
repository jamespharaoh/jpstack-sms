package wbs.sms.message.core.console;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageContentHtmlFormFieldInterfaceMapping")
public
class MessageContentHtmlFormFieldInterfaceMapping
	implements FormFieldInterfaceMapping<MessageRec,MessageRec,String> {

	// dependencies

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	// implementation

	@Override
	public
	Optional<MessageRec> interfaceToGeneric (
			@NonNull MessageRec container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull MessageRec container,
			@NonNull Optional<MessageRec> genericValue) {

		return Optional.of (
			messageConsoleLogic.messageContentHtml (
				container));

	}

}
