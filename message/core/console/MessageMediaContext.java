package wbs.sms.message.core.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.tab.ConsoleContextTab;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.model.MediaRec;

import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

import wbs.web.file.WebFile;

@Accessors (fluent = true)
@PrototypeComponent ("messageMediaContext")
public
class MessageMediaContext
	extends ConsoleContext {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	// properties

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
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleContextStuff contextStuff) {

		return stringFormat (
			"/%u",
			integerToDecimalString (
				(Integer)
				contextStuff.get (
					"messageId")),
			"/%u",
			integerToDecimalString (
				(Integer)
				contextStuff.get (
					"messageMediaIndex")));

	}

	@Override
	public
	String titleForStuff (
			@NonNull ConsoleContextStuff contextStuff) {

		return stringFormat (
			"Media %s",
			integerToDecimalString (
				(Integer)
				contextStuff.get (
					"messageMediaIndex")));

	}

	@Override
	public
	void initContext (
			@NonNull Transaction parentTransaction,
			@NonNull UserPrivChecker privChecker,
			@NonNull PathSupply pathParts,
			@NonNull ConsoleContextStuff stuff) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"initContext");

		) {

			Long messageId =
				Long.parseLong (
					pathParts.next ());

			Integer mediaIndex =
				Integer.parseInt (
					pathParts.next ());

			MessageRec message =
				messageHelper.findRequired (
					transaction,
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
				transaction,
				privChecker,
				"message",
				stuff);

		}

	}

}
