
package org.luwrain.app.chat;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class Base
{
    private Luwrain luwrain;
    private SectionsTreeModelSource treeModelSource;
    private CachedTreeModel treeModel;

    boolean init(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	treeModelSource = new SectionsTreeModelSource(this);
	treeModel = new CachedTreeModel(treeModelSource);
	return true;
    }

    Object[] getAccounts()
    {
	return new String[]{"1", "2", "3"};
    }

    TreeArea.Model getTreeModel()
    {
	return treeModel;
    }
}
