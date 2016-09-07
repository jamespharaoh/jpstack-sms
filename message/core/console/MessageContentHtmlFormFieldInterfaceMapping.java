package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.component.annotations.PrototypeComponent;
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
	Either<Optional<MessageRec>,String> interfaceToGeneric (
			@NonNull MessageRec container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull MessageRec container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<MessageRec> genericValue) {

		return successResult (
			Optional.of (
				messageConsoleLogic.messageContentHtml (
					container)));

	}

}
