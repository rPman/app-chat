
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class SectionsTreeModelSource implements CachedTreeModelSource
{
    private final Base base;
    private final String root = "root";

    SectionsTreeModelSource(Base base)
    {
	NullCheck.notNull(base, "base");
	this.base = base;
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

	return new Object[0];
    }
}
