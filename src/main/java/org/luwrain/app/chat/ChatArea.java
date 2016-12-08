
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
    protected final String enteringPrefix = ">";
    protected String enteringText = "";
    //    private Account currentAccount = null;
    private Contact contact = null;
    private Message[] messages = new Message[0];

    ChatArea(ControlEnvironment environment)
    {
	super(environment);
	edit = new EmbeddedSingleLineEdit(environment, this, this, 0, 0);
	updateEditPos();
    }

    void setEnteringPrefix(String prefix)
    {
	NullCheck.notNull(prefix, "prefix");
	//	this.enteringPrefix = prefix;
	//	updateEditPos();
	//	environment.onAreaNewContent(this);
    }

    @Override public int getLineCount()
    {
	return messages.length + 2;
    }

    @Override public String getLine(int index)
    {
	if (index < messages.length)
	    return messages[index].text;
	if (index == messages.length)
	    return enteringPrefix + enteringText;
	return "";
    }

    @Override public String getAreaName()
    {
	return "Беседа";
    }
	@Override protected boolean onArrowDown(KeyboardEvent event)
	{
		onChangedMessage();	
		return super.onArrowDown(event);
	}
	@Override protected boolean onArrowUp(KeyboardEvent event)
	{
		onChangedMessage();
		return super.onArrowUp(event);
	}
    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (contact != null && edit.isPosCovered(getHotPointX(), getHotPointY()))
	    if (event.isSpecial() && !event.isModified())
		switch(event.getSpecial())
		{
		case ENTER:
		    return onEnterInEdit();

		}
	if (contact != null && edit.isPosCovered(getHotPointX(), getHotPointY()) && edit.onKeyboardEvent(event))
	    return true;
	return super.onKeyboardEvent(event);
    }

    private boolean onChangedMessage()
	{
    	if (getHotPointY()>=messages.length)
    		return true;
    	if (messages[getHotPointY()].contact==null)
        	environment.playSound(Sounds.MAIN_MENU_ITEM);
    	else
    		environment.playSound(Sounds.DONE);

    		
		return true;
	}

	

	@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.getType() != EnvironmentEvent.Type.REGULAR)
	    return super.onEnvironmentEvent(event);
	if (contact != null && edit.isPosCovered(getHotPointX(), getHotPointY()) && edit.onEnvironmentEvent(event))
	    return true;
	switch(event.getCode())
	{
	case OK:
	    if (contact != null && edit.isPosCovered(getHotPointX(), getHotPointY()))
		return onEnterInEdit();
	    return false;
	default:
	return super.onEnvironmentEvent(event);
	}
    }

    @Override public boolean onAreaQuery(AreaQuery query)
    {
	NullCheck.notNull(query, "query");
	if (contact != null && edit.isPosCovered(getHotPointX(), getHotPointY()) && edit.onAreaQuery(query))
	    return true;
	return super.onAreaQuery(query);
    }

@Override public void setEmbeddedEditLine(int x, int y, String line)
    {
	NullCheck.notNull(line, "line");
	enteringText = line;
	environment.onAreaNewContent(this);
    }

@Override public String getEmbeddedEditLine(int x,int y)
    {
	return enteringText;
    }

    protected boolean onEnterInEdit()
    {
	if (contact == null || enteringText.isEmpty())
	    return false;
	contact.getAccount().sendMessage(enteringText, contact);
	enteringText = "";
	refresh();
	setHotPoint(enteringPrefix.length(), messages.length);
	return true;
    }

    protected void updateEditPos()
    {
	edit.setNewPos(enteringPrefix.length(), messages.length);
	environment.onAreaNewContent(this);
    }

    void setCurrentContact(Contact contact)
	{
	    NullCheck.notNull(contact, "contact");
		this.contact = contact;
		refresh();
		setHotPoint(enteringPrefix.length(), messages.length);
	}

    void refresh()
    {
    if (contact==null) return;
	messages = contact.getMessages();
	if (messages == null)
	    messages = new Message[0];
	updateEditPos();
	environment.onAreaNewContent(this);
    }

}
