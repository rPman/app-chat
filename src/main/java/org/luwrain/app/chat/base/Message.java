
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
