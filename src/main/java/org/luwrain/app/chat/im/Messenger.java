
package org.luwrain.app.chat.im;

public interface Messenger
{
    void go();
    //    void twoPass(String code);
    void checkContacts(String q);
    void finish();
}
