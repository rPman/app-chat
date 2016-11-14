package org.luwrain.app.chat;

import java.util.Vector;

import org.luwrain.app.chat.im.ChatMenu;
import org.luwrain.app.chat.im.Contact;
import org.luwrain.app.chat.im.Events;
import org.luwrain.app.chat.im.Messagener;
import org.luwrain.app.chat.im.telegram.Config;
import org.luwrain.app.chat.im.telegram.TelegramImpl;
import org.luwrain.core.Luwrain;
import org.luwrain.popups.Popups;

public class TelegramAccauntImpl implements ChatMenu
{
	private ConfigAccessor.Telegram config;
	private Messagener messagener=null;
	private Luwrain luwrain;
	boolean status=false;
	Vector<Contact> contacts;
	
	private static Object autorun=autoRun(); 
	public TelegramAccauntImpl(Luwrain luwrain,ConfigAccessor.Telegram config)
	{
		this.config=config;
		this.luwrain=luwrain;
		this.contacts=new Vector<Contact>();
	}

	private static Object autoRun()
	{
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
		return null;
	}

	public TelegramAccauntImpl()
	{
	}

	public ConfigAccessor.Telegram getConfig()
	{
		return config;
	}

	public String toString()
	{
		return (status? "*":" ")+"Telegram:"+config.getPhone("");
	}

	
	@Override public void onClick()
	{
		// TODO Auto-generated method stub
		if (messagener==null)
		{
			Config config=new Config();
			config.firstName="fhgfhfg";
			config.lastName="gfhgfh";
			config.phone="79528900423";
			
			messagener=new TelegramImpl(config);
			messagener.go(new Events()
			{
				
				@Override public void onWarning(String message)
				{
					// TODO Auto-generated method stub
					System.out.println("onWarning"+message);
				}
				
				@Override public void onNewContact(Contact contact)
				{
					contacts.add(contact);
				}
				
				@Override public void onError(String message)
				{
					System.out.println("onError"+message);
					messagener.finish();				
				}
				
				@Override public void onAuthFinish()
				{
					System.out.println("onAuthFinish");
					messagener.checkContacts("gdfg");
					status=true;
				}
				
				@Override public void on2PassAuth(String message)
				{
					// TODO Auto-generated method stub
					String code=Popups.simple(luwrain, "Подключение к учетной записи", message, "");
					if (code==null || code.isEmpty()) 
					{ 
						messagener.finish();
						return;
					}
					messagener.twoPass(code);
				}
			});
		}
	}

	@Override public Contact[] getContacts()
	{
		return contacts.toArray(new Contact[contacts.size()]);
	}

}
