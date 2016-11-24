
package org.luwrain.app.chat.im;

import org.luwrain.app.chat.im.Messenger.State;

public interface Messenger
{
	public enum State {
	    	/**  требуется регистрация, будет прислан смс */
	    	unregitred,
	    	/**  требуется подтверждение смс */
	    	registred,
	    	/**  авторизован, смс не потребуется*/
	    	authorized
	    	/** еще не запущена */,
	    	none}
    void go();
    //    void twoPass(String code);
    void checkContacts();
    void finish();
	State getState();
	void setState(State state);
}
