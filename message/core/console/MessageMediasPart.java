package wbs.sms.message.core.console;

import static wbs.utils.etc.LogicUtils.ifNullThenEmDash;
import static wbs.utils.etc.Misc.prettySize;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageMediasPart")
public
class MessageMediasPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	// state

	MessageRec message;
	List<MediaRec> medias;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

		medias =
			message.getMedias ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Thumbnail",
			"Type",
			"Filename",
			"Size");

		if (medias.size () == 0) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				"(no media)",
				htmlColumnSpanAttribute (4l));

			htmlTableRowClose ();

		} else {

			for (
				int index = 0;
				index < medias.size ();
				index ++
			) {

				MediaRec media =
					medias.get (index);

				htmlTableRowOpen (
					htmlClassAttribute (
						"magic-table-row"),
					htmlDataAttribute (
						"target-href",
						requestContext.resolveLocalUrl (
							stringFormat (
								"/message.mediaSummary",
								"?index=%u",
								index))));

				htmlTableCellOpen ();

				mediaConsoleLogic.writeMediaThumb100 (
					media);

				htmlTableCellClose ();

				htmlTableCellWrite (
					media.getMediaType ().getMimeType ());

				htmlTableCellWrite (
					ifNullThenEmDash (
						media.getFilename ()));

				htmlTableCellWrite (
					prettySize (
						media.getContent().getData ().length));

				htmlTableRowClose ();

			}

		}

		htmlTableClose ();

	}

}
