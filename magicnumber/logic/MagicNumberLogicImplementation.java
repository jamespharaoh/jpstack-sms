package wbs.sms.magicnumber.logic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.lock.logic.LockLogic;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.command.model.CommandRec;
import wbs.sms.magicnumber.model.MagicNumberObjectHelper;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.magicnumber.model.MagicNumberUseObjectHelper;
import wbs.sms.magicnumber.model.MagicNumberUseRec;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.router.model.RouterRec;

@Log4j
@SingletonComponent ("magicNumberLogic")
public
class MagicNumberLogicImplementation
	implements MagicNumberLogic {

	// singleton dependencies

	@SingletonDependency
	LockLogic coreLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	MagicNumberObjectHelper magicNumberHelper;

	@SingletonDependency
	MagicNumberUseObjectHelper magicNumberUseHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

	// implementation

	/**
	 * Allocate a magic number for a given command from a given magic number set
	 * for the given recipient. Will reuse an existing allocation if possible.
	 *
	 * @param magicNumberSet
	 *            the magic number set to allocate from.
	 * @param number
	 *            the number object representing the recipient.
	 * @param command
	 *            the command to associate with the magic number for this
	 *            recipient.
	 * @param ref
	 *            an integer reference to be passed to the command.
	 * @return the MagicNumber object representing the allocation.
	 */
	@Override
	public
	MagicNumberRec allocateMagicNumber (
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec command,
			long ref) {

		Transaction transaction =
			database.currentTransaction ();

		// create a lock over the magic number set and number

		coreLogic.magicLock (
			magicNumberSet,
			number);

		// lookup an existing use

		MagicNumberUseRec magicNumberUse =
			magicNumberUseHelper.findExistingByRef (
				magicNumberSet,
				number,
				command,
				ref);

		if (

			magicNumberUse != null

			&& ! magicNumberUse.getMagicNumber ().getDeleted ()

		) {

			magicNumberUse

				.setLastUseTimestamp (
					transaction.now ());

			return magicNumberUse.getMagicNumber ();

		}

		// lookup an unused magic number

		MagicNumberRec magicNumber =
			magicNumberHelper.findExistingUnused (
				magicNumberSet,
				number);

		if (magicNumber != null) {

			magicNumberUse =
				magicNumberUseHelper.insert (
					magicNumberUseHelper.createInstance ()

				.setNumber (
					number)

				.setMagicNumber (
					magicNumber)

				.setCommand (
					command)

				.setRefId (
					ref)

				.setLastUseTimestamp (
					transaction.now ())

			);

			return magicNumber;

		}

		// ok, reallocate the least-recently-used one

		magicNumberUse =
			magicNumberUseHelper.findExistingLeastRecentlyUsed (
				magicNumberSet,
				number);

		if (magicNumberUse == null) {

			log.fatal (
				stringFormat (
					"No magic numbers found for %s",
					integerToDecimalString (
						magicNumberSet.getId ())));

			return null;

		}

		magicNumberUse

			.setCommand (
				command)

			.setRefId (
				ref)

			.setLastUseTimestamp (
				transaction.now ());

		return magicNumberUse.getMagicNumber ();

	}

	@Override
	public
	Long sendMessage (
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec magicCommand,
			@NonNull Long magicRef,
			@NonNull Optional<Long> threadId,
			@NonNull Collection<TextRec> parts,
			@NonNull RouterRec router,
			@NonNull ServiceRec service,
			@NonNull Optional<BatchRec> batch,
			@NonNull AffiliateRec affiliate,
			@NonNull Optional<UserRec> user) {

		// allocate a magic number

		MagicNumberRec magicNumber =
			allocateMagicNumber (
				magicNumberSet,
				number,
				magicCommand,
				magicRef);

		// and send parts

		for (
			TextRec part
				: parts
		) {

			MessageRec message =
				messageSender.get ()

				.threadId (
					threadId.orNull ())

				.number (
					number)

				.messageText (
					part)

				.numFrom (
					magicNumber.getNumber ())

				.routerResolve (
					router)

				.service (
					service)

				.batch (
					batch.orNull ())

				.affiliate (
					affiliate)

				.user (
					user.orNull ())

				.send ();

			if (
				optionalIsNotPresent (
					threadId)
			) {

				threadId =
					Optional.of (
						message.getId ());

			}

		}

		return threadId.get ();

	}

	@Override
	public
	MessageRec sendMessage (
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec magicCommand,
			long magicRef,
			@NonNull Optional<Long> threadId,
			@NonNull TextRec messageText,
			@NonNull RouterRec router,
			@NonNull ServiceRec service,
			@NonNull Optional<BatchRec> batch,
			@NonNull AffiliateRec affiliate,
			@NonNull Optional<UserRec> user) {

		// allocate a magic number

		MagicNumberRec magicNumber =
			allocateMagicNumber (
				magicNumberSet,
				number,
				magicCommand,
				magicRef);

		// and send message

		return messageSender.get ()

			.threadId (
				threadId.orNull ())

			.number (
				number)

			.messageText (
				messageText)

			.numFrom (
				magicNumber.getNumber ())

			.routerResolve (
				router)

			.service (
				service)

			.batch (
				batch.orNull ())

			.affiliate (
				affiliate)

			.send ();

	}

}
