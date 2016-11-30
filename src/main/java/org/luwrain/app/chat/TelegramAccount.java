package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;
import org.luwrain.app.chat.im.*;
import org.luwrain.app.chat.im.telegram.*;

class TelegramAccount implements Account
{
    private final Luwrain luwrain;
    private final Settings.Telegram sett;
    private final Listener listener;
    private final Telegram telegram;

	private final LinkedList<Contact> contacts = new LinkedList<Contact>();

    TelegramAccount(Luwrain luwrain, Settings.Telegram sett, 
		    Listener listener)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(listener, "listener");
	this.luwrain=luwrain;
	this.sett = sett;
	this.listener = listener;
	final Config config = new Config();
	config.firstName = sett.getFirstName("");
	config.lastName = sett.getLastName("");
	config.phone = sett.getPhone("");
	telegram = new Telegram(config,
				    new Events(){
					@Override public void onIncomingMessage(String text, int date, int userId)
					{
					    NullCheck.notNull(text, "text");
					    luwrain.runInMainThread(()->					    onIncomingMessageImpl(text, date, userId));
					}
					@Override public void onWarning(String message)
					{
					    NullCheck.notNull(message, "message");
					    Log.warning("chat-telegram", message);
					}
					@Override public void onBeginAddingContact()
					{
					    contacts.clear();			
					}
					@Override public void onNewContact(Contact contact)
					{
					    NullCheck.notNull(contact, "contact");
					    contacts.add(contact);
					}
					@Override public void onError(String message)
					{
					    NullCheck.notNull(message, "message");
					    Log.error("chat-telegram", message);
					    luwrain.runInMainThread(()->luwrain.message(message, Luwrain.MESSAGE_ERROR));
					}
					@Override public String askTwoPassAuthCode()
					{
					    return Popups.simple(luwrain, "Подключение к учетной записи", "Введите PIN:", "");
					}
				    },this, sett);
    }

    @Override public void open(Runnable onFinished)
	{	
	    NullCheck.notNull(onFinished, "onFinished");
	    new Thread(()->{
		    telegram.open();
		    luwrain.runInMainThread(onFinished);
}).start();
	}

    @Override public void activate(Runnable onFinished)
    {
	telegram.connect();
telegram.getContacts();
onFinished.run();
    }

    @Override public Contact[] getContacts()
    {
	return contacts.toArray(new Contact[contacts.size()]);
    }

private void onIncomingMessageImpl(String text,int date,int userId)
	{
	    NullCheck.notNull(text, "text");
	    for(Contact c: contacts)
	    {
		TelegramContactImpl contact=(TelegramContactImpl)c;
		if (contact.getUserId() == userId)
		{
		    final TelegramMessageImpl msg=new TelegramMessageImpl(text, new Date(),contact);
		    contact.registerNewMessage(msg);
		    listener.refreshTree();
		    listener.refreshChatArea();
		    return;
		}
	    }
	    luwrain.message("Unknown contact " + text);
	}

	@Override public void sendMessage(String text,Contact contact)
	{
	    NullCheck.notNull(text, "text");
	    NullCheck.notNull(contact, "contact");
	    Log.debug("chat-telegram", "sending \"" + text + "\' to " + contact);
		TelegramContactImpl tcontact=(TelegramContactImpl)contact;
		telegram.sendNewMessage(tcontact.getAcessHash(),tcontact.getUserId(),text);
	}

    @Override public void addContact(String phone, String firstName,
				     String lastName, Runnable onFinished)
    {
	telegram.addNewContact(phone, firstName, lastName, ()->luwrain.runInMainThread(()->{
		    telegram.getContacts();		
		onFinished.run();
		}));
    }

    @Override public String toString()
    {
	return telegram.getState() + Base.getPhoneDesignation(sett.getPhone(""));
    }
}
