
package org.luwrain.app.chat.im;

import java.util.*;

import org.luwrain.core.*;

public class Message
{
    public final Date date;
    public final 	String text;
    public final 	Contact contact;

    public Message(String text, Date date, Contact contact)
    {
	NullCheck.notNull(text, "text");
	NullCheck.notNull(date, "date");
	NullCheck.notNull(contact, "contact");
	this.text = text;
	this.date = date;
	this.contact = contact;
    }
}
