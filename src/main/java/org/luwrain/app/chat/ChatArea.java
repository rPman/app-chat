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
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.app.chat.base.Account;
import org.luwrain.app.chat.base.Contact;
import org.luwrain.app.chat.base.Message;
import org.luwrain.controls.*;

class ChatArea extends ConsoleArea
{
    private Contact contact = null;

    ChatArea(Luwrain luwrain, String areaName)
    {
	super(prepareParams(luwrain, areaName));
    }

    void setCurrentContact(Contact contact)
	{
	    NullCheck.notNull(contact, "contact");
		this.contact = contact;
		final Message[] messages = contact.getMessages();
		if (messages != null)
		    setItems(messages);
		super.refresh();
	}

    static private Params prepareParams(Luwrain luwrain, String areaName)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(areaName, "areaName");
	final Params params = new Params();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.appearance = new Appearance(luwrain);
	params.areaName = areaName;
	return params;
    }

    static private class Appearance implements ConsoleArea.Appearance
    {
	private final Luwrain luwrain;

	Appearance(Luwrain luwrain)
	{
	    NullCheck.notNull(luwrain, "luwrain");
	    this.luwrain = luwrain;
	}

@Override public void announceItem(Object item)
	{
	    NullCheck.notNull(item, "item");
	    if (!(item instanceof Message))
		return;
	    final Message message = (Message)item;
	    luwrain.silence();
	    if (message.contact != null)
		luwrain.playSound(Sounds.PARAGRAPH);
	    luwrain.say(message.text + " " + luwrain.i18n().getPastTimeBrief(message.date));
	}

@Override public String getTextAppearance(Object item)
	{
	    return "kaka";
	}
    }
}
