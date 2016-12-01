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
				    },this, sett);
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
	if (telegram.getState() == State.UNREGISTERED || telegram.getState() == State.REGISTERED)
	    if (!Popups.confirmDefaultYes(luwrain, "Подключение новой учётной записи", "Учётная запись не подключена; для подключения вам будет выслан PIN-код, и открыто окно для его ввода. Вы хотите продолжить?"))
		return;
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
	case UNREGISTERED:
	case REGISTERED:
	    prefix = "?? ";
	    break;
	case READY_FOR_AUTHORIZATION:
	    prefix = "...";
	    break;
	default:
	    prefix = "";
	}
	return prefix + title;
    }
}
