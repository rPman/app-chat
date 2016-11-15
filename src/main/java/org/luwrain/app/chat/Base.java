
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.EnvironmentEvent;
import org.luwrain.popups.EditListPopup;
import org.luwrain.popups.EditListPopupUtils;
import org.luwrain.popups.Popups;
import org.luwrain.app.chat.im.*;
import org.luwrain.controls.*;

class Base
{
    private final String[] TYPE_CHATS=new String[]{"Telegram","Jabber"};

    
    private Luwrain luwrain;
    private SectionsTreeModelSource treeModelSource;
    private CachedTreeModel treeModel;
    
    private TreeArea sectionsArea;
    private ChatArea chatArea;
    
    Vector<Account> accounts;
    
    public ChatArea getChatArea()
	{
		return chatArea;
	}

	public void setSectionsArea(TreeArea sectionsArea)
	{
		this.sectionsArea=sectionsArea;
	}

	public void setChatArea(ChatArea chatArea)
	{
		this.chatArea=chatArea;
	}

	boolean init(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	treeModelSource = new SectionsTreeModelSource(this);
	treeModel = new CachedTreeModel(treeModelSource);
	return true;
    }
    

     private void refreshSectionsTree()
     {
     }


     boolean onTreeAction(EnvironmentEvent event)
     {
     	System.out.println(event.getType().toString()+event.getCode());
 	return false;
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

    Object[] loadAccounts()
    {
    	if (accounts==null)
    	{
	    	accounts=new Vector<Account>();
	    	String[] dirs=luwrain.getRegistry().getDirectories(Settings.ACCOUNTS_PATH);
	    	if (dirs==null)
	    		return new String[]{};
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
    					TelegramAccount telegram=new TelegramAccount(luwrain, sett);
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
