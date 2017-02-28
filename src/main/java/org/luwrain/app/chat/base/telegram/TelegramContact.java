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

package org.luwrain.app.chat.base.telegram;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.app.chat.base.*;

public class TelegramContact implements Contact
{
    long accessHash;
    int userId;

    String firstName;
    String lastName;
    String userName;
    String phone;

    private Message[] messages = new Message[0];
    private final Account account;

    TelegramContact(Account account)
    {
	NullCheck.notNull(account, "account");
	this.account = account;
    }

    @Override public Account getAccount()
    {
	return account;
    }

    void init(long accessHash,int userId)
    {
	this.accessHash=accessHash;
	this.userId=userId;
    }

    void setUserInfo(String firstName,String lastName,String userName,String phone)
    {
	this.firstName=firstName;
	this.lastName=lastName;
	this.userName=userName;
	this.phone=phone;
    }

    @Override public String toString()
    {
	return firstName + " " + lastName;
    }

    @Override public Message[] getMessages()
    {
	return messages;
    }

    public int getUserId()
    {
	return userId;
    }

    public long getAcessHash()
    {
	return accessHash;
    }

    @Override public void decreaseCount(int cnt)
    {
    }

    @Override public int getUnreadCount()
    {
	return 0;
    }

    @Override public void registerMessage(Message message)
    {
	NullCheck.notNull(message, "message");
	messages = Arrays.copyOf(messages, messages.length + 1);
	messages[messages.length - 1] = message;
    }
}
