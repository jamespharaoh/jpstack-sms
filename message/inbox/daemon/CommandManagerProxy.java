package wbs.sms.message.inbox.daemon;

import lombok.Delegate;
import lombok.Getter;
import lombok.Setter;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("commandManager")
public
class CommandManagerProxy
	implements CommandManager {

	@Getter @Setter
	@Delegate
	CommandManagerMethods delegate;

}
