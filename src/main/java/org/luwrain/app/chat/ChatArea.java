
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;

class ChatArea extends NavigationArea implements  EmbeddedEditLines
{
    protected Listener listener = null;
    protected final EmbeddedSingleLineEdit edit;
    protected final Vector<Line> lines = new Vector<Line>();

    protected String enteringPrefix = "";
    protected String enteringText = "";

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
	NullCheck.notNull(text, "text");
	lines.add(new Line(prefix, text));
	updateEditPos();
	environment.onAreaNewContent(this);
	if (getHotPointY() + 1 == lines.size())
	    setHotPointY(getHotPointY() + 1);
    }

    @Override public int getLineCount()
    {
	return lines.size() + 1;
    }

    @Override public String getLine(int index)
    {
	if (index < lines.size())
	    return lines.get(index).toString();
	if (index == lines.size())
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
	if (listener != null)
	    listener.onNewEnteredMessage(enteringText);
	enteringText = "";
	environment.onAreaNewContent(this);
	setHotPoint(enteringPrefix.length(), lines.size());
	return true;
    }

	protected void updateEditPos()
    {
	edit.setNewPos(enteringPrefix.length(), lines.size());
    }

interface Listener 
{
    void onNewEnteredMessage(String text);
}

    static protected class Line
    {
	final String prefix;
	final String text;

	Line(String prefix, String text)
	{
	    NullCheck.notNull(prefix, "prefix");
	    NullCheck.notNull(text, "text");
	    this.prefix = prefix;
	    this.text = text;
	}

	@Override public String toString()
	{
	    return prefix + text;
	}
    }
}
