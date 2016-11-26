
package org.luwrain.app.chat.im;

public interface Account
{
    Contact[] getContacts();
    void doAutoConnect(Runnable finish);
    void onConnect(Runnable finish);
    //    Messenger getMessenger();
    Message sendNewMessage(String text, Contact contact);
    void askCreateContact(Runnable finished);
}
