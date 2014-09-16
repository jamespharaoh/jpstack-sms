package wbs.sms.message.delivery.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.daemon.QueueBuffer;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;
import wbs.sms.message.delivery.model.DeliveryTypeRec;

@SingletonComponent ("deliveryDaemon")
public
class DeliveryDaemon
	extends AbstractDaemonService {

	// dependencies

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	DeliveryTypeObjectHelper deliveryTypeHelper;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	Map<String,Provider<DeliveryHandler>> handlersByBeanName =
		Collections.emptyMap ();

	// properties

	@Getter @Setter
	int bufferSize = 128;

	@Getter @Setter
	int numWorkerThreads = 4;

	// state

	QueueBuffer<Integer,DeliveryRec> buffer;
	Map<Integer,DeliveryHandler> handlersById;

	// implementation

	@Override
	protected
	void init () {

		buffer =
			new QueueBuffer<Integer,DeliveryRec> (
				bufferSize);

		handlersById =
			new HashMap<Integer,DeliveryHandler> ();

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		for (Map.Entry<String,Provider<DeliveryHandler>> handlerEntry
				: handlersByBeanName.entrySet ()) {

			//String beanName = ent.getKey ();

			DeliveryHandler handler =
				handlerEntry.getValue ().get ();

			for (String deliveryTypeCode
					: handler.getDeliveryTypeCodes ()) {

				DeliveryTypeRec deliveryType =
					deliveryTypeHelper.findByCode (
						GlobalId.root,
						deliveryTypeCode);

				if (deliveryType == null) {

					throw new RuntimeException (
						stringFormat (
							"No such delivery type: %s",
							deliveryTypeCode));

				}

				handlersById.put (
					deliveryType.getId (),
					handler);

			}

		}

	}

	@Override
	protected
	void deinit () {
		buffer = null;
		handlersById = null;
	}

	@Override
	protected
	String getThreadName () {
		throw new UnsupportedOperationException ();
	}

	@Override
	protected
	void createThreads () {

		Thread thread = threadManager.makeThread (new QueryThread ());
		thread.setName ("DelivQ");
		thread.start ();
		registerThread (thread);

		for (int i = 0; i < numWorkerThreads; i++) {
			thread = threadManager.makeThread(new WorkerThread ());
			thread.setName ("Deliv" + i);
			thread.start ();
			registerThread (thread);
		}
	}

	class QueryThread
		implements Runnable {

		@Override
		public
		void run () {

			try {
				while (true) {

					Set<Integer> activeIds =
						buffer.getKeys ();

					int numFound =
						pollDatabase (
							activeIds);

					if (numFound < buffer.getFullSize ()) {

						Thread.sleep (
							1000);

					} else {

						buffer.waitNotFull ();

					}

				}

			} catch (InterruptedException exception) {

				return;

			}

		}

		int pollDatabase (
				Set<Integer> activeIds) {

			int numFound = 0;

			@Cleanup
			Transaction transaction =
				database.beginReadOnly ();

			List<DeliveryRec> deliveries =
				deliveryHelper.findAllLimit (
					buffer.getFullSize ());

			for (DeliveryRec delivery
					: deliveries) {

				numFound ++;

				// if this one is already being worked on, skip it

				if (activeIds.contains (
						delivery.getId ()))
					continue;

				// make sure the delivery notice type is not a proxy

				delivery
					.getMessage ()
					.getDeliveryType ()
					.getId ();

				// and add this to the buffer

				buffer.add (
					delivery.getId (),
					delivery);

			}

			return numFound;

		}

	}

	class WorkerThread
		implements Runnable {

		@Override
		public
		void run () {

			while (true) {

				DeliveryRec delivery;
				try {
					delivery = buffer.next ();
				} catch (InterruptedException e) {
					return;
				}

				DeliveryTypeRec deliveryType =
					delivery.getMessage ().getDeliveryType ();

				DeliveryHandler handler =
					handlersById.get (
						deliveryType.getId ());

				try {

					if (handler == null) {

						throw new RuntimeException (
							stringFormat (
								"No delivery notice handler for %s",
								deliveryType.getCode ()));

					}

					handler.handle (
						delivery.getId (),
						delivery.getMessage ().getRef ());

				} catch (Exception exception) {

					exceptionLogic.logThrowable (
						"daemon",
						"Delivery notice daemon",
						exception,
						null,
						false);

				}

				buffer.remove (delivery.getId ());

			}

		}

	}

}