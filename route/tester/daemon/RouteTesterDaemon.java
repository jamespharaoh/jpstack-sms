package wbs.sms.route.tester.daemon;

import static wbs.framework.utils.etc.TimeUtils.laterThan;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.tester.model.RouteTestObjectHelper;
import wbs.sms.route.tester.model.RouteTestRec;
import wbs.sms.route.tester.model.RouteTesterObjectHelper;
import wbs.sms.route.tester.model.RouteTesterRec;

@SingletonComponent ("routeTestDaemon")
public
final class RouteTesterDaemon
	extends AbstractDaemonService {

	// dependencies

	@Inject
	Database database;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	RouteTestObjectHelper routeTestHelper;

	@Inject
	RouteTesterObjectHelper routeTesterHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSender;

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

			doIt ();

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				return;
			}

		}

	}

	private
	void doIt () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"RouteTesterDaemon.doIt ()",
				this);

		// retrieve all route testers

		List<RouteTesterRec> routeTesters =
			routeTesterHelper.findAll ();

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
				routeTester);

		}

		transaction.commit ();

	}

	private
	void doOne (
			RouteTesterRec routeTester) {

		Transaction transaction =
			database.currentTransaction ();

		// create the RouteTest

		RouteTestRec routeTest =
			routeTestHelper.insert (
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

		text.append ("ROUTETEST ID=" + routeTest.getId ());

		// send the message

		MessageRec message =
			messageSender.get ()

			.number (
				numberHelper.findOrCreate (
					routeTester.getDestNumber ()))

			.messageString (
				text.toString ())

			.numFrom (
				routeTester.getRouteNumber ())

			.route (
				routeTester.getRoute ())

			.service (
				serviceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.send ();

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
