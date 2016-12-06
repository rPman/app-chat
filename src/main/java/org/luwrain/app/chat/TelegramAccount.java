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
    private final String title;
    private final Listener listener;
    private final Telegram telegram;

	private final LinkedList<TelegramContactImpl> contacts = new LinkedList<TelegramContactImpl>();

    TelegramAccount(Luwrain luwrain, Settings.Telegram sett, 
		    Listener listener)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(listener, "listener");
	this.luwrain=luwrain;
	this.sett = sett;
	this.title = sett.getName("").trim().isEmpty()?"---":sett.getName("").trim();
	this.listener = listener;
	telegram = new Telegram(sett,
	    new Events(){
		@Override public void onIncomingMessage(String text, int date, int userId)
		{
		    NullCheck.notNull(text, "text");
		    luwrain.runInMainThread(()->onIncomingMessageImpl(text, date, userId));
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
		    if (contact instanceof TelegramContactImpl)
		    	contacts.add((TelegramContactImpl)contact);
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
	    },this);
    }

    @Override public void open()
    {	
	new Thread(()->{
		telegram.open();
		luwrain.runInMainThread(()->listener.refreshTree());
	}).start();
    }

    @Override public void activate()
    {
	switch(telegram.getState())
	{
		case UNREGISTERED:
		case UNAUTHORIZED_NEEDSMS:
		    if (!Popups.confirmDefaultYes(luwrain, "Подключение новой учётной записи", "Учётная запись не подключена; для подключения вам будет выслан PIN-код, и открыто окно для его ввода. Вы хотите продолжить?"))
		    	return;
		    break;
		case UNAUTHORIZED:
		//	// FIXME: make possible to break authorization
			break;
		case AUTHORIZED:
			return;
		case ERROR:
		    if (!Popups.confirmDefaultYes(luwrain, "Подключение новой учётной записи", "Предыдущая попытка подключения провалилась; запросить PIN-кода, и открыто окно для его ввода. Вы хотите продолжить?"))
		    	return;
		    break;
	}
	telegram.connect();
	telegram.getContacts();
	luwrain.playSound(Sounds.DONE);
	listener.refreshTree();
    }

    @Override public Contact[] getContacts()
    {
	return contacts.toArray(new Contact[contacts.size()]);
    }

    private void onIncomingMessageImpl(String text,int date,int userId)
    {
	NullCheck.notNull(text, "text");
	for(TelegramContactImpl c: contacts)
	{
	    if (c.getUserId() == userId)
	    {
		final Message msg=new Message(text, new Date(), c);
		c.registerNewMessage(msg);
		//		luwrain.playSound(Sounds.CHAT_MESSAGE);
		luwrain.message(text, Luwrain.MESSAGE_OK);
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
	Log.debug("chat-telegram", "adding contact " + phone);
	telegram.addNewContact(phone, firstName, lastName, ()->luwrain.runInMainThread(()->{
		    telegram.getContacts();		
		    listener.refreshTree();
		}));
    }

    @Override public String toString()
    {
	final String prefix;
	switch(telegram.getState())
	{
	case UNREGISTERED:			prefix = "SMS! "; break;
	case UNAUTHORIZED_NEEDSMS:	prefix = "SMS? "; break;
	case UNAUTHORIZED:			prefix = "..."; break;
	case ERROR:					prefix = "! "; break;
	case AUTHORIZED:			prefix = ""; break;
	default:					prefix = "";
	}
	return prefix + title;
    }
}
