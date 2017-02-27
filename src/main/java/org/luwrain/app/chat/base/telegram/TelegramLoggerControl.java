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

package org.luwrain.app.chat.base.telegram;

public class TelegramLoggerControl
{
	static
    {
		System.out.println("disable logs");
		org.telegram.mtproto.log.Logger.registerInterface(new org.telegram.mtproto.log.LogInterface() {
			public void w(String tag, String message) {}
			public void d(String tag, String message) {}
			public void e(String tag, String message) {            }
			public void e(String tag, Throwable t) {}
		    });
		org.telegram.api.engine.Logger.registerInterface(new org.telegram.api.engine.LoggerInterface() {
			public void w(String tag, String message) {}
			public void d(String tag, String message) {}
			public void e(String tag, String message) {}
			public void e(String tag, Throwable t) {}
		    });
    }
	// TODO: replace org.telegram.bot.services.BotLogger to disable file.log creation

}
