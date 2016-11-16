package org.luwrain.app.chat;

import java.util.Vector;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;

import org.luwrain.app.chat.im.*;
import org.luwrain.app.chat.im.telegram.*;

class TelegramAccount implements Account
{
    private final Luwrain luwrain;
    private final Settings.Telegram sett;
    private Messenger messenger;
	private final Vector<Contact> contacts = new Vector<Contact>();;

    private boolean status = false;

    private static Object autorun=autoRun(); 

    TelegramAccount(Luwrain luwrain, Settings.Telegram sett)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(sett, "sett");
	this.luwrain=luwrain;
	this.sett = sett;
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

    @Override public void onClick()
    {
	if (messenger  != null)
	    return;
	final Config config = new Config();
	config.firstName = sett.getFirstName("");
	config.lastName = sett.getLastName("");
	config.phone = sett.getPhone("");
	messenger =new TelegramImpl(config,
new Events(){
		@Override public void onWarning(String message)
		{
		    NullCheck.notNull(message, "message");
		    Log.warning("chat-telegram", message);
		}
		@Override public void onNewContact(Contact contact)
		{
		    contacts.add(contact);
		}
		@Override public void onError(String message)
		{
		    NullCheck.notNull(message, "message");
		    Log.error("chat-telegram", message);
		    //		    messenger.finish();				
		}
		@Override public void onAuthFinish()
		{
		    System.out.println("onAuthFinish");
		    //		    messenger.checkContacts("gdfg");
		    status=true;
		}
		@Override public String askTwoPassAuthCode(String message)
		{
		    NullCheck.notEmpty(message, "message");
return Popups.simple(luwrain, "Подключение к учетной записи", message, "");
		}
	    });

	Log.debug("chat", "Telegram messenger for " + sett.getPhone("") + " prepared");
	messenger.go();
    }

    @Override public Contact[] getContacts()
    {
	return contacts.toArray(new Contact[contacts.size()]);
    }

    Settings.Telegram getSettings()
    {
	return sett;
    }

    @Override public String toString()
    {
	return (status? "*":" ")+"Telegram:"+sett.getPhone("");
    }
}
