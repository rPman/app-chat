
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;

public class ConsoleArea extends NavigationArea implements  EmbeddedEditLines
{
public interface Appearance 
{
    void announceItem(Object item);
    String getTextAppearance(Object item);
}

public interface ClickHandler
{
    boolean onEnteredText(String text);
}

static public class Params
{
    public ControlEnvironment environment;
    public String areaName = "";
    public Appearance appearance;
    public ClickHandler clickHandler;
}

    protected final ControlEnvironment environment;
    protected String areaName = "";
    protected final Appearance appearance;
    protected ClickHandler clickHandler = null;
    protected final EmbeddedSingleLineEdit edit;

    protected Object[] items = new Object[0];
    protected final String enteringPrefix = ">";
    protected String enteringText = "";

public ConsoleArea(Params params)
    {
	super(params.environment);
	NullCheck.notNull(params, "params");
	NullCheck.notNull(params.appearance, "params.appearance");
	NullCheck.notNull(params.areaName, "params.areaName");
	this.environment = params.environment;
	this.appearance = params.appearance;
	this.clickHandler = params.clickHandler;
	this.areaName = params.areaName;
	edit = new EmbeddedSingleLineEdit(environment, this, this, 0, 0);
	updateEditPos();
    }

    public void setItems(Object[] items)
    {
	NullCheck.notNull(items, "items");
	this.items = items;
	refresh();
    }

    void setEnteringPrefix(String prefix)
    {
	NullCheck.notNull(prefix, "prefix");
	//	this.enteringPrefix = prefix;
	refresh();
    }

    @Override public int getLineCount()
    {
	return items.length + 2;
    }

    @Override public String getLine(int index)
    {
	if (index < items.length)
	    return appearance.getTextAppearance(items[index]);
	if (index == items.length)
	    return enteringPrefix + enteringText;
	return "";
    }

    @Override public String getAreaName()
    {
	return areaName;
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (edit.isPosCovered(getHotPointX(), getHotPointY()))
	{
	    if (event.isSpecial() && !event.isModified())
		switch(event.getSpecial())
		{
		case ENTER:
		    return onEnterInEdit();
		}
	if (edit.onKeyboardEvent(event))
	    return true;
	}
	return super.onKeyboardEvent(event);
    }

    /*
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
    */

	@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.getType() != EnvironmentEvent.Type.REGULAR)
	    return super.onEnvironmentEvent(event);
	if (edit.isPosCovered(getHotPointX(), getHotPointY()) && edit.onEnvironmentEvent(event))
	    return true;
	switch(event.getCode())
	{
	case OK:
	    if (edit.isPosCovered(getHotPointX(), getHotPointY()))
		return onEnterInEdit();
	    return false;
	default:
	return super.onEnvironmentEvent(event);
	}
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
	environment.onAreaNewContent(this);
    }

@Override public String getEmbeddedEditLine(int x,int y)
    {
	return enteringText;
    }

    protected boolean onEnterInEdit()
    {
	if (clickHandler == null || enteringText.isEmpty())
	    return false;
	if (clickHandler.onEnteredText(enteringText))
	    return false;
	enteringText = "";
	refresh();
	setHotPoint(enteringPrefix.length(), items.length);
	return true;
    }

    protected void updateEditPos()
    {
	edit.setNewPos(enteringPrefix.length(), items.length);
	environment.onAreaNewContent(this);
    }

    void refresh()
    {
	updateEditPos();
	environment.onAreaNewContent(this);
    }

    @Override public void announceLine(int index, String line)
    {
	if (index < items.length)
	{
	    appearance.announceItem(items[index]);
	    return;
	}
	if (line == null || line.isEmpty())
	{
	    environment.hint(Hints.EMPTY_LINE);
	    return;
	}
environment.silence();
environment.say(line );
    }
}
