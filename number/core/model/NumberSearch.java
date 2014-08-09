package wbs.sms.number.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString
public
class NumberSearch {
	String number;
}
