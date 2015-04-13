package wbs.sms.message.inbox.daemon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("commandManager")
public
class CommandManagerProxy
	implements CommandManager {

	@Getter @Setter
	@Delegate
	CommandManagerMethods delegate;

}
