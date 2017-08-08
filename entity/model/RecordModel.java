package wbs.framework.entity.model;

import wbs.framework.entity.record.Record;

public
interface RecordModel <RecordType extends Record <RecordType>>
	extends
		Model <RecordType>,
		RecordModelMethods <RecordType> {

}
