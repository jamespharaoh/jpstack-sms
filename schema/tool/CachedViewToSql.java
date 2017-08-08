package wbs.framework.schema.tool;

import java.util.List;

import wbs.framework.entity.model.Model;
import wbs.framework.logging.TaskLogger;

public
interface CachedViewToSql {

	void forModel (
			TaskLogger parentTaskLogger,
			List <String> sqlStatements,
			Model <?> model);

}
