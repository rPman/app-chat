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
import org.luwrain.cpanel.*;

class SettingsFactory implements org.luwrain.cpanel.Factory
{
    private final Luwrain luwrain;
    //    private final Strings strings;

    private SimpleElement chatElement = new SimpleElement(StandardElements.APPLICATIONS, this.getClass().getName());

    SettingsFactory(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	//	this.strings = new Strings();//FIXME:
    }

    @Override public Element[] getElements()
    {
	return new Element[]{chatElement};
    }

    @Override public Element[] getOnDemandElements(Element parent)
    {
	NullCheck.notNull(parent, "parent");
	if (!parent.equals(chatElement))
	    return new Element[0];
	final LinkedList<Element> res = new LinkedList<Element>();
	final Registry registry = luwrain.getRegistry();
	registry.addDirectory(Settings.ACCOUNTS_PATH);
	for(String p: registry.getDirectories(Settings.ACCOUNTS_PATH))
	{
	    final String path = Registry.join(Settings.ACCOUNTS_PATH, p);
	    final Settings.Base base = Settings.createBase(registry, path);
	    if (base.getType("").toLowerCase().trim().equals("telegram"))
	    {
		final Settings.Telegram telegram = Settings.createTelegram(registry, path);
		res.add(new SettingsTelegramElement(parent, telegram, telegram.getName("")));
	    }
	}
	return res.toArray(new Element[res.size()]);
    }

    @Override public Section createSection(Element el)
    {
	NullCheck.notNull(el, "el");
	if (el.equals(chatElement))
	    return new SimpleSection(chatElement, "Чат", null,
				     new Action[]{
					 new Action("add-chat-account", "Добавить учётную запись", new KeyboardEvent(KeyboardEvent.Special.INSERT)),
				     }, (controlPanel, event)->onActionEvent(controlPanel, event, ""));
	if (el instanceof SettingsTelegramElement)
	{
	    final SettingsTelegramElement accountEl = (SettingsTelegramElement)el;
	    return new SimpleSection(el, accountEl.getTitle(), (controlPanel)->SettingsTelegramForm.create(controlPanel, accountEl.getTelegram(), accountEl.getTitle()),
				     new Action[]{
					 new Action("add-chat-account", "Добавить учётную запись", new KeyboardEvent(KeyboardEvent.Special.INSERT)),
					 new Action("delete-chat-account", "Удалить учётную запись", new KeyboardEvent(KeyboardEvent.Special.DELETE)),
				     }, (controlPanel, event)->onActionEvent(controlPanel, event, accountEl.getTitle()));
	}
	return null;
    }

    private boolean onActionEvent(ControlPanel controlPanel, ActionEvent event, String accountName)
    {
	NullCheck.notNull(controlPanel, "controlPanel");
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "add-chat-account"))
	{
	    /*
	final String jabber = "Jabber";
	final String telegram = "Telegram";
	final Object res = Popups.fixedList(luwrain, "Выберите тип новой учётной записи:", new String[]{jabber, telegram});
	if (res == null)
	    return true;
	    */
	    final String name = Popups.simple(luwrain, "Новая учётная запись", "Имя новой учётной записи:", "");
	    if (name == null || name.trim().isEmpty())
	    return true;
	    final Registry registry = controlPanel.getCoreInterface().getRegistry();
	registry.addDirectory(Settings.ACCOUNTS_PATH);
	final int id = Registry.nextFreeNum(luwrain.getRegistry(), Settings.ACCOUNTS_PATH);
	final String accountPath = Registry.join(Settings.ACCOUNTS_PATH, String.valueOf(id));
	registry.addDirectory(accountPath);
	final Settings.Telegram sett = Settings.createTelegram(registry, accountPath);
	sett.setType("Telegram");
	sett.setName(name);
	controlPanel.refreshSectionsTree();
	return true;
	}

	/*
	if (ActionEvent.isAction(event, "delete-twitter-account"))
	{
	    NullCheck.notNull(accountName, "accountName");
	    /*
	    if (!Popups.confirmDefaultNo(luwrain, strings.deleteAccountPopupName(), strings.deleteAccountPopupText(accountName)))
		return true;
	    final Registry registry = controlPanel.getCoreInterface().getRegistry();
	    final String path = Registry.join(Settings.ACCOUNTS_PATH, accountName);
	    if (registry.deleteDirectory(path))
	    {
		luwrain.message(strings.accountDeletedSuccessfully(accountName), Luwrain.MESSAGE_OK);
		controlPanel.refreshSectionsTree();
	    }
	    return true;
	}
	return false;
	    */
	return false;
}
}
