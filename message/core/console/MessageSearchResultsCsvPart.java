package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.spacify;
import static wbs.framework.utils.etc.Misc.timestampFormatSeconds;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.part.PagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageSearchResultsCsvPart")
public
class MessageSearchResultsCsvPart
	extends AbstractPagePart {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageConsolePluginManager messageConsolePluginManager;

	@Override
	public
	void renderHtmlBodyContent () {

		List<?> messageSearchResult =
			(List<?>)
			requestContext.request ("messageSearchResult");

		out.println ("<pre>");
		out.println ("ID,From,To,Time,Route,Status,Service,Direction,Message");

		for (Object messageIdObject
				: messageSearchResult) {

			MessageRec message =
				messageHelper.find (
					(Integer) messageIdObject);

			// FailedMessageRec failedMessage =
			// smsDao.findFailedMessageById(message.getId());

			MessageConsolePlugin messageConsolePlugin =
				messageConsolePluginManager.getPlugin (
					message.getMessageType ().getCode ());

			PagePart summaryPart = null;

			if (messageConsolePlugin != null) {

				summaryPart =
					messageConsolePlugin.makeMessageSummaryPart (message);

				summaryPart.setWithMarkup (false);

				summaryPart.setup (
					Collections.<String,Object>emptyMap ());

				summaryPart.prepare ();

			}

			String rowClass =
				MessageConsoleStuff.classForMessage (message);

			printFormat (
				"%h,",
				message.getId (),

				"%h,",
				message.getDirection () == MessageDirection.in
					? message.getNumber ().getNumber ()
					: message.getNumFrom (),

				"%h,",
				message.getDirection () == MessageDirection.out
					? message.getNumber ().getNumber ()
					: message.getNumTo (),

				"%h,",
				timestampFormatSeconds.format (
					message.getCreatedTime ()),

				"%h,",
				message.getRoute ().getCode (),

				"%h-%h\n",
				MessageConsoleStuff.classForMessageStatus (
					message.getStatus ()),
				MessageConsoleStuff.textForMessageStatus (
					message.getStatus ()),

				"\"%s\",",
				objectManager.tdForObjectMiniLink (
					message.getService ()),

				"\"%h\",",
				rowClass);

			// TODO This should be able to see the details of the wap push

			if (summaryPart != null) {

				summaryPart.renderHtmlBodyContent ();

			} else {

				printFormat (
					"\"%h\"",
					spacify (
						message.getText ().getText ()));

			}

			printFormat (
				"\n");

		}

		printFormat (
			"</pre>\n");

	}

}
