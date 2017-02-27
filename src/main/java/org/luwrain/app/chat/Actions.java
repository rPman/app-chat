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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.app.chat.base.Account;
import org.luwrain.app.chat.base.Contact;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

class Actions
{
    private final Luwrain luwrain;

    Actions(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    Action[] getTreeActions()
    {
	return new Action[]{
	    new Action("add-contact", "Добавить контакт", new KeyboardEvent(KeyboardEvent.Special.INSERT)),
	};
    }

    public void onChekedContact(TreeArea area)
    {
	area.refresh();
    }

    boolean onContactsClick(ListArea contactsArea,ChatArea chatArea, Object obj)
    {
	if (obj instanceof Contact)
	{
	    final Contact contact = (Contact)obj;
	    chatArea.setCurrentContact(contact);
	    luwrain.setActiveArea(chatArea);
	    return true;
	}
	if (obj instanceof Account)
	{		
	    ((Account)obj).activate();
	    return true;
	}
	return false;
    }

    boolean onAddContact(ListArea contactsArea,ChatArea chatArea)
    {
	final Object obj = contactsArea.selected();
	if (obj == null)
	    return false;
	final Account account;
	if (obj instanceof Account)
	    account = (Account)obj; else
	    if (obj instanceof Contact)
		account = ((Contact)obj).getAccount(); else
		return false;
		final String phone = Popups.simple(luwrain, "Добавление контакта", "Введите номер мобильного телефона:", "");
		if (phone == null || phone.trim().isEmpty())
		    return true;

		final String firstName = Popups.simple(luwrain, "Добавление контакта", "Введите имя:","");
		if (firstName == null || firstName.trim().isEmpty())
		    return true;
		    final String lastName = Popups.simple(luwrain, "Добавление контакта", "Введите второе имя:","");
		    if (lastName == null || lastName.trim().isEmpty())
			return true;
		    account.addContact(phone, firstName, lastName, ()->contactsArea.refresh());
	return true;
    }
}
