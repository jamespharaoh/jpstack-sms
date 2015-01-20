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
	String getRest ();

	Integer getServiceId ();

	public
	Integer getAffiliateId ();

}
