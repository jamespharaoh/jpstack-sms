package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.module.ConsoleManager;
import wbs.console.tab.ConsoleContextTab;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.WebFile;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@Accessors (fluent = true)
@PrototypeComponent ("messageMediaContext")
public
class MessageMediaContext
	extends ConsoleContext {

	@Inject
	ConsoleManager consoleManager;

	@Inject
	Database database;

	@Inject
	MessageObjectHelper messageHelper;

	@Getter @Setter
	String name;

	@Getter @Setter
	String typeName;

	@Getter @Setter
	String pathPrefix;

	@Getter @Setter
	Boolean global;

	@Getter @Setter
	String parentContextName;

	@Getter @Setter
	String parentContextTabName;

	@Getter @Setter
	Map<String,ConsoleContextTab> contextTabs =
		new LinkedHashMap<String,ConsoleContextTab> ();

	@Getter @Setter
	Map<String,WebFile> files =
		new LinkedHashMap<String,WebFile> ();

	@Override
	public
	String localPathForStuff (
			ConsoleContextStuff contextStuff) {

		return stringFormat (
			"/%u",
			contextStuff.get (
				"messageId"),
			"/%u",
			contextStuff.get (
				"messageMediaIndex"));

	}

	@Override
	public
	String titleForStuff (
			ConsoleContextStuff contextStuff) {

		return stringFormat (
			"Media %s",
			contextStuff.get (
				"messageMediaIndex"));

	}

	@Override
	public
	void initContext (
			PathSupply pathParts,
			ConsoleContextStuff stuff) {

		int messageId =
			Integer.parseInt (
				pathParts.next ());

		int mediaIndex =
			Integer.parseInt (
				pathParts.next ());

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		MessageRec message =
			messageHelper.find (
				messageId);

		MediaRec media =
			message.getMedias ().get (
				mediaIndex);

		stuff.set (
			"messageMediaIndex",
			mediaIndex);

		stuff.set (
			"mediaId",
			media.getId ());

		consoleManager.runPostProcessors (
			"message",
			stuff);

	}

}
