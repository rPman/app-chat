package org.luwrain.app.chat.im.telegram;

import java.util.Date;

import org.luwrain.app.chat.im.Account;
import org.luwrain.app.chat.im.Contact;
import org.luwrain.app.chat.im.MessageList;

public class TelegramContactImpl implements Contact
{
	long accessHash;
	int userId;

	String firstName;
	String lastName;
	String userName;
	String phone;
	MessageList messages;
	Account account;
	
	TelegramContactImpl(Account account)
	{
		this.account=account;
	}
	
	@Override public Account getAccount()
	{
		return account;
	}

	void init(long accessHash,int userId,MessageList messages)
	{
		this.accessHash=accessHash;
		this.userId=userId;
		this.messages=messages;
	}

	void setUserInfo(String firstName,String lastName,String userName,String phone)
	{
		this.firstName=firstName;
		this.lastName=lastName;
		this.userName=userName;
		this.phone=phone;
	}
	
	@Override public String toString()
	{
		return phone+": "+firstName;
	}

	@Override public MessageList getMessages()
	{
		return messages;
	}
	
	public int getUserId()
	{
		return userId;
	}

	public long getAcessHash()
	{
		return accessHash;
	}

}
