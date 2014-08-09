package wbs.sms.gazetteer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.locator.model.LongLat;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class GazetteerEntryRec
	implements CommonRecord<GazetteerEntryRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	GazetteerRec gazetteer;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	// settings

	@SimpleField (
		columns = { "longitude", "latitude" })
	LongLat longLat =
		new LongLat ();

	@SimpleField
	Boolean canonical;

	// compare to

	@Override
	public
	int compareTo (
			Record<GazetteerEntryRec> otherRecord) {

		GazetteerEntryRec other =
			(GazetteerEntryRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getGazetteer (),
				other.getGazetteer ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
