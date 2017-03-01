/*
   Copyright 2016 Ekaterina Koryakina <ekaterina_kor@mail.ru>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.popups.Popups;
import org.luwrain.controls.*;
import org.luwrain.app.chat.base.*;

class ChatApp implements Application, MonoApp, Listener
{
    private Luwrain luwrain;
    private final Base base = new Base();
    private Actions actions;
    private Strings strings;

    private ListArea contactsArea;
    private ChatArea chatArea;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, this))
	    return false;
	actions = new Actions(luwrain);
	createArea();
	open();
	return true;
    }

    private void createArea()
    {
	final ListArea.Params contactsParams = new ListArea.Params();
	contactsParams.environment = new DefaultControlEnvironment(luwrain);
	contactsParams.model = base.getContactsModel();
	contactsParams.appearance = new ListUtils.DoubleLevelAppearance(contactsParams.environment){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    return (item instanceof Account);
		}
	    };

	contactsParams.name = strings.sectionsAreaName();

	contactsArea = new ListArea(contactsParams){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch (event.getSpecial())
			{
			case TAB:
			    return gotoChatArea();
			}
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch (event.getCode())
		    {
		    case ACTION:
			return onTreeAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actions.getContactsActions();
		}
	};

	chatArea = new ChatArea(luwrain, "Беседа") {

	    @Override public boolean onKeyboardEvent(KeyboardEvent event)
	    {
		NullCheck.notNull(event, "event");
		if (event.isSpecial() && !event.isModified())
		    switch(event.getSpecial())
		{
		case TAB:
		    return gotoTreeArea();
		}
		return super.onKeyboardEvent(event);
	    }

	    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
	    {
		NullCheck.notNull(event, "event");
		if (event.getType() != EnvironmentEvent.Type.REGULAR)
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
	};

contactsArea.setListClickHandler((area, index, obj)->actions.onContactsClick(area, chatArea, obj));
//chatArea.setEnteringPrefix("proba>");
    

    }

    private boolean onTreeAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "add-contact"))
	    return actions.onAddContact(contactsArea, chatArea);
	return false;
    }

    @Override public void refreshTree()
    {
	contactsArea.refresh();
    }

    @Override public void refreshChatArea()
    {
	chatArea.updateMessages();
    }

    private void open()
    {
	for(Account a: base.getAccounts())
	    a.open();
    }

    private boolean gotoTreeArea()
    {
	luwrain.setActiveArea(contactsArea);
	return true;
    }

    private boolean gotoChatArea()
    {
	luwrain.setActiveArea(chatArea);
	return true;
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_RIGHT, contactsArea, chatArea);
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
	for(Account a: base.getAccounts())
	    a.close();
	luwrain.closeApp();
    }
}
