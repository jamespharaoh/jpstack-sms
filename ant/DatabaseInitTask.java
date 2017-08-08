package wbs.utils.ant;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import lombok.Data;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.component.manager.BootstrapComponentManager;
import wbs.framework.logging.Log4jLogTargetFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LoggingLogic;
import wbs.framework.logging.LoggingLogicImplementation;
import wbs.framework.logging.OwnedTaskLogger;

public
class DatabaseInitTask
	extends Task {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	List <ScriptElement> scriptElements =
		new ArrayList<> ();

	// implementation

	public
	void addScript (
			ScriptElement scriptElement) {

		scriptElements.add (
			scriptElement);

	}

	@Override
	public
	void init () {

		LoggingLogic loggingLogic =
			new LoggingLogicImplementation (
				false,
				ImmutableList.of (
					new Log4jLogTargetFactory ()));

		try (

			BootstrapComponentManager bootstrapComponentManager =
				new BootstrapComponentManager (
					loggingLogic);

			OwnedTaskLogger taskLogger =
				bootstrapComponentManager.bootstrapTaskLogger (
					this);

		) {

			bootstrapComponentManager.registerStandardClasses (
				taskLogger);

			bootstrapComponentManager.bootstrapComponent (
				taskLogger,
				this);

			doNothing ();

		}

	}

	@Override
	public
	void execute ()
		throws BuildException {

		try {

			initDatabase ();

		} catch (InterruptedException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	private
	void shell (
			String command)
		throws InterruptedException {

		try {

			String args[] = {
				"bash",
				"-c",
				command
			};

			Process proc =
				new ProcessBuilder ()
					.directory (getProject ().getBaseDir ())
					.command (args)
					.start ();

			gobbleStream (
				proc.getInputStream (),
				"OUT");

			gobbleStream (
				proc.getErrorStream (),
				"ERR");

			int result =
				proc.waitFor ();

			if (result != 0) {

				throw new RuntimeException (
					stringFormat (
						"Command \"%s\" returned %s",
						command,
						integerToDecimalString (
							result)));

			}

		} catch (IOException ioException) {

			throw new RuntimeException (
				ioException);

		}

	}

	public
	void initDatabase ()
		throws InterruptedException {

		List <String> parts =
			new ArrayList<> ();

		for (
			ScriptElement scriptElement
				: scriptElements
		) {

			File scriptFile =
				new File (
					stringFormat (
						"%s/%s",
						getProject ().getBaseDir ().getPath (),
						scriptElement.getName ()));

			if (! scriptFile.exists ()) {

				throw new RuntimeException (
					stringFormat (
						"No such file: %s",
						scriptElement.getName ()));

			}

			parts.add (
				stringFormat (
					Joiner.on ("; ").join (
						"echo '\\echo'",
						stringFormat (
							"echo '\\echo RUN SCRIPT %s'",
							scriptElement.getName ()),
						"echo '\\echo'",
						stringFormat (
							"cat %s",
							scriptElement.getName ()))));

		}

		String command =
			stringFormat (
				"( %s; %s ) | %s",
				"echo '\\set ON_ERROR_STOP 1'",
				Joiner.on ("; ").join (parts),
				stringFormat (
					"psql %s",
					wbsConfig.database ().databaseName ()));

		System.out.println (
			command);

		shell (
			command);

	}

	private
	void gobbleStream (
			InputStream in,
			String type) {

		StreamGobbler gobbler =
			new StreamGobbler (in, type);

		gobbler.start ();

	}

	private
	class StreamGobbler
		extends Thread {

		InputStream inputStream;
		String type;

		private
		StreamGobbler (
				InputStream inputStream,
				String type) {

			this.inputStream =
				inputStream;

			this.type =
				type;

		}

		@Override
		public
		void run () {

			try {

				InputStreamReader inputStreamReader =
					new InputStreamReader (
						inputStream);

				BufferedReader bufferedReader =
					new BufferedReader (
						inputStreamReader);

				String line =
					null;

				while ((line = bufferedReader.readLine ()) != null) {

					System.out.println (type + "> " + line);

				}

			} catch (IOException exception) {

				exception.printStackTrace ();

			}

		}

	}

	@Data
	public static
	class ScriptElement {
		String name;
	}

}