package wbs.sms.route.sender.fixture;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ModelMetaBuilderHandler;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.sms.route.sender.metamodel.SendersSpec;

@PrototypeComponent ("sendersBuilder")
@ModelMetaBuilderHandler
public
class SendersBuilder {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	SendersSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		builder.descend (
			parent,
			spec.senders (),
			model,
			MissingBuilderBehaviour.error);

	}

}
