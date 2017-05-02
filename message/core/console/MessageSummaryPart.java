package wbs.sms.message.core.console;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifThenElseEmDash;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.utf8ToString;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlUtils.htmlLinkWriteHtml;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.console.FailedMessageConsoleHelper;
import wbs.sms.message.outbox.model.FailedMessageRec;

@PrototypeComponent ("messageSummaryPart")
public
class MessageSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	FailedMessageConsoleHelper failedMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;
	Optional <FailedMessageRec> failedMessageOptional;
	MessageConsolePlugin plug;
	String summaryHtml;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			message =
				messageHelper.findFromContextRequired (
					transaction);

			failedMessageOptional =
				failedMessageHelper.find (
					transaction,
					message.getId ());

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			// open table

			htmlTableOpenDetails ();

			// write id

			htmlTableDetailsRowWrite (
				"ID",
				integerToDecimalString (
					message.getId ()));

			htmlTableDetailsRowWrite (
				"Thread ID",
				integerToDecimalString (
					message.getThreadId ()));

			htmlTableDetailsRowWrite (
				"Other ID",
				ifNull (
					message.getOtherId (),
					"—"));

			htmlTableDetailsRowWriteHtml (
				"Message",
				() -> messageConsoleLogic.writeMessageContentHtml (
					transaction,
					formatWriter,
					message));

			if (
				enumEqualSafe (
					message.getDirection (),
					MessageDirection.in)
			) {

				htmlTableDetailsRowWriteRaw (
					"Number from",
					() ->
						objectManager.writeTdForObjectMiniLink (
							transaction,
							message.getNumber ()));

				htmlTableDetailsRowWrite (
					"Number to",
					message.getNumTo ());

			} else {

				htmlTableDetailsRowWrite (
					"Number from",
					message.getNumFrom ());

				htmlTableDetailsRowWriteRaw (
					"Number to",
					() ->
						objectManager.writeTdForObjectMiniLink (
							transaction,
							message.getNumber ()));

			}

			htmlTableDetailsRowWrite (
				"Status",
				message.getStatus ().getDescription ());

			htmlTableDetailsRowWrite (
				"Direction",
				message.getDirection ().name ());

			htmlTableDetailsRowWriteRaw (
				"Route",
				() ->
					objectManager.writeTdForObjectMiniLink (
						transaction,
						message.getRoute ()));

			htmlTableDetailsRowWrite (
				"Network",
				message.getNetwork ().getDescription ());

			htmlTableDetailsRowWriteRaw (
				"Service",
				() ->
					objectManager.writeTdForObjectMiniLink (
						transaction,
						message.getService ()));

			htmlTableDetailsRowWriteRaw (
				"Affiliate",
				() ->
					objectManager.writeTdForObjectMiniLink (
						transaction,
						message.getAffiliate ()));

			if (
				enumEqualSafe (
					message.getDirection (),
					MessageDirection.in)
			) {

				htmlTableDetailsRowWrite (
					"Time sent",
					ifNotNullThenElseEmDash (
						message.getNetworkTime (),
						() -> userConsoleLogic.timestampWithTimezoneString (
							transaction,
							message.getNetworkTime ())));

				htmlTableDetailsRowWrite (
					"Time received",
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						message.getCreatedTime ()));

				htmlTableDetailsRowWrite (
					"Time processed",
					ifNotNullThenElseEmDash (
						message.getProcessedTime (),
						() ->
							userConsoleLogic.timestampWithTimezoneString (
								transaction,
								message.getProcessedTime ())));

				htmlTableDetailsRowWriteRaw (
					"Command",
					() -> ifNotNullThenElse (
						message.getCommand (),
						() -> objectManager.writeTdForObjectMiniLink (
							transaction,
							message.getCommand ()),
						() -> htmlTableCellWrite (
							"—")));

			} else {

				htmlTableDetailsRowWrite (
					"Time created",
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						message.getCreatedTime ()));

				htmlTableDetailsRowWrite (
					"Time sent",
					ifNotNullThenElseEmDash (
						message.getProcessedTime (),
						() ->
							userConsoleLogic.timestampWithTimezoneString (
								transaction,
								message.getProcessedTime ())));

				htmlTableDetailsRowWrite (
					"Time received",
					ifNotNullThenElseEmDash (
						message.getNetworkTime (),
						() ->
							userConsoleLogic.timestampWithTimezoneString (
								transaction,
								message.getNetworkTime ())));

			}

			htmlTableDetailsRowWriteRaw (
				"Charge",
				ifThenElseEmDash (
					moreThanZero (
						message.getCharge ()),
					() -> currencyLogic.formatHtmlTd (
						message.getRoute ().getCurrency (),
						message.getCharge ())));

			List <MediaRec> medias =
				message.getMedias ();

			if (
				collectionIsNotEmpty (
					medias)
			) {

				for (
					int index = 0;
					index < medias.size ();
					index ++
				) {

					MediaRec media =
						medias.get (index);

					int mediaIndex =
						index;

					if (
						mediaLogic.isText (
							media)
					) {

						htmlTableDetailsRowWrite (
							"Media",
							utf8ToString (
								media.getContent ().getData ()));

					} else {

						htmlTableDetailsRowWriteRaw (
							"Media",
							() -> {

							formatWriter.writeLineFormatIncreaseIndent (
								"<td>");

							htmlLinkWriteHtml (
								requestContext.resolveLocalUrlFormat (
									"/message.mediaSummary?index=%u",
									integerToDecimalString (
										mediaIndex)),
								() -> mediaConsoleLogic.writeMediaThumb100 (
									transaction,
									media));

							formatWriter.writeLineFormatDecreaseIndent (
								"</td>");

						});

					}

				}

			}

			htmlTableDetailsRowWrite (
				"Tags",
				joinWithCommaAndSpace (
					message.getTags ()));

			if (
				optionalIsPresent (
					failedMessageOptional)
			) {

				htmlTableDetailsRowWrite (
					"Failure reason",
					failedMessageOptional.get ().getError ());

			}

			htmlTableDetailsRowWriteRaw (
				"User",
				() -> ifNotNullThenElse (
					message.getUser (),
					() -> objectManager.writeTdForObjectMiniLink (
						transaction,
						message.getUser ()),
					() -> htmlTableCellWrite ("—")));

			htmlTableDetailsRowWriteRaw (
				"Delivery type",
				() -> ifNotNullThenElse (
					message.getDeliveryType (),
					() -> objectManager.writeTdForObjectMiniLink (
						transaction,
						message.getDeliveryType ()),
					() -> htmlTableCellWrite ("—")));

			htmlTableClose ();

		}

	}

}
