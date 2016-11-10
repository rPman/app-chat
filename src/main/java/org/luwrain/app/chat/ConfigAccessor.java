package org.luwrain.app.chat;

public class ConfigAccessor
{
	public interface Type
	{
		String getType(String def);
	}
	
	public interface Telegram
	{
		void setType(String val);
		String getFirstName(String def);
		String getLastName(String def);
		String getPhone(String def);
		String getAuthSmsCode(String def);
		String getAuthPhoneHash(String def);
		void setFirstName(String val);
		void setLastName(String val);
		void setPhone(String val);
		void setAuthSmsCode(String val);
		void setAuthPhoneHash(String val);
		
	}
	public interface Jabber
	{
		String getServer(String def);
		String getDomain(String def);
		String getPort(String def);
		String getLogin(String def);
		String getPassword(String def);
		void setServer(String val);
		void setDomain(String val);
		void setPort(String val);
		void setLogin(String val);
		void setPassword(String val);
		void setType(String val);

	}
//	static void AddTelegram(String phone, String firstname, name lastname);
}
