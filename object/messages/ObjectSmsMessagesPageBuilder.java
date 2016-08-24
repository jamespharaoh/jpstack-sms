package wbs.sms.object.messages;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
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
import wbs.framework.entity.record.Record;
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
class ObjectSmsMessagesPageBuilder<
	ObjectType extends Record<ObjectType>
> {

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
	ConsoleContextBuilderContainer<ObjectType> container;

	@BuilderSource
	ObjectSmsMessagesPageSpec objectSmsMessagesPageSpec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<ObjectType> consoleHelper;
	String privKey;
	String tabName;
	String fileName;
	String responderName;

	Provider<PagePart> partFactory;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		buildPartFactory ();
		buildResponder ();

		for (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())
		) {

			buildContextTab (
				resolvedExtensionPoint);

			buildContextFile (
				resolvedExtensionPoint);

		}

	}

	void buildContextTab (
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

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
						"ObjectSmsMessagesPartFactory.get ()",
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

				if (object instanceof AffiliateRec) {

					affiliates =
						Collections.singletonList (
							(AffiliateRec)
							object);

				} else if (object instanceof ServiceRec) {

					services =
						Collections.singletonList (
							(ServiceRec)
							object);

				} else if (object instanceof BatchRec) {

					batches =
						Collections.singletonList (
							(BatchRec)
							object);

				} else if (object instanceof RouteRec) {

					routes =
						Collections.singletonList (
							(RouteRec)
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

					search.affiliateIdIn (
						affiliates.stream ()

						.map (
							AffiliateRec::getId)

						.collect (
							Collectors.toList ())

					);

				}

				if (! services.isEmpty ()) {

					search.serviceIdIn (
						services.stream ()

						.map (
							ServiceRec::getId)

						.collect (
							Collectors.toList ())

					);
						
				}

				if (! batches.isEmpty ()) {

					search.batchIdIn (
						batches.stream ()

						.map (
							BatchRec::getId)

						.collect (
							Collectors.toList ())

					);

				}

				if (! routes.isEmpty ()) {

					search.routeIdIn (
						routes.stream ()

						.map (
							RouteRec::getId)

						.collect (
							Collectors.toList ())

					);

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
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

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
