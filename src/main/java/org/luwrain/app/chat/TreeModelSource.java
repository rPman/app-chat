
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class TreeModelSource implements CachedTreeModelSource
{
    private final Base base;
    private final String root;

    TreeModelSource(Base base, String root)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notEmpty(root, "root");
	this.base = base;
	this.root = root;
    }

    @Override public Object getRoot()
    {
	return root;
    }

    @Override public Object[] getChildObjs(Object obj)
    {
	NullCheck.notNull(obj, "obj");
	if (obj == root)
	    return base.getAccounts();
	if (obj instanceof TelegramAccount) 
	    return ((TelegramAccount)obj).getContacts();
	return new Object[0];
    }
}
