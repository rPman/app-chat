
package org.luwrain.app.chat;

import java.util.*;
import org.luwrain.core.*;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{
	    new Command(){
		@Override public String getName()
		{
		    return "chat";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("chat");
		}
	    }};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	return new Shortcut[]{
	    new Shortcut() {
		@Override public String getName()
		{
		    return "chat";
		}
		@Override public Application[] prepareApp(String[] args)
		{
			return new Application[]{new ChatApp()};
		}
	    }};
    }
}
