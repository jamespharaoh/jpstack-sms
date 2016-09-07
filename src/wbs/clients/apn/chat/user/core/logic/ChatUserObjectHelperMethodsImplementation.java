package wbs.clients.apn.chat.user.core.logic;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.logic.ChatNumberReportLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelperMethods;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.RandomLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;

@Log4j
public
class ChatUserObjectHelperMethodsImplementation
	implements ChatUserObjectHelperMethods {

	// singleton dependencies

	@SingletonDependency
	ChatNumberReportLogic chatNumberReportLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	NumberLogic numberLogic;

	@SingletonDependency
	RandomLogic randomLogic;

	// implementation

	@Override
	public
	ChatUserRec findOrCreate (
			@NonNull ChatRec chat,
			@NonNull MessageRec message) {

		// resolve stuff

		NumberRec number =
			message.getNumber ();

		// check for an existing ChatUser

		ChatUserRec chatUser =
			chatUserHelper.find (
				chat,
				number);

		if (chatUser != null) {

			// check number

			if (
				! chatNumberReportLogic.isNumberReportSuccessful (number)
				&& number.getArchiveDate () == null
			) {

				log.debug (
					stringFormat (
						"Number archiving %s code %s",
						number.getNumber (),
						chatUser.getCode ()));

				NumberRec newNumber =
					numberLogic.archiveNumberFromMessage (
						message);

				return create (
					chat,
					newNumber);

			}

			return chatUser;

		}

		return create (
			chat,
			number);

	}

	@Override
	public
	ChatUserRec findOrCreate (
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		// check for an existing ChatUser

		ChatUserRec chatUser =
			chatUserHelper.find (
				chat,
				number);

		if (chatUser != null)
			return chatUser;

		return create (
			chat,
			number);

	}

	@Override
	public
	ChatUserRec create (
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		Transaction transaction =
			database.currentTransaction ();

		// create him

		ChatUserRec chatUser =
			chatUserHelper.createInstance ()

			.setChat (
				chat)

			.setCode (
				randomLogic.generateNumericNoZero (6))

			.setCreated (
				transaction.now ())

			.setNumber (
				number)

			.setOldNumber (
				number)

			.setType (
				ChatUserType.user)

			.setDeliveryMethod (
				ChatMessageMethod.sms)

			.setGender (
				chat.getGender ())

			.setOrient (
				chat.getOrient ())

			.setCreditMode (
				number.getFree ()
					? ChatUserCreditMode.free
					: ChatUserCreditMode.strict);

		// set adult verify on some services
		// TODO this should probably not be here

		if (chat.getAutoAdultVerify ()) {

			chatUserLogic.adultVerify (
				chatUser);

		}

		chatUserHelper.insert (
			chatUser);

		return chatUser;

	}

}