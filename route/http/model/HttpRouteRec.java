package wbs.sms.route.http.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity (
	table = "httproute")
public
class HttpRouteRec
	implements MajorRecord<HttpRouteRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField (
		column = "routeid")
	RouteRec route;

	@IdentityReferenceField (
		column = "networkid")
	NetworkRec network;

	// settings

	@SimpleField
	Boolean post;

	@SimpleField
	String url;

	@SimpleField
	String params;

	@SimpleField (
		column = "successregex")
	String successRegex;

	@SimpleField (
		column = "tempfailureregex")
	String tempFailureRegex;

	@SimpleField (
		column = "permfailureregex")
	String permFailureRegex;

	@SimpleField (
		column = "dailyfailureregex")
	String dailyFailureRegex;

	@SimpleField (
		column = "creditfailureregex")
	String creditFailureRegex;

	@SimpleField
	String paramEncoding;

	// compare to

	@Override
	public
	int compareTo (
			Record<HttpRouteRec> otherRecord) {

		HttpRouteRec other =
			(HttpRouteRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getRoute (),
				other.getRoute ())

			.append (
				getNetwork (),
				other.getNetwork ())

			.toComparison ();

	}

}
