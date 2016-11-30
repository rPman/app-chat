
package org.luwrain.app.chat.im;

public enum State {
    /**  требуется регистрация, будет прислан смс */
    UNREGISTERED,

    /**  требуется подтверждение смс */
    REGISTERED,

    /**  авторизован, смс не потребуется*/
    authorized

    /** еще не запущена */,
    READY_FOR_AUTHORIZATION,
}
