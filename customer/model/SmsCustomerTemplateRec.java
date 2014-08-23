package wbs.sms.customer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class SmsCustomerTemplateRec
	implements MajorRecord<SmsCustomerTemplateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SmsCustomerManagerRec smsCustomerManager;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	// state

	@ReferenceField (
		nullable = true)
	RouterRec router;

	@SimpleField
	String number;

	@ReferenceField
	TextRec text;

	// compare to

	public
	int compareTo (
			Record<SmsCustomerTemplateRec> otherRecord) {

		SmsCustomerTemplateRec other =
			(SmsCustomerTemplateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSmsCustomerManager (),
				other.getSmsCustomerManager ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
