package wbs.sms.message.wap.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
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
			MessageRec message) {

		WapPushMessageRec wapPushMessage =
			wapPushMessageHelper.findOrNull (
				message.getId ());

		if (wapPushMessage == null) {
			return "";
		}

		return stringFormat (
			"%s (%s)",
			wapPushMessage.getTextText ().getText (),
			wapPushMessage.getUrlText ().getText ());

	}

	@Override
	public
	String messageSummaryHtml (
			MessageRec message) {

		WapPushMessageRec wapPushMessage =
			wapPushMessageHelper.findOrNull (
				message.getId ());

		return stringFormat (
			"%h (%h)",
			wapPushMessage.getTextText ().getText (),
			wapPushMessage.getUrlText ().getText ());

	}

}
