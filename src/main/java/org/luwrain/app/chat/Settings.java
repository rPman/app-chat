
package org.luwrain.app.chat;

import org.luwrain.core.*;

interface Settings
{
    static final String ACCOUNTS_PATH = "/org/luwrain/app/chat/accounts";

    interface Base
    {
	String getType(String def);
    }

    interface Telegram
    {
	void setType(String val);
	String getFirstName(String def);
	String getLastName(String def);
	String getPhone(String def);
	String getAuthSmsCode(String def);
	String getAuthPhoneHash(String def);
	Boolean getAuthConnect(Boolean def);
	void setAuthConnect(Boolean val);
	void setFirstName(String val);
	void setLastName(String val);
	void setPhone(String val);
	void setAuthSmsCode(String val);
	void setAuthPhoneHash(String val);
    }

    interface Jabber
    {
	String getServer(String def);
	String getDomain(String def);
	String getPort(String def);
	String getLogin(String def);
	String getPassword(String def);
	Boolean getAuthConnect(Boolean def);
	void setAuthConnect(Boolean val);
	void setServer(String val);
	void setDomain(String val);
	void setPort(String val);
	void setLogin(String val);
	void setPassword(String val);
	void setType(String val);
    }

    static Telegram createTelegram(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	return RegistryProxy.create(registry, path, Telegram.class);
    }


//	static void AddTelegram(String phone, String firstname, name lastname);
}
