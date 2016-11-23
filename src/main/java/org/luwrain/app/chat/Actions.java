
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
		     new Action("select-item", "Выбрать контакт для общения", new KeyboardEvent(KeyboardEvent.Special.ENTER)),
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

	public boolean onSelectItem(TreeArea treeArea,ChatArea chatArea)
	{
		
		if (treeArea.selected() instanceof Contact)
		{
			chatArea.selectContact((Contact)treeArea.selected());
		} else
		if (treeArea.selected() instanceof Account)
		{		
			((Account)treeArea.selected()).onConnect(new Runnable()
			{
				@Override public void run()
				{
					treeArea.refresh();
				}
			});
		}
		return false;
	}
}
