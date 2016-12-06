
package org.luwrain.app.chat;

import org.luwrain.core.*;
import org.luwrain.cpanel.*;

class SettingsTelegramElement implements Element
{
    private final Element parent;
    private final Settings.Telegram telegram;
    private String title;

    SettingsTelegramElement(Element parent, Settings.Telegram telegram,
String title)
    {
	NullCheck.notNull(parent, "parent");
	NullCheck.notNull(telegram, "telegram");
	NullCheck.notEmpty(title, "title");
	this.parent = parent;
	this.telegram = telegram;
	this.title = title;
    }

    @Override public Element getParentElement()
    {
	return parent;
    }

    @Override public boolean equals(Object o)
    {
	if (o == null || !(o instanceof SettingsTelegramElement))
	    return false;
	return title.equals(((SettingsTelegramElement)o).title);
    }

    @Override public int hashCode()
    {
	return title.hashCode();
    }

    String getTitle()
    {
	return title;
    }

    Settings.Telegram getTelegram()
    {
	return telegram;
    }
}
