
package org.luwrain.app.chat;

import org.luwrain.core.*;
import org.luwrain.core.events.*;

import java.util.Date;
import java.util.Vector;

import org.luwrain.app.chat.im.Account;
import org.luwrain.app.chat.im.Contact;
import org.luwrain.app.chat.im.Message;
import org.luwrain.app.chat.im.MessageList;
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
		     new Action("add-contact", "Добавить контакт", new KeyboardEvent(KeyboardEvent.Special.F6)),
		     new Action("find-unread", "Поиск не прочитанных сообщений", new KeyboardEvent(KeyboardEvent.Special.F5))
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

	public boolean onAddContact(TreeArea treeArea,ChatArea chatArea)
	{
		Object obj=treeArea.selected();
		Account account;
		if (obj instanceof Account)
			account=(Account)obj;
		else
			if (obj instanceof Contact)
				account=((Contact)obj).getAccount();
			else
				return false;
//		if (contact==null) return;
		account.askCreateContact(new Runnable()
		{
			@Override public void run()
			{
				treeArea.refresh();
				luwrain.onAreaNewContent(treeArea);				
			}
		});
		return false;
	}

	public boolean onFindUnreadMessage(TreeArea treeArea,ChatArea chatArea,Base base)
	{
//		Object obj=chatArea.get);
		Date date=null;
		Account aread;
		Contact cread=null;
		for (Account a:base.accounts)
		{
			Contact[] contacts=a.getContacts();
			for (Contact c:contacts)
			{
				MessageList ml=c.getMessages();
				if (ml.unreadCount()!=0)
				{
					Vector<Message> m=ml.lastMessages();
					int count=ml.unreadCount();
					if (date==null)
					{
						date=ml.lastMessages().get(m.size()-count).getDate();
						aread=a;
						cread=c;
					}
					else
					if (date.before(ml.lastMessages().get(m.size()-count).getDate()))
					{
						date=ml.lastMessages().get(m.size()-count).getDate();
						aread=a;
						cread=c;
					}
				}
			}		
		}
		if (cread!=null)
		{
			treeArea.selectObject(cread);
			onSelectItem(treeArea,chatArea);
			base.gotoSecondArea();
		}
		return false;
	}
}
