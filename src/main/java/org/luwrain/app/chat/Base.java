
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.EnvironmentEvent;
import org.luwrain.app.chat.im.*;
import org.luwrain.controls.*;

class Base
{
    private Luwrain luwrain;
    private TreeModelSource treeModelSource;
    private CachedTreeModel treeModel;
    private Listener listener;
    private Account[] accounts;

    boolean init(Luwrain luwrain, Listener listener)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(listener, "listener");
	this.luwrain = luwrain;
	this.listener = listener;
	this.accounts = loadAccounts();
	treeModelSource = new TreeModelSource(this, "Учётные записи");//FIXME:strings
	treeModel = new CachedTreeModel(treeModelSource);
	return true;
    }

    Account[] getAccounts()
    {
	return accounts;
    }

    private Account[] loadAccounts()
    {
	Log.debug("chat", "loading accounts");
	final LinkedList<Account> res = new LinkedList<Account>();
	final Registry registry = luwrain.getRegistry();
	registry.addDirectory(Settings.ACCOUNTS_PATH);
	for (String str : registry.getDirectories(Settings.ACCOUNTS_PATH))
	{
	    String accountPath = Registry.join(Settings.ACCOUNTS_PATH, str);
	    final Settings.Base type=RegistryProxy.create(luwrain.getRegistry(), accountPath, Settings.Base.class);
	    switch(type.getType("").trim().toLowerCase())
	    {
	    case "telegram":
		{
		    final Settings.Telegram sett = Settings.createTelegram(luwrain.getRegistry(), accountPath );
		    res.add(new TelegramAccount(luwrain, sett, listener));
		}
		break;
	    default:
		break;
	    }
	}
	Log.debug("chat", "loaded " + res.size() + " accounts");
	return res.toArray(new TelegramAccount[res.size()]);
    }

    TreeArea.Model getTreeModel()
    {
	return treeModel;
    }

    static String getPhoneDesignation(String str)
    {
	if (str.length() < 5)
	    return str;
	return str.substring(str.length() - 4);
    }
}
