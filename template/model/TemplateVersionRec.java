package wbs.sms.template.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class TemplateVersionRec
	implements CommonRecord<TemplateVersionRec> {

	@GeneratedIdField
	Integer id;

	// TODO add index etc
	// TODO or move to events?

	@ParentField
	TemplateRec template;

	@SimpleField
	Date timestamp;

	@ReferenceField
	UserRec user;

	@SimpleField
	Boolean billedEnabled = false;

	@ReferenceField
	RouteRec billedRoute = null;

	@SimpleField
	String billedNumber = "";

	@SimpleField
	String billedMessage = "";

	// children

	@CollectionField (
		index = "i")
	List<TemplatePartRec> templateParts =
		new ArrayList<TemplatePartRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<TemplateVersionRec> otherRecord) {

		TemplateVersionRec other =
			(TemplateVersionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getTemplate (),
				other.getTemplate ())

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.toComparison ();

	}

}
