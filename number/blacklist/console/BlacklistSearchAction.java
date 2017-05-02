package wbs.sms.number.blacklist.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

import wbs.web.responder.Responder;
import wbs.web.utils.HtmlUtils;

@PrototypeComponent ("blacklistSearchAction")
public
class BlacklistSearchAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	BlacklistObjectHelper blacklistHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLinkObjectHelper eventLinkHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberFormatObjectHelper numberFormatHelper;

	@SingletonDependency
	NumberFormatLogic numberFormatLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"blacklistNewResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			NumberFormatRec ukNumberFormat =
				numberFormatHelper.findByCodeRequired (
					transaction,
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

			Optional <BlacklistRec> blacklistOptional =
				blacklistHelper.findByCode (
					transaction,
					GlobalId.root,
					number);

			if (
				optionalIsNotPresent (
					blacklistOptional)
			) {

				requestContext.addError (
					"Number is not blacklisted");

				return null;

			}

			BlacklistRec blacklist =
				blacklistOptional.get ();

			// copied from eventconsolemodule for now

			Collection <EventLinkRec> eventLinks =
				eventLinkHelper.findByTypeAndRef (
					transaction,
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
					HtmlUtils.htmlEncode (
						eventType.getDescription ());

				for (
					EventLinkRec eventLink
						: event.getEventLinks ()
				) {

					if (eventLink.getTypeId () == -1) {

						text =
							text.replaceAll (
								"%" + eventLink.getIndex (),
								HtmlUtils.htmlEncode (eventLink.getRefId ().toString ()));

					} else if (eventLink.getTypeId () == -2) {

						text =
							text.replaceAll (
								"%" + eventLink.getIndex (),
								eventLink.getRefId () != 0
									? "yes"
									: "no");

					} else {

						Record <?> object =
							objectManager.findObject (
								transaction,
								new GlobalId (
									eventLink.getTypeId (),
									eventLink.getRefId ()));

						text =
							text.replaceAll (
								"%" + eventLink.getIndex (),
								objectManager.htmlForObject (
									transaction,
									object,
									optionalAbsent (),
									false));

					}

				}

				stringBuilder.append (
					text);

			}

			requestContext.addNotice (
				stringBuilder.toString ());

			return null;

		}

	}

}
