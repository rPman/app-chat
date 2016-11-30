
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.app.chat.im.Account;
import org.luwrain.app.chat.im.Contact;
import org.luwrain.app.chat.im.Message;
import org.luwrain.controls.*;

class ChatArea extends NavigationArea implements  EmbeddedEditLines
{
    protected final EmbeddedSingleLineEdit edit;
    protected String enteringPrefix = "";
    protected String enteringText = "";
    private Account currentAccount = null;
    private Contact contact = null;

    ChatArea(ControlEnvironment environment)
    {
	super(environment);
	edit = new EmbeddedSingleLineEdit(environment, this, this, 0, 0);
    }

    void setEnteringPrefix(String prefix)
    {
	NullCheck.notNull(prefix, "prefix");
	this.enteringPrefix = prefix;
	updateEditPos();
	environment.onAreaNewContent(this);
    }

    @Override public int getLineCount()
    {
    	if (contact == null) 
	    return 2;
    	if (contact.getMessages() == null) 
return 2;
	return contact.getMessages().length + 2;
    }

    @Override public String getLine(int index)
    {
    	if (contact==null) 
return "";
    	if (contact.getMessages()==null) 
return "";
	if (index < contact.getMessages().length)
	    return contact.getMessages()[index].getMessage();
	if (index == contact.getMessages().length)
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
	currentAccount.sendNewMessage(enteringText, contact);
	enteringText = "";
	environment.onAreaNewContent(this);
	setHotPoint(enteringPrefix.length(), contact.getMessages().length);
	return true;
    }

	protected void updateEditPos()
    {
		if (contact == null)
return;
		if (contact.getMessages() == null) 
return;
		Log.debug("chat", "setting chat area edit at " + enteringPrefix.length() + "," + contact.getMessages().length);
	edit.setNewPos(enteringPrefix.length(), contact.getMessages().length);
    }

void setCurrentContact(Account account, Contact contact)
	{
	    NullCheck.notNull(account, "account");
	    NullCheck.notNull(contact, "contact");
	    Log.debug("chat", "setting new current contact to chat area:" + contact);
	    this.currentAccount = account;
		this.contact = contact;
		updateEditPos();
		environment.onAreaNewContent(this);
	}

	public void addContact(Account account)
	{
		ChatArea that=this;
//		if (contact==null) return;
/*
account.askCreateContact(new Runnable()
		{
			@Override public void run()
			{
				environment.onAreaNewContent(that);				
			}
		});
*/
	}
}
