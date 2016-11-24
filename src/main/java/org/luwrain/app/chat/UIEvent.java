package org.luwrain.app.chat;

public interface UIEvent
{
	void onNewMessage();
	void onUnknownContactReciveMessage(String message);
}
