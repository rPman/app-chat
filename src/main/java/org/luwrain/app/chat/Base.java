
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.EnvironmentEvent;
import org.luwrain.popups.EditListPopup;
import org.luwrain.popups.EditListPopupUtils;
import org.luwrain.popups.Popups;
import org.luwrain.app.chat.im.ChatMenu;
import org.luwrain.controls.*;

class Base
{
    private final String[] TYPE_CHATS=new String[]{"Telegram","Jabber"};
    public static final String REGISTRY_PATH="/org/luwrain/app/chat";
    
    private Luwrain luwrain;
    private SectionsTreeModelSource treeModelSource;
    private CachedTreeModel treeModel;
    
    private TreeArea sectionsArea;
    private ChatArea chatArea;
    
    Vector<ChatMenu> accounts;
    
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
    
    boolean addAccounts()
 	{
 		EditListPopup popup=new EditListPopup(luwrain,
 			      new EditListPopupUtils.FixedModel(TYPE_CHATS),
 			      "Добавление учетной записи","Выберите тип новой учетной записи",TYPE_CHATS[0], Popups.DEFAULT_POPUP_FLAGS);
 		luwrain.popup(popup);
 		if(popup.closing.cancelled()) 
 			return true;
 		switch(popup.text())
 		{
 			case "Telegram": return addAccountTelegram();
 			case "Jabber": return addAccountJabber();
 		}
 		sectionsArea.refresh();
 		return true;
 	}

 	private boolean addAccountJabber()
 	{
 		return true;
 		
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
 		int id=Registry.nextFreeNum(luwrain.getRegistry(),REGISTRY_PATH);
 		String accauntpath=Registry.join(REGISTRY_PATH,String.valueOf(id));
 		luwrain.getRegistry().addDirectory(accauntpath);
 		ConfigAccessor.Telegram telegramConfig=RegistryProxy.create(luwrain.getRegistry(), accauntpath, ConfigAccessor.Telegram.class);
 		telegramConfig.setLastName(lastname);
 		telegramConfig.setFirstName(firstname);
 		telegramConfig.setPhone(phone);
 		telegramConfig.setType("Telegram");
 		return false;			
 	}

     private void refreshSectionsTree()
     {
     }

     Action[] getTreeActions()
     {
 	return new Action[0];
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

    Object[] getAccounts()
    {
    	if (accounts==null)
    	{
	    	accounts=new Vector<ChatMenu>();
	    	String[] dirs=luwrain.getRegistry().getDirectories(REGISTRY_PATH);
	    	if (dirs==null)
	    		return new String[]{};
	    	int id=0;
	    	for (String str : dirs)
	    	{
	    		String accauntpath=Registry.join(REGISTRY_PATH,str);
	    		ConfigAccessor.Type type=RegistryProxy.create(luwrain.getRegistry(), accauntpath, ConfigAccessor.Type.class);
	    		switch(type.getType(""))
	    		{
	    			case "Telegram":
    				{
    					ConfigAccessor.Telegram config=RegistryProxy.create(luwrain.getRegistry(), accauntpath, ConfigAccessor.Telegram.class);
    					TelegramAccauntImpl telegram=new TelegramAccauntImpl(luwrain,config);
    					accounts.add(telegram);
    					break;
    				}
	    			//case "Jabber": res.put("Jabber"+str,id++);break;
	    			default:
	    				break;
	    		}
	    	}
    	}
	return accounts.toArray(new TelegramAccauntImpl[accounts.size()]);
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
