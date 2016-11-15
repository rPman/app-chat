
package org.luwrain.app.chat.im.telegram;

import java.util.Vector;

import org.luwrain.app.chat.im.Message;
import org.luwrain.app.chat.im.Messages;

public class TelegramMessages implements Messages
{
	Vector<Message> history;
	int history_count;

	@Override public Vector<Message> lastMessage()
	{
		// TODO Auto-generated method stub
		return history;
	}
	
	void TelegramMessagerImpl()
	{
		history=new Vector<Message>();
	}

	@Override public int unreadCount()
	{
		// TODO Auto-generated method stub
		return history_count;
	}

	@Override public void decreaseCount(int cnt)
	{
		history_count-=cnt;
	}
	

}
