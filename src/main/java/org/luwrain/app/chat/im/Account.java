
package org.luwrain.app.chat.im;

public interface Account
{
    Contact[] getContacts();
    void autoConnect(Runnable onFinished);
    void connect(Runnable onFinished);
    Message sendNewMessage(String text, Contact contact);
    void askCreateContact(Runnable finished);
}
