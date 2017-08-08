package wbs.utils.email;

import static wbs.utils.string.StringUtils.stringIsNotEmpty;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import com.sun.mail.smtp.SMTPMessage;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;

@SingletonComponent ("emailLogic")
@Accessors (fluent = true)
public
class EmailLogicImplementation
	implements EmailLogic {

	// singleton dependencies

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	Session mailSession;

	// life cycle

	@NormalLifecycleSetup
	void setup () {

		Properties properties =
			new Properties (
				System.getProperties ());

		if (
			stringIsNotEmpty (
				wbsConfig.email ().smtpHostname ())
		) {

			properties.setProperty (
				"mail.smtp.host",
				wbsConfig.email ().smtpHostname ());

		}

		if (
			stringIsNotEmpty (
				wbsConfig.email ().smtpPort ())
		) {

			properties.setProperty (
				"mail.smtp.port",
				wbsConfig.email ().smtpPort ());

		}

		if (
			stringIsNotEmpty (
				wbsConfig.email ().smtpUsername ())
		) {

			throw new RuntimeException ();

		}

		if (
			stringIsNotEmpty (
				wbsConfig.email ().smtpPassword ())
		) {

			throw new RuntimeException ();

		}

		mailSession =
			Session.getInstance (
				properties);

	}

	// implementation

	@Override
	public
	void sendEmail (
			@NonNull String fromName,
			@NonNull String fromAddress,
			@NonNull String replyToAddress,
			@NonNull Collection<String> toAddresses,
			@NonNull String subjectText,
			@NonNull String messageText) {

		try {

			SMTPMessage smtpMessage =
				new SMTPMessage (
					mailSession);

			smtpMessage.setEnvelopeFrom (
				wbsConfig.email ().defaultEnvelopeFrom ());

			try {

				smtpMessage.setFrom (
					new InternetAddress (
						fromAddress,
						fromName));

			} catch (UnsupportedEncodingException exception) {

				throw new RuntimeException (
					exception);

			}

			smtpMessage.setSubject (
				subjectText);

			for (
				String toAddress
					: toAddresses
			) {

				smtpMessage.addRecipient (
					Message.RecipientType.TO,
					new InternetAddress (
						toAddress,
						true));

			}

			smtpMessage.setText (
				messageText,
				"utf-8");

			Transport.send (
				smtpMessage);

		} catch (MessagingException exception) {

			throw new RuntimeException (exception);

		}

	}

	@Override
	public
	void sendSystemEmail (
			@NonNull Collection<String> toAddresses,
			@NonNull String subject,
			@NonNull String message) {

		sendEmail (
			wbsConfig.email ().defaultFromName (),
			wbsConfig.email ().defaultFromAddress (),
			wbsConfig.email ().defaultReplyToAddress (),
			toAddresses,
			subject,
			message);

	}

}
