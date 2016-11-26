
package org.luwrain.app.chat.im;

import org.luwrain.app.chat.im.State;

public interface Messenger
{
    //    void go();
    //    void twoPass(String code);
    void checkContacts();
    void finish();
	State getState();
	void setState(State state);
}
