package wbs.sms.number.list.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class NumberListUpdateRec
	implements CommonRecord<NumberListUpdateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	NumberListRec numberList;

	// TODO index?

	// details

	@SimpleField
	Instant timestamp;

	@ReferenceField (
		nullable = true)
	UserRec user;

	@ReferenceField (
		nullable = true)
	ServiceRec service;

	@ReferenceField (
		nullable = true)
	MessageRec message;

	@SimpleField
	Boolean present;

	@SimpleField
	Integer numberCount;

	// children

	@LinkField (
		table = "number_list_update_number")
	Set<NumberRec> numbers =
		new HashSet<NumberRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<NumberListUpdateRec> otherRecord) {

		NumberListUpdateRec other =
			(NumberListUpdateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumberList (),
				other.getNumberList ())

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.toComparison ();

	}

}
