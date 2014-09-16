package wbs.platform.console.forms;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.currency.model.CurrencyRec;

@Accessors (fluent = true)
@PrototypeComponent ("currencyFormFieldInterfaceMapping")
public
class CurrencyFormFieldInterfaceMapping<Container extends Record<?>>
	implements FormFieldInterfaceMapping<Container,Integer,String> {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	String currencyPath;

	// implementation

	@Override
	public
	Integer interfaceToGeneric (
			Container container,
			String interfaceValue,
			List<String> errors) {

		if (interfaceValue == null)
			return null;

		if (interfaceValue.isEmpty ())
			return null;

		CurrencyRec currency =
			(CurrencyRec)
			(Object)
			objectManager.dereference (
				container,
				currencyPath);

		if (currency != null) {

			return currencyLogic.parseText (
				currency,
				interfaceValue);

		} else {

			return Integer.parseInt (
				interfaceValue);

		}

	}

	@Override
	public
	String genericToInterface (
			Container container,
			Integer genericValue) {

		if (genericValue == null)
			return null;

		CurrencyRec currency =
			(CurrencyRec)
			(Object)
			objectManager.dereference (
				container,
				currencyPath);

		if (currency != null) {

			return currencyLogic.formatText (
				currency,
				genericValue);

		} else {

			return Integer.toString (
				genericValue);

		}


	}

}