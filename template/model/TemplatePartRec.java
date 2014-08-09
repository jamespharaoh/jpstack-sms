package wbs.sms.template.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class TemplatePartRec
	implements MinorRecord<TemplatePartRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	TemplateVersionRec templateVersion;

	@IndexField
	Integer index;

	// settings

	@ReferenceField
	RouterRec router;

	@SimpleField
	String number = "";

	@SimpleField
	String message = "";

	// compare to

	@Override
	public
	int compareTo (
			Record<TemplatePartRec> otherRecord) {

		TemplatePartRec other =
			(TemplatePartRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getTemplateVersion (),
				other.getTemplateVersion ())

			.append (
				getIndex (),
				other.getIndex ())

			.toComparison ();

	}

}
