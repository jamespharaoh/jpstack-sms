package wbs.sms.route.test.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.service.model.ServiceTypeObjectHelper;

@PrototypeComponent ("routeTestFixtureProvider")
public 
class RouteTestFixtureProvider 
	implements FixtureProvider {
	
	// dependencies
	
	@Inject
	ServiceObjectHelper serviceHelper;	
	
	@Inject
	ServiceTypeObjectHelper serviceTypeHelper;	
	
	@Inject
	ObjectTypeObjectHelper objectTypeHelper;	
	
	// implementation
	
	@Override
	public
	void createFixtures () {
	
		/*ServiceRec testService =
			serviceHelper.insert (
				new ServiceRec ()
				
				.setCode("test")
				
				.setDescription("Test service description")	
				
				.setParentObjectId(0)
				
				.setParentObjectType(objectTypeHelper.find(0))
				
				.setType(serviceTypeHelper.find(0))

		);*/
	
	}

}
