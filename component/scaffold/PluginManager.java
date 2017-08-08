package wbs.framework.component.scaffold;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
public
class PluginManager {

	// state

	@Getter @Setter
	List <PluginSpec> plugins;

	@Getter @Setter
	Map <String, PluginRecordModelSpec> pluginRecordModelsByName;

	@Getter @Setter
	Map <String, PluginCompositeModelSpec> pluginCompositeModelsByName;

	@Getter @Setter
	Map <String, PluginEnumTypeSpec> pluginEnumTypesByName;

	@Getter @Setter
	Map <String, PluginCustomTypeSpec> pluginCustomTypesByName;

	// implementation

	public
	Class <? extends Record <?>> recordModelClass (
			@NonNull String modelName) {

		PluginRecordModelSpec modelSpec =
			mapItemForKeyRequired (
				pluginRecordModelsByName,
				hyphenToCamel (
					modelName));

		String modelClassName =
			stringFormat (
				"%s.model.%sRec",
				modelSpec.plugin ().packageName (),
				capitalise (
					modelSpec.name ()));

		return genericCastUnchecked (
			classForNameRequired (
				modelClassName));

	}

	public
	Class <? extends Record <?>> compositeModelClass (
			@NonNull String modelName) {

		PluginCompositeModelSpec modelSpec =
			mapItemForKeyRequired (
				pluginCompositeModelsByName,
				hyphenToCamel (
					modelName));

		String modelClassName =
			stringFormat (
				"%s.model.%sRec",
				modelSpec.plugin ().packageName (),
				capitalise (
					modelSpec.name ()));

		return genericCastUnchecked (
			classForNameRequired (
				modelClassName));

	}

}
