package wbs.sms.magicnumber.logic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.Collection;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

@SingletonComponent ("magicNumberLogic")
public
class MagicNumberLogicImplementation
	implements MagicNumberLogic {

	// singleton dependencies

	@SingletonDependency
	LockLogic coreLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MagicNumberObjectHelper magicNumberHelper;

	@SingletonDependency
	MagicNumberUseObjectHelper magicNumberUseHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

	// implementation

	@Override
	public
	MagicNumberRec allocateMagicNumber (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec command,
			long ref) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"allocateMagicNumber");

		) {

			BorrowedTransaction transaction =
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
						taskLogger,
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

				taskLogger.fatalFormat (
					"No magic numbers found for %s",
					integerToDecimalString (
						magicNumberSet.getId ()));

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

	}

	@Override
	public
	Long sendMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec magicCommand,
			@NonNull Long magicRef,
			@NonNull Optional <Long> threadId,
			@NonNull Collection <TextRec> parts,
			@NonNull RouterRec router,
			@NonNull ServiceRec service,
			@NonNull Optional <BatchRec> batch,
			@NonNull AffiliateRec affiliate,
			@NonNull Optional <UserRec> user) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"sendMessage");

		) {

			// allocate a magic number

			MagicNumberRec magicNumber =
				allocateMagicNumber (
					taskLogger,
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

					.send (
						taskLogger);

				if (
					optionalIsNotPresent (
						threadId)
				) {

					threadId =
						optionalOf (
							message.getId ());

				}

			}

			return threadId.get ();

		}

	}

	@Override
	public
	MessageRec sendMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec magicCommand,
			long magicRef,
			@NonNull Optional <Long> threadId,
			@NonNull TextRec messageText,
			@NonNull RouterRec router,
			@NonNull ServiceRec service,
			@NonNull Optional <BatchRec> batch,
			@NonNull AffiliateRec affiliate,
			@NonNull Optional <UserRec> user) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"sendMessage");

		) {

			// allocate a magic number

			MagicNumberRec magicNumber =
				allocateMagicNumber (
					taskLogger,
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

				.send (
					taskLogger);

		}

	}

}
