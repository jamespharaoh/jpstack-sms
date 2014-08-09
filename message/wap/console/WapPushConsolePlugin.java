package wbs.sms.message.wap.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.utils.etc.StringFormatter;
import wbs.platform.console.part.PagePart;
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

	@Inject
	Provider<WapPushMessageSummaryPart> wapPushMessageSummaryPart;

	// details

	@Override
	public
	String getCode () {
		return "wap_push";
	}

	// implementation

	@Override
	public
	String messageSummary (
			MessageRec message) {

		WapPushMessageRec wapPushMessage =
			wapPushMessageHelper.find (
				message.getId ());

		if (wapPushMessage == null) {
			return "";
		}

		return StringFormatter.standard (
			"%s (%s)",
			wapPushMessage.getTextText ().getText (),
			wapPushMessage.getUrlText ().getText ());

	}

	@Override
	public
	PagePart makeMessageSummaryPart (
			MessageRec message) {

		return wapPushMessageSummaryPart.get ()
			.message (message);

	}

}
