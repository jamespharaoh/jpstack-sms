package wbs.sms.locator.build;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.string.StringUtils.camelToSpaces;

import com.google.common.collect.ImmutableList;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.entity.build.ModelBuilder;
import wbs.framework.entity.build.ModelFieldBuilderContext;
import wbs.framework.entity.build.ModelFieldBuilderTarget;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.sms.locator.metamodel.LongitudeLatitudeFieldSpec;
import wbs.sms.locator.model.LongLat;

@PrototypeComponent ("longitudeLatitudeModelFieldBuilder")
@ModelBuilder
public
class LongitudeLatitudeModelFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	LongitudeLatitudeFieldSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String columnNamesPattern =
			ifNull (
				spec.columnNames (),
				"%");

		String longitudeColumnName =
			columnNamesPattern.replace ("%", "longitude");

		String latitudeColumnName =
			columnNamesPattern.replace ("%", "latitude");

		// create model field

		ModelField modelField =
			new ModelField ()

			.model (
				target.model ())

			.parentField (
				context.parentModelField ())

			.name (
				spec.name ())

			.label (
				camelToSpaces (
					spec.name ()))

			.type (
				ModelFieldType.simple)

			.parent (
				false)

			.identity (
				false)

			.valueType (
				LongLat.class)

			.nullable (
				ifNull (
					spec.nullable (),
					false))

			.columnNames (
				ImmutableList.of (
					longitudeColumnName,
					latitudeColumnName))

			.hibernateTypeHelper (
				classForNameRequired (
					"wbs.sms.locator.hibernate.LongLatType"));

		// store field

		target.fields ().add (
			modelField);

		target.fieldsByName ().put (
			modelField.name (),
			modelField);

	}

}
