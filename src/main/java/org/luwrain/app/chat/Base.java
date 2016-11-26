
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
    private TelegramAccountListener telegramAccountListener;

    boolean init(Luwrain luwrain, TelegramAccountListener telegramAccountListener)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(telegramAccountListener, "telegramAccountListener");
	this.luwrain = luwrain;
	this.telegramAccountListener = telegramAccountListener;
	treeModelSource = new TreeModelSource(this, "Учётные записи");//FIXME:strings
	treeModel = new CachedTreeModel(treeModelSource);
	return true;
    }

    Account[] loadAccounts()
    {
	final LinkedList<Account> res = new LinkedList<Account>();
	final Registry registry = luwrain.getRegistry();
	registry.addDirectory(Settings.ACCOUNTS_PATH);
	for (String str : registry.getDirectories(Settings.ACCOUNTS_PATH))
	{
	    String accountPath = Registry.join(Settings.ACCOUNTS_PATH, str);
	    final Settings.Base type=RegistryProxy.create(luwrain.getRegistry(), accountPath, Settings.Base.class);
	    switch(type.getType("").trim().toLowerCase())
	    {
	    case "Telegram":
		{
		    final Settings.Telegram sett = Settings.createTelegram(luwrain.getRegistry(), accountPath );
		    res.add(new TelegramAccount(luwrain, sett, telegramAccountListener));
		}
		break;
	    default:
		break;
	    }
	}
	return res.toArray(new TelegramAccount[res.size()]);
    }

    TreeArea.Model getTreeModel()
    {
	return treeModel;
    }
}
