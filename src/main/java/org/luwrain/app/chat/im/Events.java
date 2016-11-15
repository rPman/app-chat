
package org.luwrain.app.chat.im;

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
    void on2PassAuth(String message);

    /**
     * Событие вызывается при окончании успешной авторизации
     */
    void onAuthFinish();

    /**
     * Событие вызывается на получение результата поиска
     */
    void onNewContact(Contact contact);
}
