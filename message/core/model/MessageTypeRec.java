package wbs.sms.message.core.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class MessageTypeRec
	implements MajorRecord<MessageTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// details

	@SimpleField
	String description;

	// links

	@LinkField (
		table = "route_message_type",
		where = "direction = 0")
	Set<RouteRec> inboundRoutes =
		new HashSet<RouteRec> ();

	@LinkField (
		table = "route_message_type",
		where = "direction = 1")
	Set<RouteRec> outboundRoutes =
		new HashSet<RouteRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageTypeRec> otherRecord) {

		MessageTypeRec other =
			(MessageTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
