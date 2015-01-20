package wbs.sms.message.inbox.daemon;

import lombok.Getter;

public
class ReceivedMessageImpl
	implements ReceivedMessage {

	private final
	ReceivedMessage parent;

	@Getter
	private final
	int messageId;

	@Getter
	private final
	String rest;

	@Getter
	private final
	int ref;

	Integer serviceId;
	Integer affiliateId;

	public
	ReceivedMessageImpl (
			ReceivedMessage parent,
			int messageId,
			String rest,
			int ref) {

		this.parent = parent;

		this.messageId = messageId;
		this.rest = rest;
		this.ref = ref;

	}

	public
	ReceivedMessageImpl (
			ReceivedMessage parent,
			String rest,
			int ref) {

		this (
			parent,
			parent.getMessageId (),
			rest,
			ref);
	}

	public
	ReceivedMessageImpl (
			ReceivedMessage parent,
			String rest) {

		this (
			parent,
			parent.getMessageId (),
			rest,
			0);
	}

	@Override
	public
	Integer getAffiliateId () {

		if (parent != null)
			return parent.getAffiliateId ();
		else
			return affiliateId;

	}

	@Override
	public
	Integer getServiceId () {

		if (parent != null)
			return parent.getServiceId ();
		else
			return serviceId;

	}

}