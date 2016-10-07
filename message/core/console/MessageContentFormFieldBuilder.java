package wbs.sms.message.core.console;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.FormFieldAccessor;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.console.forms.FormFieldNativeMapping;
import wbs.console.forms.FormFieldPluginManagerImplementation;
import wbs.console.forms.FormFieldRenderer;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.HtmlFormFieldRenderer;
import wbs.console.forms.IdentityFormFieldAccessor;
import wbs.console.forms.IdentityFormFieldNativeMapping;
import wbs.console.forms.ReadOnlyFormField;
import wbs.console.forms.RequiredFormFieldValueValidator;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("messageContentFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class MessageContentFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <HtmlFormFieldRenderer>
	htmlFormFieldRendererProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldAccessor>
	identityFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <MessageContentCsvFormFieldInterfaceMapping>
	messageContentCsvFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <MessageContentHtmlFormFieldInterfaceMapping>
	messageContentHtmlFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	MessageContentFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			"details";

		String label =
			"Details";

		// accessor

		FormFieldAccessor accessor =
			identityFormFieldAccessorProvider.get ()

			.containerClass (
				context.containerClass ());

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			messageContentHtmlFormFieldInterfaceMappingProvider.get ();

		// csv mapping

		FormFieldInterfaceMapping csvMapping =
			messageContentCsvFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			htmlFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label);

		// form field

		formFieldSet.addFormItem (
			readOnlyFormFieldProvider.get ()

			.name (
				name)

			.label (
				label)

			.accessor (
				accessor)

			.nativeMapping (
				nativeMapping)

			.interfaceMapping (
				interfaceMapping)

			.csvMapping (
				csvMapping)

			.renderer (
				renderer)

		);

	}

}
