package wbs.platform.core.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.ScriptRef;
import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.platform.user.console.UserConsoleLogic;

@Accessors (fluent = true)
@PrototypeComponent ("coreTitledResponder")
public
class CoreTitledResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ExceptionUtils exceptionLogic;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	String title;

	@Getter @Setter
	PagePart pagePart;

	// state

	Throwable pagePartThrew;

	// details

	@Override
	protected
	Set<ScriptRef> scriptRefs () {
		return pagePart.scriptRefs ();
	}

	// implementation

	@Override
	protected
	void setup ()
		throws IOException {

		super.setup ();

		pagePart.setup (
			Collections.<String,Object>emptyMap ());

	}

	@Override
	protected
	void prepare () {

		super.prepare ();

		if (pagePart != null) {

			try {

				pagePart.prepare ();

			} catch (RuntimeException exception) {

				// record the exception

				String path =
					stringFormat (
						"%s%s",
						requestContext.servletPath (),
						requestContext.pathInfo () != null
							? requestContext.pathInfo ()
							: "");

				exceptionLogger.logThrowable (
					"console",
					path,
					exception,
					userConsoleLogic.userId (),
					GenericExceptionResolution.ignoreWithUserWarning);

				// and remember we had a problem

				pagePartThrew =
					exception;

				requestContext.addError (
					"Internal error");

			}

		}

	}

	@Override
	protected
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<link",
			" rel=\"stylesheet\"",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/style/basic.css"),
			">\n");

		pagePart.renderHtmlHeadContent ();

	}

	protected
	void goTab () {
	}

	@Override
	protected
	void renderHtmlBodyContents () {

		printFormat (
			"<h1>%h</h1>\n",
			title);

		requestContext.flushNotices (
			printWriter);

		if (pagePartThrew != null) {

			printFormat (
				"<p>Unable to show page contents.</p>\n");

			if (
				privChecker.canRecursive (
					GlobalId.root,
					"debug")
			) {

				printFormat (
					"<p><pre>%h</pre></p>\n",
					exceptionLogic.throwableDump (
						pagePartThrew));

			}

		} else if (pagePart != null) {

			pagePart.renderHtmlBodyContent ();

		}

	}

}
