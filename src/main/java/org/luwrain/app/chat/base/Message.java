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

package org.luwrain.app.chat.base;

import java.util.*;

import org.luwrain.core.*;

public class Message
{
    public final Date date;
    public final String text;
    public final Contact contact;

    /**
     * @param contact null for my messages
     */
    public Message(String text, Date date, Contact contact)
    {
	NullCheck.notNull(text, "text");
	NullCheck.notNull(date, "date");
	this.text = text;
	this.date = date;
	this.contact = contact;
    }
}
