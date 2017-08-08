package wbs.utils.thread;

import java.util.concurrent.ThreadFactory;

public
interface ThreadManager
	extends ThreadFactory {

	Thread makeThread (
			Runnable target);

	Thread startThread (
			Runnable target);

	Thread makeThread (
			Runnable target,
			String name);

	Thread startThread (
			Runnable target,
			String name);

}