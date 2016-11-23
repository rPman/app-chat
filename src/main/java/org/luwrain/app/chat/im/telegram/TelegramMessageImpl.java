package org.luwrain.app.chat.im.telegram;

import java.util.Date;

import org.luwrain.app.chat.im.Contact;
import org.luwrain.app.chat.im.Message;

public class TelegramMessageImpl implements Message
{
	Date date;
	String message;
	Contact author; // TODO удалить?

	@Override public Date getDate()
	{
		return date;
	}

	@Override public String getMessage()
	{
		return message;
	}
	public TelegramMessageImpl(String message,Date date,Contact author)
	{
		this.message=message;
		this.date=date;
		this.author=author;
	}

	@Override public Contact getAuthor()
	{
		return author;
	}
	
}
