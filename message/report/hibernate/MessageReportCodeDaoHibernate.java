package wbs.sms.message.report.hibernate;

import static wbs.framework.utils.etc.Misc.toIntegerNullSafe;

import org.hibernate.type.IntegerType;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.report.model.MessageReportCodeDao;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;

public
class MessageReportCodeDaoHibernate
	extends HibernateDao
	implements MessageReportCodeDao {

	@Override
	public
	MessageReportCodeRec find (
			MessageReportCodeType type,
			Long status,
			Long statusType,
			Long reason) {

		return findOne (
			MessageReportCodeRec.class,

			createQuery (
				"FROM MessageReportCodeRec mr " +
				"WHERE type = :type " +
					"AND ((status IS NULL " +
							"AND :status IS NULL) " +
						"OR status = :status) " +
					"AND ((status_type IS NULL " +
							"AND :status_type IS NULL) " +
						"OR status_type = :status_type) " +
					"AND ((reason IS NULL " +
							"AND :reason IS NULL) " +
						"OR reason = :reason)")

			.setParameter (
				"type",
				type,
				MessageReportCodeTypeType.INSTANCE)

			.setParameter (
				"status",
				toIntegerNullSafe (
					status),
				IntegerType.INSTANCE)

			.setParameter (
				"status_type",
				toIntegerNullSafe (
					statusType),
				IntegerType.INSTANCE)

			.setParameter (
				"reason",
				toIntegerNullSafe (
					reason),
				IntegerType.INSTANCE)

			.list ());

	}

}
