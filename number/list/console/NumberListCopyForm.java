package wbs.sms.number.list.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.sms.number.list.model.NumberListRec;

@Accessors (fluent = true)
@Data
public
class NumberListCopyForm {

	NumberListRec numberList;

}
