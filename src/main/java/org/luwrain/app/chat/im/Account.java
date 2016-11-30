
package org.luwrain.app.chat.im;

public interface Account
{
    Contact[] getContacts();
    void open(Runnable onFinished);
    void activate(Runnable onFinished);
    void sendMessage(String text, Contact contact);
    void addContact(String phone, String firstName, String lastName, Runnable finished);
}
