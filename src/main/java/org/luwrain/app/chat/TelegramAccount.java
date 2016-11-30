package org.luwrain.app.chat;

import java.util.Date;
import java.util.Vector;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;

import org.luwrain.app.chat.im.*;
import org.luwrain.app.chat.im.telegram.*;

public class TelegramAccount implements Account
{
    private final Luwrain luwrain;
    private final Settings.Telegram sett;
    private TelegramAccountListener listener;

    private final TelegramImpl messenger;
	private final Vector<Contact> contacts = new Vector<Contact>();

	// если любой контакт null, то это мы сами
	private Contact me=null;

    TelegramAccount(Luwrain luwrain, Settings.Telegram sett, 
TelegramAccountListener listener)
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
	messenger =new TelegramImpl(config,
				    new Events(){
@Override public void onIncomingMessage(String text, int date, int userId)
					{
					    NullCheck.notNull(text, "text");
					    luwrain.runInMainThread(()->					    receiveNewMessageImpl(text, date, userId));
					}
					@Override public void onWarning(String message)
					{
					    NullCheck.notNull(message, "message");
					    Log.warning("chat-telegram", message);
					}
					@Override public void onNewContact(Contact contact)
					{
					    NullCheck.notNull(contact, "contact");
					    Log.debug("chat-telegram", "receiving contact " + contact);
					    contacts.add(contact);
					}
					@Override public void onError(String message)
					{
					    NullCheck.notNull(message, "message");
					    Log.error("chat-telegram", message);
					}
					@Override public String askTwoPassAuthCode()
					{
					    return Popups.simple(luwrain, "Подключение к учетной записи", "Введите PIN:", "");
					}
					@Override public void onNewMessage(Message message,Contact recipient)
					{
					    recipient.getMessages().lastMessages().add(message);
					    listener.onNewMessage();
					}
					@Override public void onBeginAddingContact()
					{
					    Log.debug("chat-telegram", "starting receiving contacts");
					    contacts.clear();			
					}
				    },this, sett);
    }

    @Override public void open(Runnable onFinished)
	{	
	    NullCheck.notNull(onFinished, "onFinished");
	    new Thread(()->{
		    messenger.open();
		    luwrain.runInMainThread(onFinished);
}).start();
	}

    @Override public void activate(Runnable onFinished)
    {
messenger.connect();
messenger.getContacts();
onFinished.run();
    }

    @Override public Contact[] getContacts()
    {
	return contacts.toArray(new Contact[contacts.size()]);
    }

private void receiveNewMessageImpl(String message,int date,int userId)
	{
		//TelegramMessageImpl message=new TelegramMessageImpl();
		for(Contact c:contacts)
		{
			TelegramContactImpl contact=(TelegramContactImpl)c;
			if (contact.getUserId() == userId)
			{
				TelegramMessageImpl msg=new TelegramMessageImpl(message,new Date(),contact);
				contact.getMessages().lastMessages().add(msg);
listener.onNewMessage();
				return;
			}
		}
		//
//		addNewContact(userId);
listener.onUnknownContactReciveMessage(message);
//		System.out.println("пришло сообщение от неизвестного userId");
	}

//	private void addNewContact(int userId)
//	{
//		TelegramImpl timp=(TelegramImpl)messenger;
//		timp.addNewContact(userId);
//		
//	}



	@Override public Message sendNewMessage(String text,Contact contact)
	{
	    NullCheck.notNull(text, "text");
	    NullCheck.notNull(contact, "contact");
	    Log.debug("chat-telegram", "sending \"" + text + "\' to " + contact);
		TelegramImpl timp=(TelegramImpl)messenger;
		TelegramContactImpl tcontact=(TelegramContactImpl)contact;
		timp.sendNewMessage(tcontact.getAcessHash(),tcontact.getUserId(),text);
		Message message=new TelegramMessageImpl(text,new Date(),me);
		return message;
	}

    @Override public void addContact(String phone, String firstName,
				     String lastName, Runnable onFinished)
    {
	messenger.addNewContact(phone, firstName, lastName, ()->luwrain.runInMainThread(()->{
		messenger.getContacts();		
		onFinished.run();
		}));
    }

    @Override public String toString()
    {
	return messenger.getState() + Base.getPhoneDesignation(sett.getPhone(""));
    }
}
