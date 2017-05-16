package wbs.sms.gazetteer.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.basic.ChainedFormFieldNativeMapping;
import wbs.console.forms.basic.NullFormFieldConstraintValidator;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.object.DereferenceFormFieldAccessor;
import wbs.console.forms.object.DynamicFormFieldAccessor;
import wbs.console.forms.text.TextFormFieldRenderer;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.sms.gazetteer.model.GazetteerEntryRec;

import wbs.utils.etc.PropertyUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("gazetteerFormFieldBuilder")
public
class GazetteerFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChainedFormFieldNativeMapping>
	chainedFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <DereferenceFormFieldAccessor> dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <DynamicFormFieldAccessor> dynamicFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <GazetteerCodeFormFieldNativeMapping>
	gazetteerCodeFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <GazetteerFormFieldInterfaceMapping>
	gazetteerFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField> readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <TextFormFieldRenderer> textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	GazetteerFormFieldSpec spec;

	@BuilderTarget
	FormFieldSetImplementation target;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

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
					PropertyUtils.propertyClassForClass (
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

			ConsoleFormNativeMapping nativeMapping;

			Optional gazetteerNativeMappingOptional =
				formFieldPluginManager.getNativeMapping (
					context,
					context.containerClass (),
					name,
					GazetteerEntryRec.class,
					propertyClass);

			if (
				optionalIsPresent (
					gazetteerNativeMappingOptional)
			) {

				nativeMapping =
					(ConsoleFormNativeMapping)
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
					optionalIsNotPresent (
						stringNativeMappingOptional)
				) {

					throw new RuntimeException ();

				}

				nativeMapping =
					chainedFormFieldNativeMappingProvider.get ()

					.previousMapping (
						gazetteerCodeFormFieldNativeMappingProvider.get ())

					.nextMapping (
						(ConsoleFormNativeMapping)
						stringNativeMappingOptional.get ());

			}

			// value validators

			List <FormFieldValueValidator> valueValidators =
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

				target.addFormItem (
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

				target.addFormItem (
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

}
