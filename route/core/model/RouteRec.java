package wbs.sms.route.core.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.currency.model.CurrencyRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.number.lookup.model.NumberLookupRec;
import wbs.sms.route.sender.model.SenderRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class RouteRec
	implements MajorRecord<RouteRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	@SimpleField (
		nullable = true)
	String number;

	// settings

	@ReferenceField (
		nullable = true)
	CommandRec command;

	@SimpleField
	Boolean canReceive = false;

	@SimpleField
	Boolean canSend = false;

	@SimpleField
	Boolean deliveryReports = false;

	@SimpleField (
		nullable = true)
	Integer expirySecs;

	@SimpleField
	Integer inCharge = 0;

	@SimpleField
	Integer outCharge = 0;

	@ReferenceField (nullable = true)
	SenderRec sender;

	@SimpleField (nullable = true)
	Integer maxTries;

	@SimpleField
	Boolean avRequired = false;

	@SimpleField
	Boolean inboundImpliesAdult = false;

	@SimpleField
	RouteNetworkBehaviour networkBehaviour =
		RouteNetworkBehaviour.alwaysUpdate;

	@ReferenceField (
		nullable = true)
	CurrencyRec currency;

	@ReferenceField (
		nullable = true)
	NumberLookupRec blockNumberLookup;

	// children

	@LinkField (
		table = "route_message_type",
		where = "direction = 0")
	Set<MessageTypeRec> inboundMessageTypes =
		new HashSet<MessageTypeRec> ();

	@LinkField (
		table = "route_message_type",
		where = "direction = 1")
	Set<MessageTypeRec> outboundMessageTypes =
		new HashSet<MessageTypeRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<RouteRec> otherRecord) {

		RouteRec other =
			(RouteRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
