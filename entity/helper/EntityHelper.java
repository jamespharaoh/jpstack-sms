package wbs.framework.entity.helper;

import java.util.List;
import java.util.Map;

import wbs.framework.entity.model.Model;

public
interface EntityHelper {

	List <String> recordClassNames ();
	List <Class <?>> recordClasses ();
	List <Model <?>> recordModels ();
	Map <Class <?>, Model <?>> recordModelsByClass ();
	Map <String, Model <?>> recordModelsByName ();

	List <String> compositeClassNames ();
	List <Class <?>> compositeClasses ();
	List <Model <?>> compositeModels ();
	Map <Class <?>, Model <?>> compositeModelsByClass ();
	Map <String, Model <?>> compositeModelsByName ();

	List <String> allModelClassNames ();
	List <Class <?>> allModelClasses ();
	List <Model <?>> allModels ();
	Map <Class <?>, Model <?>> allModelsByClass ();
	Map <String, Model <?>> allModelsByName ();

}
