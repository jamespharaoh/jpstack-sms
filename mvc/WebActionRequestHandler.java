package wbs.web.mvc;

import static wbs.utils.etc.LogicUtils.attemptWithRetries;
import static wbs.utils.etc.LogicUtils.attemptWithRetriesVoid;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("webActionRequestHandler")
public
class WebActionRequestHandler
	implements WebRequestHandler {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WebExceptionHandler webExceptionHandler;

	// properties

	ComponentProvider <WebAction> actionProvider;

	// details

	public final static
	long maxAttempts = 5;

	public final static
	Duration backoffDuration =
		Duration.millis (10l);

	// property setters

	public
	WebActionRequestHandler actionProvider (
			@NonNull ComponentProvider <WebAction> actionProvider) {

		this.actionProvider =
			actionProvider;

		return this;

	}

	public
	WebActionRequestHandler actionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		return actionProvider (
			componentManager.getComponentProviderRequired (
				parentTaskLogger,
				actionName,
				WebAction.class));

	}

	// implementation

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

			WebResponder responder =
				runAction (
					taskLogger);

			runResponder (
				taskLogger,
				responder);

		} catch (InterruptedException interrupedException) {

			Thread.currentThread ().interrupt ();

		}

	}

	// private implementation

	private
	WebResponder runAction (
			@NonNull TaskLogger parentTaskLogger)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runAction");

		) {

			return attemptWithRetries (
				maxAttempts,
				backoffDuration,

				() -> {

					WebAction action =
						actionProvider.provide (
							taskLogger);

					return action.handle (
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

		}

	}

	private
	void runResponder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull WebResponder responder)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runResponder");

		) {

			attemptWithRetriesVoid (
				maxAttempts,
				backoffDuration,

				() ->
					responder.execute (
						taskLogger),

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

		}

	}

}
