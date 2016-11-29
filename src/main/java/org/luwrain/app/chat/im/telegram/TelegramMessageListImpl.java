
package org.luwrain.app.chat.im.telegram;

import java.util.Vector;

import org.luwrain.app.chat.im.Message;
import org.luwrain.app.chat.im.MessageList;

public class TelegramMessageListImpl implements MessageList
{
	Vector<Message> history;
	int unreadCount;

	@Override public Vector<Message> lastMessages()
	{
		// TODO Auto-generated method stub
		return history;
	}
	
	public TelegramMessageListImpl()
	{
		history=new Vector<Message>();
	}

	@Override public int unreadCount()
	{
		// TODO Auto-generated method stub
		return unreadCount;
	}

	@Override public void decreaseCount(int cnt)
	{
		unreadCount-=cnt;
	}

	@Override public void addUnreadMessage()
	{
		unreadCount++;
		
	}
	

}
