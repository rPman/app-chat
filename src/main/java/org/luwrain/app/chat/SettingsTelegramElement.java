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
import org.luwrain.cpanel.*;

class SettingsTelegramElement implements Element
{
    private final Element parent;
    private final Settings.Telegram telegram;
    private String title;

    SettingsTelegramElement(Element parent, Settings.Telegram telegram,
String title)
    {
	NullCheck.notNull(parent, "parent");
	NullCheck.notNull(telegram, "telegram");
	NullCheck.notEmpty(title, "title");
	this.parent = parent;
	this.telegram = telegram;
	this.title = title;
    }

    @Override public Element getParentElement()
    {
	return parent;
    }

    @Override public boolean equals(Object o)
    {
	if (o == null || !(o instanceof SettingsTelegramElement))
	    return false;
	return title.equals(((SettingsTelegramElement)o).title);
    }

    @Override public int hashCode()
    {
	return title.hashCode();
    }

    String getTitle()
    {
	return title;
    }

    Settings.Telegram getTelegram()
    {
	return telegram;
    }
}
