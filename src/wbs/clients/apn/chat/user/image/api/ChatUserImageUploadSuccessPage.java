package wbs.clients.apn.chat.user.image.api;

import java.io.IOException;

import com.google.common.collect.ImmutableMap;

import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.clients.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.web.PrintResponder;
import wbs.framework.web.RequestContext;

@PrototypeComponent ("chatUserImageUploadSuccessPage")
public
class ChatUserImageUploadSuccessPage
	extends PrintResponder {

	// singleton dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	RequestContext requestContext;

	// state

	ChatUserImageUploadTokenRec imageUploadToken;
	ChatUserRec chatUser;

	String titleText;
	String bodyHtml;

	// implementation

	@Override
	protected
	void prepare () {

		imageUploadToken =
			chatUserImageUploadTokenHelper.findByToken (
				(String)
				requestContext.requestRequired (
					"chatUserImageUploadToken"));

		chatUser =
			imageUploadToken.getChatUser ();

		titleText =
			chatSendLogic.renderTemplate (
				chatUser,
				"web",
				"image_upload_success_title",
				ImmutableMap.<String,String> of ());

		bodyHtml =
			chatSendLogic.renderTemplate (
				chatUser,
				"web",
				"image_upload_success_body",
				ImmutableMap.<String,String> of ());

	}

	@Override
	protected
	void goHeaders ()
		throws IOException {

		requestContext.addHeader (
			"Content-Type",
			"text/html");

	}

	@Override
	protected
	void goContent ()
		throws IOException {

		printFormat (
			"<!DOCTYPE html>\n");

		printFormat (
			"<html>\n");

		goHead ();

		goBody ();

		printFormat (
			"</html>\n");

	}

	protected
	void goHead () {

		printFormat (
			"<head>\n");

		printFormat (
			"<title>%h</title>\n",
			titleText);

		printFormat (
			"</head>\n");

	}

	protected
	void goBody () {

		printFormat (
			"<body>\n");

		printFormat (
			"<h1>%h</h1>\n",
			titleText);

		printFormat (
			"%s\n",
			bodyHtml);

		printFormat (
			"</body>\n");

	}

}
