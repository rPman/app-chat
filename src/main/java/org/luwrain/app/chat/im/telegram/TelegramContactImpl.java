package org.luwrain.app.chat.im.telegram;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.app.chat.im.*;

public class TelegramContactImpl implements Contact
{
    long accessHash;
	int userId;

	String firstName;
	String lastName;
	String userName;
	String phone;

    private Message[] messages = new Message[0];
	private final Account account;

	TelegramContactImpl(Account account)
	{
	    NullCheck.notNull(account, "account");
	    this.account = account;
	}
	
	@Override public Account getAccount()
	{
		return account;
	}

	void init(long accessHash,int userId)
	{
		this.accessHash=accessHash;
		this.userId=userId;
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

	@Override public Message[] getMessages()
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

	@Override public void decreaseCount(int cnt)
    {
    }

    @Override public int getUnreadCount()
    {
	return 0;
    }

    @Override public void registerNewMessage(Message message)
    {
	NullCheck.notNull(message, "message");
	messages = Arrays.copyOf(messages, messages.length + 1);
	messages[messages.length - 1] = message;
    }

}
