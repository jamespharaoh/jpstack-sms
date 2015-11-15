package wbs.sms.object.messages;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.console.MessageSource;
import wbs.sms.message.core.console.MessageSourceImplementation;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("objectSmsMessagesPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSmsMessagesPageBuilder {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	@Inject
	Database database;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFileProvider;

	@Inject
	Provider<ConsoleContextTab> contextTabProvider;

	@Inject
	Provider<ObjectSmsMessagesPart> messageBrowserPartProvider;

	@Inject
	Provider<MessageSourceImplementation> messageSourceProvider;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectSmsMessagesPageSpec objectSmsMessagesPageSpec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;
	String privKey;
	String tabName;
	String fileName;
	String responderName;

	Provider<PagePart> partFactory;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildPartFactory ();
		buildResponder ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

			buildContextTab (
				resolvedExtensionPoint);

			buildContextFile (
				resolvedExtensionPoint);

		}

	}

	void buildContextTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			"end",
			contextTabProvider.get ()
				.name (tabName)
				.defaultLabel ("Messages")
				.localFile (fileName)
				.privKeys (privKey),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildPartFactory () {

		partFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				@Cleanup
				Transaction transaction =
					database.beginReadOnly (
						this);

				Record<?> object =
					consoleHelper.lookupObject (
						requestContext.contextStuff ());

				MessageSearch search =
					new MessageSearch ();

				List<AffiliateRec> affiliates =
					Collections.<AffiliateRec>emptyList ();

				List<ServiceRec> services =
					Collections.<ServiceRec>emptyList ();

				List<BatchRec> batches =
					Collections.<BatchRec>emptyList ();

				List<RouteRec> routes =
					Collections.<RouteRec>emptyList ();

				if ((Object) object instanceof AffiliateRec) {

					affiliates =
						Collections.singletonList (
							(AffiliateRec)
							(Object)
							object);

				} else if ((Object) object instanceof ServiceRec) {

					services =
						Collections.singletonList (
							(ServiceRec)
							(Object)
							object);

				} else if ((Object) object instanceof BatchRec) {

					batches =
						Collections.singletonList (
							(BatchRec)
							(Object)
							object);

				} else if ((Object) object instanceof RouteRec) {

					routes =
						Collections.singletonList (
							(RouteRec)
							(Object)
							object);

				} else {

					affiliates =
						objectManager.getChildren (
							object,
							AffiliateRec.class);

					services =
						objectManager.getChildren (
							object,
							ServiceRec.class);

					batches =
						objectManager.getChildren (
							object,
							BatchRec.class);

				}

				if (
					affiliates.isEmpty ()
					&& services.isEmpty ()
					&& batches.isEmpty ()
					&& routes.isEmpty ()
				) {

					throw new RuntimeException (
						stringFormat (
							"No affiliates, services, batches or routes for %s",
							object));

				}

				if (
					(
					  (affiliates.isEmpty () ? 0 : 1)
					+ (services.isEmpty () ? 0 : 1)
					+ (batches.isEmpty () ? 0 : 1)
					+ (routes.isEmpty () ? 0 : 1)
					) > 1
				) {

					throw new RuntimeException ();

				}

				if (! affiliates.isEmpty ()) {

					List<Integer> affiliateIds =
						new ArrayList<Integer> ();

					for (AffiliateRec affiliate : affiliates)
						affiliateIds.add (affiliate.getId ());

					search.affiliateIdIn (
						affiliateIds);

				}

				if (! services.isEmpty ()) {

					List<Integer> serviceIds =
						new ArrayList<Integer> ();

					for (ServiceRec service : services)
						serviceIds.add (service.getId ());

					search.serviceIdIn (
						serviceIds);

				}

				if (! batches.isEmpty ()) {

					List<Integer> batchIds =
						new ArrayList<Integer> ();

					for (BatchRec batch : batches)
						batchIds.add (batch.getId ());

					search.batchIdIn (
						batchIds);

				}

				if (! routes.isEmpty ()) {

					List<Integer> routeIds =
						new ArrayList<Integer> ();

					for (RouteRec route : routes)
						routeIds.add (route.getId ());

					search.routeIdIn (
						routeIds);

				}

				MessageSource source =
					messageSourceProvider.get ()
						.searchTemplate (search);

				return messageBrowserPartProvider.get ()
					.localName ("/" + fileName)
					.messageSource (source);

			}

		};

	}

	void buildContextFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (
			fileName,
			consoleFileProvider.get ()
				.getResponderName (responderName)
				.privKeys (
					Collections.singletonList (privKey)),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()
				.tab (tabName)
				.title (capitalise (
					consoleHelper.friendlyName () + " messages"))
				.pagePartFactory (partFactory));

	}

	// defaults

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		privKey =
			ifNull (
				objectSmsMessagesPageSpec.privKey (),
				stringFormat (
					"%s.messages",
					consoleHelper.objectName ()));

		tabName =
			ifNull (
				objectSmsMessagesPageSpec.tabName (),
				stringFormat (
					"%s.messages",
					container.pathPrefix ()));

		fileName =
			ifNull (
				objectSmsMessagesPageSpec.fileName (),
				stringFormat (
					"%s.messages",
					container.pathPrefix ()));

		responderName =
			ifNull (
				objectSmsMessagesPageSpec.responderName (),
				stringFormat (
					"%sMessagesResponder",
					container.newBeanNamePrefix ()));

	}

}
