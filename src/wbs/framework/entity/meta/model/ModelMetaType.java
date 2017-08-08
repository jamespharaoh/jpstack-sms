package wbs.framework.entity.meta.model;

public
enum ModelMetaType {

	common,
	composite,
	ephemeral,
	event,
	major,
	minor,
	root,
	type;

	public
	boolean composite () {
		return this == composite;
	}

	public
	boolean record () {
		return this != composite;
	}

}
