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
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

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

import wbs.utils.string.FormatWriter;

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
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

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
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			// open table

			htmlTableOpenDetails (
				formatWriter);

			// write id

			htmlTableDetailsRowWrite (
				formatWriter,
				"ID",
				integerToDecimalString (
					message.getId ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Thread ID",
				integerToDecimalString (
					message.getThreadId ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Other ID",
				ifNull (
					message.getOtherId (),
					"—"));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
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
					formatWriter,
					"Number from",
					() ->
						objectManager.writeTdForObjectMiniLink (
							transaction,
							formatWriter,
							privChecker,
							message.getNumber ()));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Number to",
					message.getNumTo ());

			} else {

				htmlTableDetailsRowWrite (
					formatWriter,
					"Number from",
					message.getNumFrom ());

				htmlTableDetailsRowWriteRaw (
					formatWriter,
					"Number to",
					() ->
						objectManager.writeTdForObjectMiniLink (
							transaction,
							formatWriter,
							privChecker,
							message.getNumber ()));

			}

			htmlTableDetailsRowWrite (
				formatWriter,
				"Status",
				message.getStatus ().getDescription ());

			htmlTableDetailsRowWrite (
				formatWriter,
				"Direction",
				message.getDirection ().name ());

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Route",
				() ->
					objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						privChecker,
						message.getRoute ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Network",
				message.getNetwork ().getDescription ());

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Service",
				() ->
					objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						privChecker,
						message.getService ()));

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Affiliate",
				() ->
					objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						privChecker,
						message.getAffiliate ()));

			if (
				enumEqualSafe (
					message.getDirection (),
					MessageDirection.in)
			) {

				htmlTableDetailsRowWrite (
					formatWriter,
					"Time sent",
					ifNotNullThenElseEmDash (
						message.getNetworkTime (),
						() -> userConsoleLogic.timestampWithTimezoneString (
							transaction,
							message.getNetworkTime ())));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Time received",
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						message.getCreatedTime ()));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Time processed",
					ifNotNullThenElseEmDash (
						message.getProcessedTime (),
						() ->
							userConsoleLogic.timestampWithTimezoneString (
								transaction,
								message.getProcessedTime ())));

				htmlTableDetailsRowWriteRaw (
					formatWriter,
					"Command",
					() -> ifNotNullThenElse (
						message.getCommand (),
						() -> objectManager.writeTdForObjectMiniLink (
							transaction,
							formatWriter,
							privChecker,
							message.getCommand ()),
						() -> htmlTableCellWrite (
							formatWriter,
							"—")));

			} else {

				htmlTableDetailsRowWrite (
					formatWriter,
					"Time created",
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						message.getCreatedTime ()));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Time sent",
					ifNotNullThenElseEmDash (
						message.getProcessedTime (),
						() ->
							userConsoleLogic.timestampWithTimezoneString (
								transaction,
								message.getProcessedTime ())));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Time received",
					ifNotNullThenElseEmDash (
						message.getNetworkTime (),
						() ->
							userConsoleLogic.timestampWithTimezoneString (
								transaction,
								message.getNetworkTime ())));

			}

			htmlTableDetailsRowWriteRaw (
				formatWriter,
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
							formatWriter,
							"Media",
							utf8ToString (
								media.getContent ().getData ()));

					} else {

						htmlTableDetailsRowWriteRaw (
							formatWriter,
							"Media",
							() -> {

							formatWriter.writeLineFormatIncreaseIndent (
								"<td>");

							htmlLinkWriteHtml (
								formatWriter,
								requestContext.resolveLocalUrlFormat (
									"/message.mediaSummary?index=%u",
									integerToDecimalString (
										mediaIndex)),
								() -> mediaConsoleLogic.writeMediaThumb100 (
									transaction,
									formatWriter,
									media));

							formatWriter.writeLineFormatDecreaseIndent (
								"</td>");

						});

					}

				}

			}

			htmlTableDetailsRowWrite (
				formatWriter,
				"Tags",
				joinWithCommaAndSpace (
					message.getTags ()));

			if (
				optionalIsPresent (
					failedMessageOptional)
			) {

				htmlTableDetailsRowWrite (
					formatWriter,
					"Failure reason",
					failedMessageOptional.get ().getError ());

			}

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"User",
				() -> ifNotNullThenElse (
					message.getUser (),
					() -> objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						privChecker,
						message.getUser ()),
					() -> htmlTableCellWrite (
						formatWriter,
						"—")));

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Delivery type",
				() -> ifNotNullThenElse (
					message.getDeliveryType (),
					() -> objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						privChecker,
						message.getDeliveryType ()),
					() -> htmlTableCellWrite (
						formatWriter,
						"—")));

			htmlTableClose (
				formatWriter);

		}

	}

}
