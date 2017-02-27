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
import org.luwrain.core.events.EnvironmentEvent;
import org.luwrain.app.chat.base.*;
import org.luwrain.controls.*;

class Base
{
    private Luwrain luwrain;
    private Listener listener;
    private Account[] accounts;
    private ContactsModel contactsModel = null;

    boolean init(Luwrain luwrain, Listener listener)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(listener, "listener");
	this.luwrain = luwrain;
	this.listener = listener;
	this.accounts = loadAccounts();
	contactsModel = new ContactsModel(this);
	return true;
    }

    Account[] getAccounts()
    {
	return accounts;
    }

    private Account[] loadAccounts()
    {
	Log.debug("chat", "loading accounts");
	final LinkedList<Account> res = new LinkedList<Account>();
	final Registry registry = luwrain.getRegistry();
	registry.addDirectory(Settings.ACCOUNTS_PATH);
	for (String str : registry.getDirectories(Settings.ACCOUNTS_PATH))
	{
	    String accountPath = Registry.join(Settings.ACCOUNTS_PATH, str);
	    final Settings.Base type=RegistryProxy.create(luwrain.getRegistry(), accountPath, Settings.Base.class);
	    switch(type.getType("").trim().toLowerCase())
	    {
	    case "telegram":
		{
		    final Settings.Telegram sett = Settings.createTelegram(luwrain.getRegistry(), accountPath );
		    res.add(new TelegramAccount(luwrain, sett, listener));
		}
		break;
	    default:
		break;
	    }
	}
	Log.debug("chat", "loaded " + res.size() + " accounts");
	return res.toArray(new TelegramAccount[res.size()]);
    }

    ListArea.Model getContactsModel()
    {
	return contactsModel;
    }

    static String getPhoneDesignation(String str)
    {
	if (str.length() < 5)
	    return str;
	return str.substring(str.length() - 4);
    }
}
