package wbs.sms.route.tester.daemon;

import static wbs.utils.time.TimeUtils.laterThan;

import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.service.model.ServiceObjectHelper;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.tester.model.RouteTestObjectHelper;
import wbs.sms.route.tester.model.RouteTestRec;
import wbs.sms.route.tester.model.RouteTesterObjectHelper;
import wbs.sms.route.tester.model.RouteTesterRec;

@SingletonComponent ("routeTestDaemon")
public
final class RouteTesterDaemon
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RouteTestObjectHelper routeTestHelper;

	@SingletonDependency
	RouteTesterObjectHelper routeTesterHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

	// details

	@Override
	protected
	String getThreadName () {
		return "RouteTester";
	}

	@Override
	protected
	void runService () {

		while (true) {

			runOnce ();

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				return;
			}

		}

	}

	private
	void runOnce () {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					"runOnce");

		) {

			// retrieve all route testers

			List <RouteTesterRec> routeTesters =
				routeTesterHelper.findAll (
					transaction);

			// for each one...

			for (
				RouteTesterRec routeTester
					: routeTesters
			) {

				// if it's had a test recently skip it

				if (routeTester.getLastTest () != null) {

					Instant lastTest =
						routeTester.getLastTest ();

					Instant nextTest =
						lastTest.plus (
							Duration.standardSeconds (
								routeTester.getIntervalSecs ()));

					if (
						laterThan (
							nextTest,
							transaction.now ())
					) {
						continue;
					}

				}

				// ok, do this one

				doOne (
					transaction,
					routeTester);

			}

			transaction.commit ();

		}

	}

	private
	void doOne (
			@NonNull Transaction parentTransaction,
			@NonNull RouteTesterRec routeTester) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"doOne");

		) {

			// create the RouteTest

			RouteTestRec routeTest =
				routeTestHelper.insert (
					transaction,
					routeTestHelper.createInstance ()

				.setRoute (
					routeTester.getRoute ())

				.setSentTime (
					transaction.now ())

			);

			// construct the message text

			StringBuilder text =
				new StringBuilder ();

			if (routeTester.getDestKeyword ().length () > 0) {

				text.append (
					routeTester.getDestKeyword ());

				text.append (
					' ');

			}

			text.append (
				"ROUTETEST ID=" + routeTest.getId ());

			// send the message

			MessageRec message =
				messageSender.get ()

				.number (
					numberHelper.findOrCreate (
						transaction,
						routeTester.getDestNumber ()))

				.messageString (
					transaction,
					text.toString ())

				.numFrom (
					routeTester.getRouteNumber ())

				.route (
					routeTester.getRoute ())

				.service (
					serviceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test"))

				.send (
					transaction);

			// connect the message to the RouteTest

			routeTest

				.setSentMessage (
					message);

			// and update the tester's last test time

			routeTester

				.setLastTest (
					transaction.now ());

		}

	}

}
