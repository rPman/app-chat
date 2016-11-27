
package org.luwrain.app.chat;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.app.chat.im.Account;
import org.luwrain.app.chat.im.Contact;
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
	    new Action("add-account", "Добавить новую учётную запись", new KeyboardEvent(KeyboardEvent.Special.INSERT)),
	    //	    new Action("select-item", "Выбрать контакт для общения", new KeyboardEvent(KeyboardEvent.Special.ENTER)),
	    new Action("add-contact", "Добавить контакт", new KeyboardEvent(KeyboardEvent.Special.F6)),
	};
    }

    boolean onAddAccount(TreeArea area)
    {
	NullCheck.notNull(area, "area");
	Log.debug("chat", "adding new account");
	final String jabber = "Jabber";
	final String telegram = "Telegram";
	final Object res = Popups.fixedList(luwrain, "Выберите тип новой учётной записи:", new String[]{jabber, telegram});
	if (res == null)
	    return true;
	if (res == jabber)
	    addAccountJabber();
	if (res == telegram)
	    addAccountTelegram();
	area.refresh();
	return true;
    }

    private boolean addAccountJabber()
    {
	return true;
    }

    public void onChekedContact(TreeArea area)
    {
	area.refresh();
    }

    private boolean addAccountTelegram()
    {
	String phone = Popups.simple(luwrain, "Добавление учетной записи", "Введите номер Вашего мобильного телефона:", "");
	phone=phone.trim();
	if(phone==null || phone.isEmpty())
	    return true;
	String firstname = Popups.simple(luwrain, "Добавление учетной записи", "Введите ваше имя, если Вам нужна регистрация в Telegram","");
	firstname=firstname.trim();
	String lastname="";
	if (!(firstname==null || firstname.isEmpty()))
	{
	    lastname = Popups.simple(luwrain, "Добавление учетной записи", "Введите ваше второе имя","");
	    lastname=lastname.trim();
	}
	//TODO проверить на наличин учетной записи с таком же телефоном
	final Registry registry = luwrain.getRegistry();
	registry.addDirectory(Settings.ACCOUNTS_PATH);
	final int id = Registry.nextFreeNum(luwrain.getRegistry(), Settings.ACCOUNTS_PATH);
	final String accountPath = Registry.join(Settings.ACCOUNTS_PATH, String.valueOf(id));
	Log.debug("chat", "adding registry directory " + accountPath);
	registry.addDirectory(accountPath);
	final Settings.Telegram sett = Settings.createTelegram(registry, accountPath);
	sett.setLastName(lastname);
	sett.setFirstName(firstname);
	sett.setPhone(phone);
	sett.setType("Telegram");
	return true;
    }

    boolean onTreeClick(TreeArea treeArea,ChatArea chatArea, Object obj)
    {
	if (obj instanceof Contact)
	{
	    final Contact contact = (Contact)treeArea.selected();
	    chatArea.setCurrentContact(contact.getAccount(), contact);
	    luwrain.setActiveArea(chatArea);
	    return true;
	}
	if (obj instanceof Account)
	{		
	    ((Account)treeArea.selected()).activate(()->treeArea.refresh());
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
