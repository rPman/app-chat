
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
	addEdit("auth-sms-code", "Проверочный код:", telegram.getAuthSmsCode(""));
	addEdit("auth-phone-hash", "Шифр сервера:", telegram.getAuthPhoneHash(""));
    }

    @Override public boolean saveSectionData()
    {
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
