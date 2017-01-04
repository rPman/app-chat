
package org.luwrain.app.chat.base;

public interface Account
{
    Contact[] getContacts();
    void open();
    void activate();
    void sendMessage(String text, Contact contact);
    void addContact(String phone, String firstName, String lastName, Runnable finished);
}
