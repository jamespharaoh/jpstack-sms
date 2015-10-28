package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.timestampFormatSeconds;

import java.util.List;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageSearchResultsCsvPart")
public
class MessageSearchResultsCsvPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ConsoleObjectManager objectManager;

	// implementation

	@Override
	public
	void renderHtmlBodyContent () {

		List<?> messageSearchResult =
			(List<?>)
			requestContext.request ("messageSearchResult");

		printFormat (
			"<pre>");

		printFormat (
			"ID,From,To,Time,Route,Status,Service,Direction,Message");

		for (
			Object messageIdObject
				: messageSearchResult
		) {

			MessageRec message =
				messageHelper.find (
					(Integer) messageIdObject);

			String summaryText =
				messageConsoleLogic.messageContentText (
					message);

			String rowClass =
				MessageConsoleStuff.classForMessage (message);

			printFormat (
				"%h,",
				message.getId ());

			printFormat (
				"%h,",
				message.getDirection () == MessageDirection.in
					? message.getNumber ().getNumber ()
					: message.getNumFrom ());

			printFormat (
				"%h,",
				message.getDirection () == MessageDirection.out
					? message.getNumber ().getNumber ()
					: message.getNumTo ());

			printFormat (
				"%h,",
				timestampFormatSeconds.format (
					message.getCreatedTime ()));

			printFormat (
				"%h,",
				message.getRoute ().getCode ());

			printFormat (
				"%h-%h\n",
				MessageConsoleStuff.classForMessageStatus (
					message.getStatus ()),
				MessageConsoleStuff.textForMessageStatus (
					message.getStatus ()));

			printFormat (
				"\"%s\",",
				objectManager.tdForObjectMiniLink (
					message.getService ()));

			printFormat (
				"\"%h\",",
				rowClass);

			printFormat (
				"\"%h\",",
				summaryText);

			printFormat (
				"\n");

		}

		printFormat (
			"</pre>\n");

	}

}
