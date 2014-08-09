package wbs.sms.messageset.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.console.lookup.ObjectLookup;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.sms.messageset.model.MessageSetRec;

@Accessors (fluent = true)
@PrototypeComponent ("simpleMessageSetFinder")
public
class SimpleMessageSetFinder
	implements MessageSetFinder {

	@Inject
	ObjectManager objectManager;

	@Inject
	MessageSetConsoleHelper messageSetHelper;

	@Getter @Setter
	ObjectLookup<?> objectLookup;

	@Getter @Setter
	String code;

	@Override
	public
	MessageSetRec findMessageSet (
			ConsoleRequestContext requestContext) {

		Record<?> object =
			(Record<?>)
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		MessageSetRec messageSet =
			messageSetHelper.findByCode (
				object,
				code);

		if (messageSet == null) {

			throw new RuntimeException (
				stringFormat (
					"Can't find message set %s with parent %s",
					code,
					object));

		}

		return messageSet;

	}

}
