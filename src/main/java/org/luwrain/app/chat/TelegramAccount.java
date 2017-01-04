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
		@Override public void onHistoryMessage(Contact from,String text,int date,int userId,boolean unread)
		{
			NullCheck.notNull(text, "text");
		    luwrain.runInMainThread(()->onHistoryMessageImpl(from,text, date, userId,unread));
			
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
		case REQUIRE_SIGN_UP:
		case REQUIRE_SIGN_IN:
		case REQUIRE_RESIGN_IN:
		    if (!Popups.confirmDefaultYes(luwrain, "Подключение новой учётной записи", "Учётная запись не подключена; для подключения вам будет выслан PIN-код, и открыто окно для его ввода. Вы хотите продолжить?"))
		    	return;
		    break;
		case AUTHORIZED:
			return;
	}
	/*
		case ERROR:
		    if (!Popups.confirmDefaultYes(luwrain, "Подключение новой учётной записи", "Предыдущая попытка подключения провалилась; запросить PIN-кода, и открыто окно для его ввода. Вы хотите продолжить?"))
		    	return;
		    break;
	}
	*/
	telegram.connect();
	if (telegram.getState() == Telegram.State.REQUIRE_RESIGN_IN)
	{
	    luwrain.message("ККод устарел. Надо пробовать ещё раз.");
	    return;
	}
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
    
    protected void onHistoryMessageImpl(Contact from,String text,int date,int userId,boolean unread)
   	{
    	// FIXME: add unread support for messages
    	NullCheck.notNull(text, "text");
    	TelegramContactImpl contact=null;
    	for(TelegramContactImpl c: contacts)
    	{
    	    if (c.getUserId() == userId)
    	    {
    	    	contact=c;
    	    	break;
    	    }
    	}
    	final Message msg=new Message(text, new Date(date*1000), contact);
    	from.registerHistoryMessage(msg,unread);
   	}

    @Override public void sendMessage(String text,Contact contact)
    {
	NullCheck.notNull(text, "text");
	NullCheck.notNull(contact, "contact");
	Log.debug("chat-telegram", "sending \"" + text + "\' to " + contact);
	TelegramContactImpl tcontact=(TelegramContactImpl)contact;
	telegram.sendMessage(tcontact.getAcessHash(),tcontact.getUserId(),text);
	Message message=new Message(text,new Date(),contact);
	tcontact.registerHistoryMessage(message,true);
    }

    @Override public void addContact(String phone, String firstName,
				     String lastName, Runnable onFinished)
    {
	Log.debug("chat-telegram", "adding contact " + phone);
	telegram.addContact(phone, firstName, lastName, ()->luwrain.runInMainThread(()->{
		    telegram.getContacts();		
		    listener.refreshTree();
		}));
    }

    @Override public String toString()
    {
	return title;
    }
}
