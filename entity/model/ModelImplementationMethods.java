package wbs.framework.entity.model;

public
interface ModelImplementationMethods <
	ConcreteImplementation extends ModelImplementationMethods <?, DataType>,
	DataType
>
	extends Model <DataType> {

	ConcreteImplementation codeField (
			ModelField codeField);

	ConcreteImplementation deletedField (
			ModelField deletedField);

	ConcreteImplementation descriptionField (
			ModelField descriptionField);

	ConcreteImplementation idField (
			ModelField idField);

	ConcreteImplementation indexField (
			ModelField indexField);

	ConcreteImplementation masterField (
			ModelField masterField);

	ConcreteImplementation nameField (
			ModelField nameField);

	ConcreteImplementation parentField (
			ModelField parentField);

	ConcreteImplementation parentIdField (
			ModelField parentIdField);

	ConcreteImplementation parentTypeField (
			ModelField parentTypeField);

	ConcreteImplementation timestampField (
			ModelField timestampField);

	ConcreteImplementation typeCodeField (
			ModelField typeCodeField);

	ConcreteImplementation typeField (
			ModelField typeField);

}
