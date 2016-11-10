
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.Settings.I18n;
import org.luwrain.core.events.*;
import org.luwrain.popups.EditListPopup;
import org.luwrain.popups.EditListPopupUtils;
import org.luwrain.popups.Popups;
import org.luwrain.controls.*;

class ChatApp implements Application, MonoApp
{

    private Luwrain luwrain;
    private final Base base = new Base();
    public enum TypeChats{Telegram,Jabber};
    private Strings strings;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain))
	    return false;
	createArea();
	return true;
    }

    private void createArea()
    {
	final TreeArea.Params treeParams = new TreeArea.Params();
	treeParams.environment = new DefaultControlEnvironment(luwrain);
	treeParams.model = base.getTreeModel();
	treeParams.name = strings.sectionsAreaName();
	//treeParams.clickHandler = (area, obj)->openSection(obj);

	base.setSectionsArea(new TreeArea(treeParams){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch (event.getSpecial())
			{
			case TAB:
			    return base.gotoSecondArea();
			case INSERT:
				return base.addAccounts();
			}
		    return super.onKeyboardEvent(event);
		}

		
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch (event.getCode())
		    {
		    case ACTION:
			return base.onTreeAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return base.getTreeActions();
		}
		
		@Override public void onClick(Object obj)
		{
			((ChatMenu)obj).onClick();
		}

	    });
    

    base.setChatArea(new ChatArea(new DefaultControlEnvironment(luwrain)) {

	    @Override public boolean onKeyboardEvent(KeyboardEvent event)
	    {
		NullCheck.notNull(event, "event");
		if (event.isSpecial() && !event.isModified())
		    switch(event.getSpecial())
		{
		case TAB:
		    return base.gotoSectionsArea();
		}
		return super.onKeyboardEvent(event);
	    }

	    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
	    {
		NullCheck.notNull(event, "event");
		if (event.getType() == EnvironmentEvent.Type.REGULAR)
		    return super.onEnvironmentEvent(event);
		switch(event.getCode())
		{
		case CLOSE:
		    closeApp();
		    return true;
		default:
		    return super.onEnvironmentEvent(event);
		}
	    }
	});

		base.getChatArea().setEnteringPrefix("proba>");
		base.getChatArea().setListener((text)->base.getChatArea().addLine("entered>", text));

	}

 

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_RIGHT, base.getSectionsArea(), base.getChatArea());
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

private void closeApp()
    {
	luwrain.closeApp();
    }
}
