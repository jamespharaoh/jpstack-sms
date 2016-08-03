package wbs.sms.message.core.model;

import java.util.Set;

import lombok.Getter;

import com.google.common.collect.ImmutableSet;

public
enum MessageStatus {

	pending (0, "pending", 0),
	processed (1, "processed", 1),
	cancelled (2, "cancelled", -1),
	failed (3, "failed", -1),
	sent (4, "sent", 0),
	delivered (5, "delivered", 1),
	undelivered (6, "undelivered", -1),
	notProcessed (7, "not processed", -1),
	ignored (8, "ignored", -1),
	manuallyProcessed (9, "manually processed", 1),
	submitted (10, "submitted", 0),
	reportTimedOut (11, "report timed out", -1),
	held (12, "held", 0),
	blacklisted (13, "blacklisted", -1),
	manuallyUndelivered (14, "manually undelivered", -1),
	manuallyDelivered (15, "manually delivered", 1),
	rejected (16, "rejected", -1);

	@Getter
	int ordinal;

	@Getter
	String description;

	@Getter
	int type;

	MessageStatus (
			int newOrdinal,
			String newDescription,
			int newType) {

		ordinal = newOrdinal;
		description = newDescription;
		type = newType;

	}

	public
	boolean isGoodType () {
		return type > 0;
	}

	public
	boolean isBadType () {
		return type < 0;
	}

	public
	boolean isPending () {
		return type == 0;
	}

	public static
	Set<MessageStatus> badStatus;

	public static
	Set<MessageStatus> goodStatus;

	static {

		ImmutableSet.Builder<MessageStatus> goodBuilder =
			ImmutableSet.<MessageStatus>builder ();

		ImmutableSet.Builder<MessageStatus> badBuilder =
			ImmutableSet.<MessageStatus>builder ();

		for (MessageStatus status
				: MessageStatus.values ()) {

			if (status.isGoodType ())
				goodBuilder.add (status);

			if (status.isBadType ())
				badBuilder.add (status);

		}

		badStatus =
			badBuilder.build ();

		goodStatus =
			goodBuilder.build ();

	}

}
