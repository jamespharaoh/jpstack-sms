package wbs.sms.messageset.console;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
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

		return messageSetHelper.findByCodeRequired (
			object,
			code);

	}

}
