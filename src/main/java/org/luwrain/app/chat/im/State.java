
package org.luwrain.app.chat.im;

public enum State
{
	/** not connected. unauthorized and sms code needed to auth, for example registration process or first login to machine */
	UNREGISTERED,
	/** not connected. unauthorized but sms code may not needed t auth */
	UNAUTHORIZED,
	//** not connected. unauthorized but in porgress, for example connecting or awaiting for sms code */
	UNAUTHORIZED_NEEDSMS,
	/** is connected. authorized, auth process finished */
	AUTHORIZED,
	/** not connected. error happened, auth process finished */
	ERROR, 
}
