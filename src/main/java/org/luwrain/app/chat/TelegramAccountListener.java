
package org.luwrain.app.chat;

interface TelegramAccountListener
{
    void onNewMessage();
    void onUnknownContactReciveMessage(String message);
}
