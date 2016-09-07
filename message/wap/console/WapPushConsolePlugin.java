package wbs.sms.message.wap.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.sms.message.core.console.MessageConsolePlugin;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;

@SingletonComponent ("wapPushConsolePlugin")
public
class WapPushConsolePlugin
	implements MessageConsolePlugin {

	// dependencies

	@Inject
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
	String messageSummaryText (
			@NonNull MessageRec message) {

		Optional<WapPushMessageRec> wapPushMessageOptional =
			wapPushMessageHelper.find (
				message.getId ());

		if (
			optionalIsNotPresent (
				wapPushMessageOptional)
		) {
			return "";
		}

		WapPushMessageRec wapPushMessage =
			wapPushMessageOptional.get ();

		return stringFormat (
			"%s (%s)",
			wapPushMessage.getTextText ().getText (),
			wapPushMessage.getUrlText ().getText ());

	}

	@Override
	public
	String messageSummaryHtml (
			@NonNull MessageRec message) {

		WapPushMessageRec wapPushMessage =
			wapPushMessageHelper.findRequired (
				message.getId ());

		return stringFormat (
			"%h (%h)",
			wapPushMessage.getTextText ().getText (),
			wapPushMessage.getUrlText ().getText ());

	}

}
