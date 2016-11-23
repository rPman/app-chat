package org.luwrain.app.chat.im;

import java.util.Date;

public interface Message
{
	Date getDate();
	String getMessage();
	Contact getAuthor();
}
