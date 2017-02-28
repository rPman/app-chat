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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.cpanel.*;

class SettingsTelegramForm extends FormArea implements SectionArea
{
    private final ControlPanel controlPanel;
    private final Luwrain luwrain;
    //    private Strings strings;
    private final Settings.Telegram telegram;

    SettingsTelegramForm(ControlPanel controlPanel, Settings.Telegram telegram, String title)
    {
	super(new DefaultControlEnvironment(controlPanel.getCoreInterface()), title);
	NullCheck.notNull(controlPanel, "controlPanel");
	NullCheck.notNull(telegram, "telegram");
	this.controlPanel = controlPanel;
	this .luwrain = controlPanel.getCoreInterface();
	this.telegram = telegram;
	fillForm();
    }

    private void fillForm()
    {
	addStatic("type", "Тип:Телеграм");
	addEdit("name", "Имя учётной записи:", telegram.getName(""));
	addEdit("phone", "Номер телефона:", telegram.getPhone(""));
	addEdit("first-name", "Имя владельца:", telegram.getFirstName(""));
	addEdit("last-name", "Фамилия владельца:", telegram.getLastName(""));
    }

    @Override public boolean saveSectionData()
    {
telegram.setName(getEnteredText("name"));
telegram.setPhone(getEnteredText("phone"));
telegram.setFirstName(getEnteredText("first-name"));
telegram.setLastName(getEnteredText("last-name"));
	return true;
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (controlPanel.onKeyboardEvent(event))
	    return true;
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (controlPanel.onEnvironmentEvent(event))
	    return true;
	return super.onEnvironmentEvent(event);
    }

    static SettingsTelegramForm create(ControlPanel controlPanel, Settings.Telegram telegram, String title)
    {
	NullCheck.notNull(controlPanel, "controlPanel");
	NullCheck.notNull(telegram, "telegram");
	NullCheck.notNull(title, "title");
	return new SettingsTelegramForm(controlPanel, telegram, title);
    }
}
