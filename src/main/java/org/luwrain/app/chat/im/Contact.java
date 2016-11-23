package org.luwrain.app.chat.im;

public interface Contact
{
	String toString();
	MessageList getMessages();
	Account getAccount();
}
