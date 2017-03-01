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

package org.luwrain.app.chat.base;

public interface Events
{
    /**
     * Событие вызывается при возникновении ошибки, 
     * после чего требуется повторное выполнение действия его вызвавшего
     * @param message описание ошибки
     */
    void onError(String message);

    void onWarning(String message);

    /**
     * Событие вызывается когда сервер требует кода двухфакторной авторизации
     * @param message необязательное сообщение
     */
    String askTwoPassAuthCode();

    /**
     * Событие вызывается при окончании успешной авторизации
     */


/** вызывается перед добавлением контактов в список для очитски*/
	void onBeginAddingContact();

    /**
     * Событие вызывается на получение результата поиска
     */
    void onNewContact(Contact contact);


    void onIncomingMessage(String text, int date, int userId);
    
//    void onHistoryMessage(Contact from, String text, long date, int userId,boolean unread);
}
