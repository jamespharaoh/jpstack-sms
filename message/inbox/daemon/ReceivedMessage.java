package wbs.sms.message.inbox.daemon;

public
interface ReceivedMessage {

	/**
	 * Returns a copy of the Message object from the database.
	 *
	 * @return the Message object
	 */
	public
	int getMessageId ();

	/**
	 * Returns the ref associated with this message, for use by auto-allocated
	 * commands.
	 *
	 * @return the reference
	 */
	public
	int getRef ();

	/**
	 * Returns the unprocessed part of the message (ie without any keywords or
	 * stuff at the front).
	 *
	 * @return the message
	 */
	public
	String getRest ();

	/**
	 * Set the message's serviceid. This will be saved back to the message along
	 * with its status by ReceivedManager.
	 *
	 * @param serviceId
	 *            the serviceid to associate with the message
	 */
	public
	void setServiceId (
			int serviceId);

	/**
	 * Sets the message's affiliate_id. This will be saved back to the message
	 * along with its status by ReceivedManager.
	 *
	 * @param newAffiliateId
	 *            the affiliate id to use.
	 */
	public
	void setAffiliateId (
			int newAffiliateId);

	public
	Integer getServiceId ();

	public
	Integer getAffiliateId ();

}
