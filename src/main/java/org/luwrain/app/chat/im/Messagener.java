package org.luwrain.app.chat.im;

public interface Messagener {
	
	void go(Events events);
	void twoPass(String code);
	void checkContacts(String q);
	void finish();

}
