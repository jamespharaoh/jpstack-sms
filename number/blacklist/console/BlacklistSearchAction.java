package wbs.sms.number.blacklist.console;

import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.Cleanup;
import wbs.console.action.ConsoleAction;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.framework.web.Responder;
import wbs.platform.event.model.EventLinkObjectHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;
import wbs.platform.event.model.EventTypeRec;
import wbs.sms.number.blacklist.model.BlacklistObjectHelper;
import wbs.sms.number.blacklist.model.BlacklistRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.format.model.NumberFormatRec;

@PrototypeComponent ("blacklistSearchAction")
public
class BlacklistSearchAction
	extends ConsoleAction {

	// dependencies

	@Inject
	BlacklistObjectHelper blacklistHelper;

	@Inject
	Database database;

	@Inject
	EventLinkObjectHelper eventLinkHelper;

	@Inject
	NumberFormatObjectHelper numberFormatHelper;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"blacklistNewResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"BlacklistSearchAction.goReal ()",
				this);

		NumberFormatRec ukNumberFormat =
			numberFormatHelper.findByCodeRequired (
				GlobalId.root,
				"uk");

		String number;

		try {

			number =
				numberFormatLogic.parse (
					ukNumberFormat,
					requestContext.parameterRequired (
						"number"));

		} catch (WbsNumberFormatException exception) {

			requestContext.addError (
				"Invalid mobile number");

			return null;

		}

		Optional<BlacklistRec> blacklistOptional =
			blacklistHelper.findByCode (
				GlobalId.root,
				number);

		if (
			isNotPresent (
				blacklistOptional)
		) {

			requestContext.addError (
				"Number is not blacklisted");

			return null;

		}

		BlacklistRec blacklist =
			blacklistOptional.get ();

		// copied from eventconsolemodule for now

		Collection<EventLinkRec> eventLinks =
			eventLinkHelper.findByTypeAndRef (
				objectManager.objectClassToTypeId (
					BlacklistRec.class),
				blacklist.getId ());

		StringBuilder stringBuilder =
			new StringBuilder (
				"Number is blacklisted ");

		Set<EventRec> events =
			new TreeSet<EventRec> ();

		for (
			EventLinkRec eventLink
				: eventLinks
		) {

			events.add (
				eventLink.getEvent ());

		}

		for (
			EventRec event
				: events
		) {

			EventTypeRec eventType =
				event.getEventType ();

			String text =
				Html.encode (
					eventType.getDescription ());

			for (
				EventLinkRec eventLink
					: event.getEventLinks ()
			) {

				if (eventLink.getTypeId () == -1) {

					text =
						text.replaceAll (
							"%" + eventLink.getIndex (),
							Html.encode (eventLink.getRefId ().toString ()));

				} else if (eventLink.getTypeId () == -2) {

					text =
						text.replaceAll (
							"%" + eventLink.getIndex (),
							eventLink.getRefId () != 0
								? "yes"
								: "no");

				} else {

					Record<?> object =
						objectManager.findObject (
							new GlobalId (
								eventLink.getTypeId (),
								eventLink.getRefId ()));

					text =
						text.replaceAll (
							"%" + eventLink.getIndex (),
							objectManager.objectToSimpleHtml (
								object,
								null,
								false));

				}

			}

			stringBuilder.append (text);

		}

		requestContext.addNotice (
			stringBuilder.toString ());

		return null;

	}

}
