package wbs.sms.message.wap.console;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.message.core.console.MessageConsolePlugin;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;
import wbs.utils.string.FormatWriter;

@SingletonComponent ("wapPushConsolePlugin")
public
class WapPushConsolePlugin
	implements MessageConsolePlugin {

	// singleton dependencies

	@SingletonDependency
	WapPushMessageObjectHelper wapPushMessageHelper;

	// details

	@Override
	public
	String getCode () {
		return "wap_push";
	}

	// implementation

	@Override
	public
	void writeMessageSummaryText (
			@NonNull FormatWriter formatWriter,
			@NonNull MessageRec message) {

		Optional <WapPushMessageRec> wapPushMessageOptional =
			wapPushMessageHelper.find (
				message.getId ());

		if (
			optionalIsNotPresent (
				wapPushMessageOptional)
		) {
			return;
		}

		WapPushMessageRec wapPushMessage =
			wapPushMessageOptional.get ();

		formatWriter.writeFormat (
			"%s (%s)",
			wapPushMessage.getTextText ().getText (),
			wapPushMessage.getUrlText ().getText ());

	}

	@Override
	public
	void writeMessageSummaryHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull MessageRec message) {

		WapPushMessageRec wapPushMessage =
			wapPushMessageHelper.findRequired (
				message.getId ());

		formatWriter.writeFormat (
			"%h (%h)",
			wapPushMessage.getTextText ().getText (),
			wapPushMessage.getUrlText ().getText ());

	}

}
