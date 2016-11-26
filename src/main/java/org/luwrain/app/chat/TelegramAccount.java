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

    private TelegramImpl messenger;
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
    }

    @Override public void onConnect(Runnable finish)
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
		}
		@Override public void onAuthFinish()
		{
		    Log.debug("chat-telegram",  "onAuthFinish");
		    messenger.checkContacts();
		    finish.run();
		}
		@Override public String askTwoPassAuthCode(String message)
		{
		    NullCheck.notEmpty(message, "message");
		    return Popups.simple(luwrain, "Подключение к учетной записи", message, "");
		}
		@Override public void onNewMessage(Message message,Contact recipient)
		{
			recipient.getMessages().lastMessages().add(message);
listener.onNewMessage();
		}
		@Override public void onBeginAddingContact()
		{
			contacts.clear();			
		}
	    },this);
	Log.debug("chat", "Telegram messenger for " + sett.getPhone("") + " prepared");
	messenger.run();
    }

    @Override public Contact[] getContacts()
    {
	return contacts.toArray(new Contact[contacts.size()]);
    }


    @Override public void doAutoConnect(Runnable finish)
	{	
	    //		final TelegramAccount that=this;
		Thread thread=new Thread(new Runnable(){
			@Override public void run()
			{
			    Boolean authconnect=sett.getAutoConnect(true);
				//TODO: null почему не null, когда в реестре значение не установлено
				if (authconnect==null) 
authconnect=true;
				if (authconnect==true)
				{
				    onConnect(finish);
				}				
			}});
		thread.start();

	}

public void receiveNewMessage(String message,int date,int userId)
	{
		//TelegramMessageImpl message=new TelegramMessageImpl();
		for(Contact c:contacts)
		{
			TelegramContactImpl contact=(TelegramContactImpl)c;
			if (contact.getUserId()==userId)
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
		TelegramImpl timp=(TelegramImpl)messenger;
		TelegramContactImpl tcontact=(TelegramContactImpl)contact;
		timp.sendNewMessage(tcontact.getAcessHash(),tcontact.getUserId(),text);
		Message message=new TelegramMessageImpl(text,new Date(),me);
		return message;
	}

	@Override public void askCreateContact(Runnable finished)
	{
		String phone = Popups.simple(luwrain, "Добавление нового контакта", "Введите номер мобильного телефона:", "");
		phone=phone.trim();
		if(phone==null || phone.isEmpty())
		    return;
		String firstname = Popups.simple(luwrain, "Добавление нового контакта", "Введите имя ","");
		firstname=firstname.trim();
		String lastname="";
		if (!(firstname==null || firstname.isEmpty()))
		{
		    lastname = Popups.simple(luwrain, "Добавление нового контакта", "Введите второе имя ","");
		    lastname=lastname.trim();
		}
		TelegramImpl timp=(TelegramImpl)messenger;
		timp.addNewContact(phone,firstname,lastname,new Runnable()
		{
			
			@Override public void run()
			{
				messenger.checkContacts();		
				finished.run();
			}
				});
			}

    Settings.Telegram getSettings()
    {
	return sett;
    }

    @Override public String toString()
    {
	return (messenger==null?"":messenger.getState().name())+":Telegram:"+sett.getPhone("");
    }
}
