package wbs.sms.messageset.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;

import wbs.sms.messageset.model.MessageSetRec;

@Accessors (fluent = true)
@PrototypeComponent ("simpleMessageSetFinder")
public
class SimpleMessageSetFinder
	implements MessageSetFinder {

	// singleton dependencies

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	MessageSetConsoleHelper messageSetHelper;

	// properties

	@Getter @Setter
	ObjectLookup<?> objectLookup;

	@Getter @Setter
	String code;

	// implementation

	@Override
	public
	MessageSetRec findMessageSet (
			@NonNull ConsoleRequestContext requestContext) {

		Record <?> object =
			genericCastUnchecked (
				objectLookup.lookupObject (
					requestContext.consoleContextStuffRequired ()));

		return messageSetHelper.findByCodeRequired (
			object,
			code);

	}

}
