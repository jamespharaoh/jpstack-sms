package wbs.sms.message.delivery.daemon;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.daemon.QueueBuffer;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;
import wbs.sms.message.delivery.model.DeliveryTypeRec;

@SingletonComponent ("deliveryDaemon")
public
class DeliveryDaemon
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	@SingletonDependency
	DeliveryTypeObjectHelper deliveryTypeHelper;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	Map <String, Provider <DeliveryHandler>> handlersByBeanName;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	int bufferSize = 128;

	@Getter @Setter
	int numWorkerThreads = 4;

	// state

	QueueBuffer<Long,DeliveryRec> buffer;
	Map<Long,DeliveryHandler> handlersById;

	// implementation

	@Override
	protected
	void init () {

		buffer =
			new QueueBuffer<> (
				bufferSize);

		handlersById =
			new HashMap<> ();

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"DeliveryDaemon.init ()",
				this);

		for (
			Map.Entry<String,Provider<DeliveryHandler>> handlerEntry
				: handlersByBeanName.entrySet ()
		) {

			//String beanName = ent.getKey ();

			DeliveryHandler handler =
				handlerEntry.getValue ().get ();

			for (
				String deliveryTypeCode
					: handler.getDeliveryTypeCodes ()
			) {

				DeliveryTypeRec deliveryType =
					deliveryTypeHelper.findByCodeRequired (
						GlobalId.root,
						deliveryTypeCode);

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

					Set<Long> activeIds =
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
				Set<Long> activeIds) {

			int numFound = 0;

			@Cleanup
			Transaction transaction =
				database.beginReadOnly (
					"DeliveryDaemon.QueryThread.pollDatabase (activeIds)",
					this);

			List <DeliveryRec> deliveries =
				deliveryHelper.findAllLimit (
					buffer.getFullSize ());

			for (
				DeliveryRec delivery
					: deliveries
			) {

				numFound ++;

				// if this one is already being worked on, skip it

				if (activeIds.contains (
						delivery.getId ()))
					continue;

				// make sure the delivery notice type is not a proxy

				transaction.fetch (
					delivery,
					delivery.getMessage (),
					delivery.getMessage ().getDeliveryType ());

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

				TaskLogger taskLogger =
					logContext.createTaskLogger (
						"worker.run");

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
						taskLogger,
						delivery.getId (),
						delivery.getMessage ().getRef ());

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						"daemon",
						"Delivery notice daemon",
						exception,
						Optional.absent (),
						GenericExceptionResolution.tryAgainLater);

				}

				buffer.remove (delivery.getId ());

			}

		}

	}

}
