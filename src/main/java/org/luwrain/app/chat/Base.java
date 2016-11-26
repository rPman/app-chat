
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

    private TreeArea sectionsArea;
    private ChatArea chatArea;

    private final Vector<Account> accounts = new Vector<Account>();

    ChatArea getChatArea()
	{
		return chatArea;
	}

    void setSectionsArea(TreeArea sectionsArea)
	{
		this.sectionsArea=sectionsArea;
	}

void setChatArea(ChatArea chatArea)
	{
		this.chatArea=chatArea;
	}

	boolean init(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	treeModelSource = new TreeModelSource(this, "Учётные записи");//FIXME:strings
	treeModel = new CachedTreeModel(treeModelSource);
	return true;
    }

     boolean gotoSectionsArea()
     {
 	luwrain.setActiveArea(sectionsArea);
 	return true;
     }

     boolean gotoSecondArea()
     {
 	luwrain.setActiveArea(chatArea);
 	return true;
     }

    Account[] loadAccounts()
    {
    	if (accounts==null)
    	{
	    //	    	accounts=new Vector<Account>();
	    	String[] dirs=luwrain.getRegistry().getDirectories(Settings.ACCOUNTS_PATH);
	    	if (dirs==null)
	    		return new Account[]{};
	    	int id=0;
	    	for (String str : dirs)
	    	{
	    		String accountPath = Registry.join(Settings.ACCOUNTS_PATH, str);
			Settings.Base type=RegistryProxy.create(luwrain.getRegistry(), accountPath, Settings.Base.class);
	    		switch(type.getType(""))
	    		{
	    			case "Telegram":
    				{
final Settings.Telegram sett = Settings.createTelegram(luwrain.getRegistry(), accountPath );
    					TelegramAccount telegram=new TelegramAccount(luwrain, sett,new UIEvent()
						{
							
							@Override public void onNewMessage()
							{
								luwrain.onAreaNewContent(chatArea);								
							}

							@Override public void onUnknownContactReciveMessage(String message)
							{
								luwrain.message("Неизвестный контакт: "+message);
								luwrain.onAreaNewContent(sectionsArea);
								
							}
						});
    					accounts.add(telegram);
    					break;
    				}
	    			//case "Jabber": res.put("Jabber"+str,id++);break;
	    			default:
	    				break;
	    		}
	    	}
    	}
	return accounts.toArray(new TelegramAccount[accounts.size()]);
    }

    public TreeArea getSectionsArea()
	{
		return sectionsArea;
	}

	TreeArea.Model getTreeModel()
    {
	return treeModel;
    }
}
