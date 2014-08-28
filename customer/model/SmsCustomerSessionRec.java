package wbs.sms.customer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SmsCustomerSessionRec
	implements CommonRecord<SmsCustomerSessionRec> {

	// id
	
	@GeneratedIdField
	Integer id;

	// identity
	
	@ParentField
	SmsCustomerRec customer;

	@IndexField
	Integer index;

	// details
	
	@SimpleField
	Instant startTime;
	
	@SimpleField (
		nullable = true)
	Instant endTime;

	// state

	@ReferenceField (
		nullable = true)
	MessageRec welcomeMessage;

	@ReferenceField (
		nullable = true)
	MessageRec warningMessage;

	// compare to

	public 
	int compareTo (
			Record<SmsCustomerSessionRec> otherRecord) {

		SmsCustomerSessionRec other =
			(SmsCustomerSessionRec) otherRecord;

		return new CompareToBuilder ()
		
			.append (
				getCustomer (),
				other.getCustomer ())
			
			.append (
				getIndex (),
				other.getIndex ())
				
			.toComparison ();

	}

}
