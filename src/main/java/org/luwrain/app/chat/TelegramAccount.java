/*
   Copyright 2016 Ekaterina Koryakina <ekaterina_kor@mail.ru>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;
import org.luwrain.app.chat.base.*;
import org.luwrain.app.chat.base.telegram.*;

class TelegramAccount implements Account
{
    private final Luwrain luwrain;
    private final Settings.Telegram sett;
    private final String title;
    private final Listener listener;
    private final Telegram telegram;

	private final LinkedList<TelegramContact> contacts = new LinkedList<TelegramContact>();

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
		    if (contact instanceof TelegramContact)
		    	contacts.add((TelegramContact)contact);
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
	    luwrain.message("Информация для аутентификации устарела. Попытайтесь подключиться ещё раз для получения проверочного СМС.");
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
	for(TelegramContact c: contacts)
	{
	    if (c.getUserId() == userId)
	    {
		final Message msg=new Message(text, new Date(), c);
		c.registerMessage(msg);
		luwrain.message(text, Sounds.CHAT_MESSAGE);
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
    	TelegramContact contact=null;
    	for(TelegramContact c: contacts)
    	{
    	    if (c.getUserId() == userId)
    	    {
    	    	contact=c;
    	    	break;
    	    }
    	}
    	final Message msg=new Message(text, new Date(date*1000), contact);
    	from.registerMessage(msg);
   	}

    @Override public void sendMessage(String text,Contact contact)
    {
	NullCheck.notNull(text, "text");
	NullCheck.notNull(contact, "contact");
	Log.debug("chat-telegram", "sending \"" + text + "\' to " + contact);
	TelegramContact tcontact=(TelegramContact)contact;
	telegram.sendMessage(tcontact.getAcessHash(),tcontact.getUserId(),text);
	tcontact.registerMessage(new Message(text, new Date(), null));
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
