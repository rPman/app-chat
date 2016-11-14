package org.luwrain.app.chat.im;

import java.util.Collection;
import java.util.Date;
import java.util.Vector;

public interface Messages 
{
	Vector<Message> lastMessage();
	int unreadCount();
	void decreaseCount(int cnt);
	
}
