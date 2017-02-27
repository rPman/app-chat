/*
   Copyright 2016 Ekaterina Koryakina <ekaterina_kor@mail.ru>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

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
