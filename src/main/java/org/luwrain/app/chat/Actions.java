
package org.luwrain.app.chat;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.app.chat.base.Account;
import org.luwrain.app.chat.base.Contact;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

class Actions
{
    private final Luwrain luwrain;

    Actions(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    Action[] getTreeActions()
    {
	return new Action[]{
	    new Action("add-contact", "Добавить контакт", new KeyboardEvent(KeyboardEvent.Special.INSERT)),
	};
    }

    public void onChekedContact(TreeArea area)
    {
	area.refresh();
    }

    boolean onTreeClick(TreeArea treeArea,ChatArea chatArea, Object obj)
    {
	if (obj instanceof Contact)
	{
	    final Contact contact = (Contact)treeArea.selected();
	    chatArea.setCurrentContact(contact);
	    luwrain.setActiveArea(chatArea);
	    return true;
	}
	if (obj instanceof Account)
	{		
	    ((Account)treeArea.selected()).activate();
	    return true;
	}
	return false;
    }

    boolean onAddContact(TreeArea treeArea,ChatArea chatArea)
    {
	final Object obj=treeArea.selected();
	if (obj == null)
	    return false;
	final Account account;
	if (obj instanceof Account)
	    account = (Account)obj; else
	    if (obj instanceof Contact)
		account = ((Contact)obj).getAccount(); else
		return false;
		final String phone = Popups.simple(luwrain, "Добавление контакта", "Введите номер мобильного телефона:", "");
		if (phone == null || phone.trim().isEmpty())
		    return true;

		final String firstName = Popups.simple(luwrain, "Добавление контакта", "Введите имя:","");
		if (firstName == null || firstName.trim().isEmpty())
		    return true;
		    final String lastName = Popups.simple(luwrain, "Добавление контакта", "Введите второе имя:","");
		    if (lastName == null || lastName.trim().isEmpty())
			return true;
		    account.addContact(phone, firstName, lastName, ()->treeArea.refresh());
	return true;
    }
}
