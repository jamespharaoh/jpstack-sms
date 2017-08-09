package wbs.sms.number.list.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class NumberListNumberSearch
	implements Serializable {

	Long numberListId;

	Boolean present;

}
