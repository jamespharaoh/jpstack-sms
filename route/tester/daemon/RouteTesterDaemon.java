package wbs.sms.route.tester.daemon;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.number.core.model.NumberObjectHelper;
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
	RouteTesterObjectHelper routeTesterHelper;

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
			database.beginReadWrite ();

		// retrieve all route testers

		List<RouteTesterRec> routeTesters =
			routeTesterHelper.findAll ();

		long now =
			System.currentTimeMillis ();

		// for each one...

		for (RouteTesterRec routeTester
				: routeTesters) {

			// if it's had a test recently skip it

			if (routeTester.getLastTest () != null) {

				long lastTest =
					routeTester.getLastTest ().getTime ();

				long nextTest =
					lastTest + 1000 * routeTester.getIntervalSecs ();

				if (nextTest > now)
					continue;

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

		Date now =
			new Date ();

		// create the RouteTest

		RouteTestRec routeTest =
			routeTesterHelper.insert (
				new RouteTestRec ()
					.setRoute (routeTester.getRoute ())
					.setSentTime (now));

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
				objectManager.findChildByCode (
					ServiceRec.class,
					new GlobalId (0, 0),
					"test"))

			.send ();

		// connect the message to the RouteTest

		routeTest.setSentMessage (message);

		// and update the tester's last test time

		routeTester.setLastTest (now);

	}

}
