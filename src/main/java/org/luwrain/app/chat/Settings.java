
package org.luwrain.app.chat;

import org.luwrain.core.*;

public interface Settings
{
    static final String ACCOUNTS_PATH = "/org/luwrain/app/chat/accounts";

    interface Base
    {
	String getType(String def);
    }

    public interface Telegram extends Base
    {
	String getName(String defValue);
	void setName(String value);
	void setType(String val);
	String getFirstName(String def);
	String getLastName(String def);
	String getPhone(String def);
	String getAuthSmsCode(String def);
	String getAuthPhoneHash(String def);
	boolean getSmsVoice(boolean def);
	//	boolean getAutoConnect(boolean def);
	//	void setAutoConnect(boolean val);
	void setFirstName(String val);
	void setLastName(String val);
	void setPhone(String val);
	void setAuthSmsCode(String val);
	void setAuthPhoneHash(String val);
	void setSmsVoice(boolean val);
    }

    interface Jabber
    {
	String getServer(String def);
	String getDomain(String def);
	String getPort(String def);
	String getLogin(String def);
	String getPassword(String def);
	boolean getAutoConnect(boolean def);
	void setAutoConnect(boolean val);
	void setServer(String val);
	void setDomain(String val);
	void setPort(String val);
	void setLogin(String val);
	void setPassword(String val);
	void setType(String val);
    }

    static Base createBase(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	return RegistryProxy.create(registry, path, Base.class);
    }
    static Telegram createTelegram(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	return RegistryProxy.create(registry, path, Telegram.class);
    }


//	static void AddTelegram(String phone, String firstname, name lastname);
}
