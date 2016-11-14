package org.luwrain.app.chat.im.telegram;

import org.luwrain.app.chat.im.Contact;

public class TelegramContactImpl implements Contact
{
	long accessHash;
	int userId;
	String firstName;
	String lastName;
	String userName;
	String phone;
	void TelegramContactImpl(long accessHash,int userId)
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
}
