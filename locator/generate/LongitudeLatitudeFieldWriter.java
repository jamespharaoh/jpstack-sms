package wbs.sms.locator.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.AnnotationWriter;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.utils.etc.FormatWriter;
import wbs.sms.locator.metamodel.LongitudeLatitudeFieldSpec;

@PrototypeComponent ("longitudeLatitudeFieldWriter")
@ModelWriter
public
class LongitudeLatitudeFieldWriter {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	LongitudeLatitudeFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		// write field annotation

		AnnotationWriter annotationWriter =
			new AnnotationWriter ()

			.name (
				"SimpleField");

		if (ifNull (spec.nullable (), false)) {

			annotationWriter.addAttributeFormat (
				"nullable",
				"true");

		}

		String columnNamesPattern =
			ifNull (
				spec.columnNames (),
				"%");

		String longitudeColumnName =
			columnNamesPattern.replace ("%", "longitude");

		String latitudeColumnName =
			columnNamesPattern.replace ("%", "latitude");

		annotationWriter.addAttributeFormat (
			"columns",
			"{ \"%s\", \"%s\" }",
			longitudeColumnName.replace ("\"", "\\\""),
			latitudeColumnName.replace ("\"", "\\\""));

		annotationWriter.write (
			javaWriter,
			"\t");

		// write field

		new PropertyWriter ()

			.thisClassNameFormat (
				"%sRec",
				capitalise (
					parent.name ()))

			.typeNameFormat (
				"wbs.sms.locator.model.LongLat")

			.propertyNameFormat (
				"%s",
				spec.name ())

			.write (
				javaWriter,
				"\t");

	}

}
