package wbs.sms.message.report.model;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class MessageReportCodeRec
	implements CommonRecord<MessageReportCodeRec> {

	@GeneratedIdField
	Integer id;

	@SimpleField
	Integer status;

	@SimpleField
	Integer statusType;

	@SimpleField
	Integer reason;

	@SimpleField
	Boolean success;

	@SimpleField
	Boolean permanent;

	@SimpleField
	String description;

	@SimpleField
	MessageReportCodeType type;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageReportCodeRec> otherRecord) {

		MessageReportCodeRec other =
			(MessageReportCodeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface MessageReportCodeDaoMethods {

		MessageReportCodeRec find (
				MessageReportCodeType type,
				Integer status,
				Integer statusType,
				Integer reason);

	}

	// object helper methods

	public static
	interface MessageReportCodeObjectHelperMethods {

		MessageReportCodeRec findOrCreate (
				Integer status,
				Integer statusType,
				Integer reason,
				MessageReportCodeType type,
				boolean success,
				boolean permanent,
				String description);

	}

	// object helper implementation

	public static
	class MessageReportCodeObjectHelperImplementation
		implements MessageReportCodeObjectHelperMethods {

		@Inject
		Provider<MessageReportCodeObjectHelper> messageReportCodeHelperProvider;

		@Override
		public
		MessageReportCodeRec findOrCreate (
				Integer status,
				Integer statusType,
				Integer reason,
				MessageReportCodeType type,
				boolean success,
				boolean permanent,
				String description) {

			MessageReportCodeObjectHelper messageReportCodeHelper =
				messageReportCodeHelperProvider.get ();

			// TODO move this

			if (description != null && description.length () == 0)
				description = null;

			MessageReportCodeRec reportCode =
				messageReportCodeHelper.find (
					type,
					status,
					statusType,
					reason);

			if (reportCode != null) {

				// update description

				if (description != null) {

					reportCode
						.setDescription (description);

				}

				return reportCode;

			}

			return messageReportCodeHelper.insert (
				new MessageReportCodeRec ()
					.setPermanent (permanent)
					.setReason (reason)
					.setStatus (status)
					.setStatusType (statusType)
					.setSuccess (success)
					.setDescription (description)
					.setType (type));

		}

	}

}
