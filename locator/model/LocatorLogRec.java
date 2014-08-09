package wbs.sms.locator.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class LocatorLogRec
	implements CommonRecord<LocatorLogRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ReferenceField
	LocatorRec locator;

	@ReferenceField
	NumberRec number;

	// TODO index?

	// details

	@SimpleField
	Date startTime;

	@SimpleField
	Date endTime;

	@SimpleField
	Boolean success;

	@SimpleField (
		columns = { "longitude", "latitude" })
	LongLat longLat;

	@ReferenceField
	ServiceRec service;

	@ReferenceField
	AffiliateRec affiliate;

	@SimpleField
	String errorCode;

	@ReferenceField
	TextRec errorText;

	// compare to

	@Override
	public
	int compareTo (
			Record<LocatorLogRec> otherRecord) {

		LocatorLogRec other =
			(LocatorLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getStartTime (),
				getStartTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}