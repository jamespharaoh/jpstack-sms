package wbs.web.mvc;

import static wbs.utils.etc.LogicUtils.attemptWithRetriesVoid;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
@PrototypeComponent ("webResponderRequestHandler")
public
class WebResponderRequestHandler
	implements WebRequestHandler {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WebExceptionHandler webExceptionHandler;

	// properties

	@Getter @Setter
	ComponentProvider <? extends WebResponder> responderProvider;

	// details

	public final static
	long maxAttempts = 5;

	public final static
	Duration backoffDuration =
		Duration.millis (10l);

	// public implementation

	@Override
	public
	void handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			attemptWithRetriesVoid (
				maxAttempts,
				backoffDuration,

				() -> {

					WebResponder responder =
						responderProvider.provide (
							taskLogger);

					responder.execute (
						taskLogger);

				},

				(attempt, exception) ->
					webExceptionHandler.handleExceptionRetry (
						taskLogger,
						attempt,
						exception),

				(attempt, exception) ->
					webExceptionHandler.handleExceptionFinal (
						taskLogger,
						attempt,
						exception)

			);

		} catch (InterruptedException interruptedException) {

			Thread.currentThread ().interrupt ();

		}

	}

}
