package wbs.utils.etc;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

public
class ThreadUtils {

	public static
	void threadInterruptAndJoinIgnoreInterrupt (
			@NonNull Thread thread) {

		thread.interrupt ();

		for (;;) {

			thread.interrupt ();

			try {

				thread.join ();

				break;

			} catch (InterruptedException interruptedException) {

				doNothing ();

			}

		}

	}

}
