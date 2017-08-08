package wbs.web.pathhandler;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringEndsWithSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.file.WebFile;
import wbs.web.responder.WebModule;

/**
 * Implementation of PathHandler which delegates to other PathHandlers or
 * WebFiles based on simple string mappings.
 */
@PrototypeComponent ("delegatingPathHandler")
public
class DelegatingPathHandler
	implements PathHandler {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	Map <String, WebModule> servletModules;

	// properties

	@Getter @Setter
	Map <String, PathHandler> paths =
		new HashMap<> ();

	@Getter @Setter
	Map <String, WebFile> files =
		new HashMap<> ();

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			Map <String, String> pathDeclaredByModule =
				new HashMap<> ();

			Map <String, String> fileDeclaredByModule =
				new HashMap<> ();

			// for each one...

			for (
				Map.Entry <String, WebModule> servletModuleEntry
					: servletModules.entrySet ()
			) {

				String servletModuleName =
					servletModuleEntry.getKey ();

				WebModule servletModule =
					servletModuleEntry.getValue ();

				// import all its paths

				Map <String, ? extends PathHandler> modulePaths =
					servletModule.webModulePaths (
						taskLogger);

				if (modulePaths != null) {

					for (
						Map.Entry <String, ? extends PathHandler> modulePathEntry
							: modulePaths.entrySet ()
					) {

						String modulePathName =
							modulePathEntry.getKey ();

						PathHandler modulePathHandler =
							modulePathEntry.getValue ();

						if (
							pathDeclaredByModule.containsKey (
								modulePathName)
						) {

							throw new RuntimeException (
								stringFormat (
									"Duplicated path '%s' (in %s and %s)",
									modulePathName,
									pathDeclaredByModule.get (
										modulePathName),
									servletModuleName));

						}

						pathDeclaredByModule.put (
							modulePathName,
							servletModuleName);

						taskLogger.debugFormat (
							"Adding path %s",
							modulePathName);

						paths.put (
							modulePathName,
							modulePathHandler);

					}

				}

				// import all its files

				Map <String, ? extends WebFile> moduleFiles =
					servletModule.webModuleFiles (
						taskLogger);

				if (moduleFiles != null) {

					for (
						Map.Entry <String, ? extends WebFile> moduleFileEntry
							: moduleFiles.entrySet ()
					) {


						String moduleFileName =
							moduleFileEntry.getKey ();

						if (
							fileDeclaredByModule.containsKey (
								moduleFileName)
						) {

							throw new RuntimeException (
								stringFormat (
									"Duplicated file '%s' (in %s and %s)",
									moduleFileName,
									fileDeclaredByModule.get (
										moduleFileName),
									servletModuleName));

						}

						fileDeclaredByModule.put (
							moduleFileName,
							servletModuleName);

						taskLogger.debugFormat (
							"Adding file %s",
							moduleFileName);

						files.put (
							moduleFileName,
							moduleFileEntry.getValue ());

					}

				}

			}

		}

	}

	@Override
	public
	WebFile processPath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String originalPath) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processPath");

		) {

			taskLogger.debugFormat (
				"processPath (\"%s\")",
				originalPath);

			// strip any trailing '/'


			String currentPath =
				ifThenElse (
					stringEndsWithSimple (
						"/",
						originalPath),
					() -> originalPath.substring (
						0,
						originalPath.length () - 1),
					() -> originalPath);

			// check for a file with the exact path

			if (files != null) {

				WebFile webFile =
					files.get (
						currentPath);

				if (webFile != null) {
					return webFile;
				}

			}

			// ok, look for a handler, and keep stripping off bits until we find one

			if (paths != null) {

				String remain = "";

				while (true) {

					PathHandler pathHandler =
						paths.get (
							currentPath);

					if (pathHandler != null) {

						return pathHandler.processPath (
							taskLogger,
							remain);

					}

					int slashPosition =
						currentPath.lastIndexOf (
							'/');

					if (slashPosition == 0)
						return null;

					if (slashPosition == -1)
						return null;

					remain =
						joinWithoutSeparator (
							currentPath.substring (
								slashPosition),
							remain);

					currentPath =
						currentPath.substring (
							0,
							slashPosition);

				}

			}

			return null;

		}

	}

}
