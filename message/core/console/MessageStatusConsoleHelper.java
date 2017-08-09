package wbs.sms.message.core.console;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.enums.EnumConsoleHelper;

import wbs.framework.component.annotations.SingletonComponent;

import wbs.sms.message.core.model.MessageStatus;

@SingletonComponent ("messageStatusConsoleHelper")
public
class MessageStatusConsoleHelper
	extends EnumConsoleHelper<MessageStatus> {

	{

		enumClass (MessageStatus.class);

		auto ();

	}

	@Override
	public
	Optional <String> htmlClass (
			@NonNull MessageStatus messageStatus) {

		if (messageStatus.isGoodType ()) {

			return Optional.of (
				"message-status-succeeded");

		} else if (messageStatus.isBadType ()) {

			return Optional.of (
				"message-status-failed");

		} else {

			return Optional.of (
				"message-status-unknown");

		}

	}

}
