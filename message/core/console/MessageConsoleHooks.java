package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.moreThan;

import com.google.common.base.Optional;

import wbs.console.helper.AbstractConsoleHooks;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;

@SingletonComponent ("messageConsoleHooks")
public
class MessageConsoleHooks
	extends AbstractConsoleHooks<MessageRec> {

	@Override
	public
	Optional<String> getListClass (
			MessageRec message) {

		if (
			equal (
				message.getDirection (),
				MessageDirection.in)
		) {

			return Optional.of (
				"message-in");

		} else if (
			moreThan (
				message.getCharge (),
				0)
		) {

			return Optional.of (
				"message-out-charge");

		} else {

			return Optional.of (
				"message-out");

		}

	}

}
