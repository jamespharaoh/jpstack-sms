package wbs.sms.gazetteer.console;

import static wbs.framework.utils.etc.NullUtils.ifNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.ChainedFormFieldNativeMapping;
import wbs.console.forms.DereferenceFormFieldAccessor;
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

import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import wbs.sms.gazetteer.model.GazetteerEntryRec;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("gazetteerFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class GazetteerFormFieldBuilder {

	// dependencies

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<ChainedFormFieldNativeMapping>
	chainedFormFieldNativeMappingProvider;

	@Inject
	Provider<DereferenceFormFieldAccessor> dereferenceFormFieldAccessorProvider;

	@Inject
	Provider<DynamicFormFieldAccessor> dynamicFormFieldAccessorProvider;

	@Inject
	Provider<GazetteerCodeFormFieldNativeMapping>
	gazetteerCodeFormFieldNativeMappingProvider;

	@Inject
	Provider<GazetteerFormFieldInterfaceMapping>
	gazetteerFormFieldInterfaceMappingProvider;

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
	GazetteerFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String nativeFieldName =
			ifNull (
				spec.fieldName (),
				name);

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

		Boolean dynamic =
			ifNull (
				spec.dynamic (),
				false);

		// property class

		Class<?> propertyClass;

		if (! dynamic) {

			propertyClass =
				BeanLogic.propertyClassForClass (
					context.containerClass (),
					nativeFieldName);

		} else {

			propertyClass =
				GazetteerEntryRec.class;

		}


		// accessor

		FormFieldAccessor accessor;

		if (dynamic) {

			accessor =
				dynamicFormFieldAccessorProvider.get ()

				.name (
					nativeFieldName)

				.nativeClass (
					propertyClass);

		} else if (readOnly) {

			accessor =
				dereferenceFormFieldAccessorProvider.get ()

				.path (
					nativeFieldName);

		} else {

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					nativeFieldName)

				.nativeClass (
					propertyClass);

		}

		// native mapping

		FormFieldNativeMapping nativeMapping;

		Optional gazetteerNativeMappingOptional =
			formFieldPluginManager.getNativeMapping (
				context,
				context.containerClass (),
				name,
				GazetteerEntryRec.class,
				propertyClass);

		if (
			isPresent (
				gazetteerNativeMappingOptional)
		) {

			nativeMapping =
				(FormFieldNativeMapping)
				gazetteerNativeMappingOptional.get ();

		} else {

			Optional stringNativeMappingOptional =
				formFieldPluginManager.getNativeMapping (
					context,
					context.containerClass (),
					name,
					String.class,
					propertyClass);

			if (
				isNotPresent (
					stringNativeMappingOptional)
			) {

				throw new RuntimeException ();

			}

			nativeMapping =
				chainedFormFieldNativeMappingProvider.get ()

				.previousMapping (
					gazetteerCodeFormFieldNativeMappingProvider.get ())

				.nextMapping (
					(FormFieldNativeMapping)
					stringNativeMappingOptional.get ());

		}

		// value validators

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		if (! nullable) {

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.get ());

		}

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			gazetteerFormFieldInterfaceMappingProvider.get ()

			.gazetteerFieldName (
				spec.gazetteerFieldName ());

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
