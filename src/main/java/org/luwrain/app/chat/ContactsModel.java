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
import org.luwrain.controls.*;

import org.luwrain.app.chat.base.*;

class ContactsModel implements ListArea.Model
{
    private final Base base;

    ContactsModel(Base base)
    {
	NullCheck.notNull(base, "base");
	this.base = base;
    }

    @Override public int getItemCount()
    {
	return buildList().length;
    }

    @Override public Object getItem(int index)
    {
	final Object[] res = buildList();
	if (res != null && index < res.length)
	    return res[index];
	return null;
    }

    @Override public void refresh()
    {
    }

    @Override public boolean toggleMark(int index)
    {
	return false;
    }

    private Object[] buildList()
    {
	final LinkedList res = new LinkedList();
	for(Account a: base.getAccounts())
	{
	    res.add(a);
	    final Contact[] contacts = a.getContacts();
	    if (contacts != null)
		for(Contact c: contacts)
		    res.add(c);
	}
	return res.toArray(new Object[res.size()]);
    }
}
