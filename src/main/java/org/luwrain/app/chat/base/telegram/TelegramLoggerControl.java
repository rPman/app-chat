
package org.luwrain.app.chat.base.telegram;

public class TelegramLoggerControl
{
	static
    {
		System.out.println("disable logs");
		org.telegram.mtproto.log.Logger.registerInterface(new org.telegram.mtproto.log.LogInterface() {
			public void w(String tag, String message) {}
			public void d(String tag, String message) {}
			public void e(String tag, String message) {            }
			public void e(String tag, Throwable t) {}
		    });
		org.telegram.api.engine.Logger.registerInterface(new org.telegram.api.engine.LoggerInterface() {
			public void w(String tag, String message) {}
			public void d(String tag, String message) {}
			public void e(String tag, String message) {}
			public void e(String tag, Throwable t) {}
		    });
    }
	// TODO: replace org.telegram.bot.services.BotLogger to disable file.log creation

}
