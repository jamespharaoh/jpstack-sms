package wbs.sms.message.core.console;

import static wbs.utils.etc.ResultUtils.successResultPresent;
import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.message.core.model.MessageRec;

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
			@NonNull Transaction parentTransaction,
			@NonNull MessageRec container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <MessageRec> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToInterface");

		) {

			return successResultPresent (
				formatWriterConsumerToString (
					formatWriter ->
						messageConsoleLogic.writeMessageContentHtml (
							transaction,
							formatWriter,
							container)));

		}

	}

}
