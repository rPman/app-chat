
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;

class ChatArea extends NavigationArea implements  EmbeddedEditLines
{
    protected final EmbeddedSingleLineEdit edit;
    protected final Vector<Line> lines = new Vector<Line>();

    protected String enteringPrefix = "";
    protected String enteringText = "";

    ChatArea(ControlEnvironment environment)
    {
	super(environment);
	edit = new EmbeddedSingleLineEdit(environment, this, this, 0, 0);
    }

    @Override public int getLineCount()
    {
	return 1;
    }

    @Override public String getLine(int index)
    {
	return "";
    }

    @Override public String getAreaName()
    {
	return "";
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
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
