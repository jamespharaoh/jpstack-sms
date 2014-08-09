package wbs.sms.route.core.hibernate;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.framework.hibernate.EnumUserType;
import wbs.sms.route.core.model.RouteNetworkBehaviour;

public
class RouteNetworkBehaviourType
	extends EnumUserType<String,RouteNetworkBehaviour> {

	{

		sqlType (1111);
		enumClass (RouteNetworkBehaviour.class);

		auto (String.class);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new RouteNetworkBehaviourType ());

}
