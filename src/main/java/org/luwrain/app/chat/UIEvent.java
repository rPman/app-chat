
package org.luwrain.app.chat;

interface UIEvent
{
    void onNewMessage();
    void onUnknownContactReciveMessage(String message);
}
