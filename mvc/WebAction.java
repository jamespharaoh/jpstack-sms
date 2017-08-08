package wbs.web.mvc;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

public
interface WebAction {

	WebResponder handle (
			TaskLogger parentTaskLogger);

	default
	Optional <WebResponder> defaultResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return optionalAbsent ();

	}

}
