
package org.luwrain.app.chat.base;

public interface Contact
{
    Account getAccount();
    Message[] getMessages();
    void registerNewMessage(Message message);
    int getUnreadCount();
    void decreaseCount(int cnt);
	void registerHistoryMessage(Message msg,boolean unread);
}
