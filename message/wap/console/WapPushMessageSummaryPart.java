package wbs.sms.message.wap.console;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;

@Accessors (fluent = true)
@PrototypeComponent ("wapPushMessageSummaryPart")
public
class WapPushMessageSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	WapPushMessageObjectHelper wapPushMessageHelper;

	// properties

	@Getter @Setter
	MessageRec message;

	@Getter @Setter
	Boolean withMarkup = true;

	// state

	WapPushMessageRec wapPushMessage;

	// prepare

	@Override
	public
	void prepare () {

		wapPushMessage =
			wapPushMessageHelper.find (
				message.getId ());

	}

	@Override
	public
	void goBodyStuff () {

		if (isWithMarkup ()) {

			printFormat (
				"<tr>\n",

				"<th>WAP push text</th>\n",

				"<td>%h</td>\n",
				wapPushMessage.getTextText ().getText (),

				"</tr>\n");

			printFormat (
				"<tr>\n",

				"<th>WAP push URL</th>\n",

				"<td>%h</td>\n",
				wapPushMessage.getUrlText ().getText (),

				"</tr>\n");

		} else {

			printFormat (
				"%h (%h)",
				wapPushMessage.getTextText ().getText (),
				wapPushMessage.getUrlText ().getText ());

		}

	}

}
