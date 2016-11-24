
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.app.chat.im.Account;
import org.luwrain.app.chat.im.Contact;
import org.luwrain.app.chat.im.Message;
import org.luwrain.app.chat.im.MessageList;
import org.luwrain.controls.*;

class ChatArea extends NavigationArea implements  EmbeddedEditLines
{
    protected Listener listener = null;
    protected final EmbeddedSingleLineEdit edit;
    protected String enteringPrefix = "";
    protected String enteringText = "";
	private Contact contact=null;

    ChatArea(ControlEnvironment environment)
    {
	super(environment);
	edit = new EmbeddedSingleLineEdit(environment, this, this, 0, 0);
    }

    void setListener(Listener listener)
    {
	this.listener = listener;
    }

    void setEnteringPrefix(String prefix)
    {
	NullCheck.notNull(prefix, "prefix");
	this.enteringPrefix = prefix;
	updateEditPos();
	environment.onAreaNewContent(this);
    }

    void addLine(String prefix, String text)
    {
	NullCheck.notNull(prefix, "prefix");
	NullCheck.notNull(text, "text");if (contact==null) return;
	if (contact.getMessages()==null) return;
	if (contact.getMessages().lastMessages()==null) return;
	contact.getMessages().lastMessages().add(contact.getAccount().sendNewMessage(text,contact));
	updateEditPos();
	environment.onAreaNewContent(this);
	if (getHotPointY()  == contact.getMessages().lastMessages().size())
	    setHotPointY(getHotPointY() + 1);
    }

    @Override public int getLineCount()
    {
    	if (contact==null) return 1;
    	if (contact.getMessages()==null) return 1;
	return 1+contact.getMessages().lastMessages().size();
    }

    @Override public String getLine(int index)
    {
    	if (contact==null) return "";
    	if (contact.getMessages()==null) return "";
    	if (contact.getMessages().lastMessages()==null) return "";
	if (index < contact.getMessages().lastMessages().size())
	    return contact.getMessages().lastMessages().get(index).getMessage();
	if (index == contact.getMessages().lastMessages().size())
	    return enteringPrefix + enteringText;
	return "";
    }

    @Override public String getAreaName()
    {
	return "chat";
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (edit.isPosCovered(getHotPointX(), getHotPointY()))
	    if (event.isSpecial() && !event.isModified())
		switch(event.getSpecial())
		{
		case ENTER:
		    return onEnterInEdit();
		}
	if (edit.isPosCovered(getHotPointX(), getHotPointY()) && edit.onKeyboardEvent(event))
	    return true;
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (edit.isPosCovered(getHotPointX(), getHotPointY()) && edit.onEnvironmentEvent(event))
	    return true;
	return super.onEnvironmentEvent(event);
    }

    @Override public boolean onAreaQuery(AreaQuery query)
    {
	NullCheck.notNull(query, "query");
	if (edit.isPosCovered(getHotPointX(), getHotPointY()) && edit.onAreaQuery(query))
	    return true;
	return super.onAreaQuery(query);
    }

@Override public void setEmbeddedEditLine(int x, int y, String line)
    {
	NullCheck.notNull(line, "line");
	enteringText = line;
    }

@Override public String getEmbeddedEditLine(int x,int y)
    {
	return enteringText;
    }

    protected boolean onEnterInEdit()
    {
	if (enteringText.isEmpty())
	    return false;
	if (contact==null) return false;
	if (contact.getMessages()==null) return false;
	if (listener != null)
	    listener.onNewEnteredMessage(enteringText);
	enteringText = "";
	environment.onAreaNewContent(this);
	setHotPoint(enteringPrefix.length(), contact.getMessages().lastMessages().size());
	return true;
    }

	protected void updateEditPos()
    {
		if (contact==null) return;
		if (contact.getMessages()==null) return;
	edit.setNewPos(enteringPrefix.length(), contact.getMessages().lastMessages().size());
    }

	interface Listener 
	{
	    void onNewEnteredMessage(String text);
	}


	public void selectContact(Contact selected)
	{
		this.contact=selected;
		environment.onAreaNewContent(this);
	}

	public void addContact(Account account)
	{
		ChatArea that=this;
//		if (contact==null) return;
		account.askCreateContact(new Runnable()
		{
			@Override public void run()
			{
				environment.onAreaNewContent(that);				
			}
		});
	}

}
