package wbs.sms.gsm.console;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.DynamicFormFieldAccessor;
import wbs.console.forms.FormFieldAccessor;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.forms.FormFieldConstraintValidator;
import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.console.forms.FormFieldNativeMapping;
import wbs.console.forms.FormFieldPluginManagerImplementation;
import wbs.console.forms.FormFieldRenderer;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormFieldUpdateHook;
import wbs.console.forms.FormFieldValueValidator;
import wbs.console.forms.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.NullFormFieldConstraintValidator;
import wbs.console.forms.ReadOnlyFormField;
import wbs.console.forms.RequiredFormFieldValueValidator;
import wbs.console.forms.SimpleFormFieldAccessor;
import wbs.console.forms.TextFormFieldRenderer;
import wbs.console.forms.UpdatableFormField;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("gsmFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class GsmFormFieldBuilder {

	// dependencies

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<DynamicFormFieldAccessor> dynamicFormFieldAccessorProvider;

	@Inject
	Provider<GsmFormFieldValueValidator> gsmValueValidatorProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<ReadOnlyFormField> readOnlyFormFieldProvider;

	@Inject
	Provider<RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@Inject
	Provider<SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@Inject
	Provider<TextFormFieldRenderer> textFormFieldRendererProvider;

	@Inject
	Provider<UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	GsmFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String label =
			ifNull (
				spec.label (),
				capitalise (
					camelToSpaces (
						name)));

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

		Boolean nullable =
			ifNull (
				spec.nullable (),
				false);

		Integer minimumLength =
			spec.minimumLength ();

		Integer maximumLength =
			spec.maximumLength ();

		Boolean dynamic =
			ifNull (
				spec.dynamic (),
				false);

		// field type

		Class<?> propertyClass;

		if (! dynamic) {

			propertyClass =
				BeanLogic.propertyClassForClass (
					context.containerClass (),
					name);

		} else {

			propertyClass = String.class;

		}

		// accessor

		FormFieldAccessor accessor;

		if (dynamic) {

			accessor =
				dynamicFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					propertyClass);

		} else {

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					propertyClass);

		}

		// TODO dynamic

		// native mapping

		FormFieldNativeMapping nativeMapping =
			formFieldPluginManager.getNativeMappingRequired (
				context,
				context.containerClass (),
				name,
				String.class,
				propertyClass);

		// value validator

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		if (! nullable) {

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.get ());

		}

		valueValidators.add (
			gsmValueValidatorProvider.get ()

			.minimumLength (
				minimumLength)

			.maximumLength (
				maximumLength)

		);

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// form field

		if (readOnly) {

			target.addFormField (
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
					interfaceMapping)

				.renderer (
					renderer)

			);

		} else {

			target.addFormField (
				updatableFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.valueValidators (
					valueValidators)

				.constraintValidator (
					constraintValidator)

				.interfaceMapping (
					interfaceMapping)

				.csvMapping (
					interfaceMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

}
