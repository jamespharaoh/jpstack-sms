package wbs.web.responder;

import static wbs.utils.collection.MapUtils.emptyMap;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;

public
interface WebModule {

	default
	Map <String, PathHandler> webModulePaths (
			@NonNull TaskLogger parentTaskLogger) {

		return emptyMap ();

	}

	default
	Map <String, WebFile> webModuleFiles (
			@NonNull TaskLogger parentTaskLogger) {

		return emptyMap ();

	}

}
