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

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.charset.Charset;

import org.luwrain.core.*;
import org.luwrain.app.chat.base.*;

import org.apache.commons.codec.binary.Hex;

import org.telegram.api.TLConfig;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLCheckedPhone;
import org.telegram.api.auth.TLExportedAuthorization;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.contacts.TLContacts;
import org.telegram.api.contacts.TLImportedContacts;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.RpcCallback;
import org.telegram.api.engine.RpcException;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.functions.auth.TLRequestAuthCheckPhone;
import org.telegram.api.functions.auth.TLRequestAuthExportAuthorization;
import org.telegram.api.functions.auth.TLRequestAuthImportAuthorization;
import org.telegram.api.functions.auth.TLRequestAuthSendCall;
import org.telegram.api.functions.auth.TLRequestAuthSignIn;
import org.telegram.api.functions.auth.TLRequestAuthSignUp;
import org.telegram.api.functions.contacts.TLRequestContactsGetContacts;
import org.telegram.api.functions.contacts.TLRequestContactsImportContacts;
import org.telegram.api.functions.help.TLRequestHelpGetConfig;
import org.telegram.api.functions.messages.TLRequestMessagesGetHistory;
import org.telegram.api.functions.messages.TLRequestMessagesSendMessage;
import org.telegram.api.input.TLInputPhoneContact;
import org.telegram.api.input.peer.TLAbsInputPeer;
import org.telegram.api.input.peer.TLInputPeerUser;
import org.telegram.api.message.TLAbsMessage;
import org.telegram.api.message.TLMessage;
import org.telegram.api.messages.TLAbsMessages;
import org.telegram.api.messages.TLMessages;
import org.telegram.api.messages.TLMessagesSlice;
import org.telegram.api.updates.TLAbsUpdates;
import org.telegram.api.updates.TLUpdateShortMessage;
import org.telegram.api.user.TLAbsUser;
import org.telegram.api.user.TLUser;
import org.telegram.bot.kernel.engine.MemoryApiState;
import org.telegram.tl.TLBytes;
import org.telegram.tl.TLVector;

import org.luwrain.app.chat.Settings;
import org.luwrain.app.chat.TelegramAccount;

public class Telegram extends TelegramLoggerControl
{
    public enum State {READY, REQUIRE_SIGN_UP, REQUIRE_SIGN_IN, REQUIRE_RESIGN_IN,
		       MIGRATE, AUTHORIZED, ERROR};

    /** Timeout milliseconds */
    private final int TIMEOUT = 15000;

    /** Telegram Application hash */
    final String APIHASH = "62155226f23b8565aa3aaa0fa68df878";

    /** Telegram Application id */
    final Integer APIID = 97022;

    private final Settings.Telegram sett;
    private final Events events;
    private TelegramApi api;
    private TLAuthorization auth;
    TLConfig tlconfig;
    private final TelegramAccount account;
    private final MemoryApiState memstate;

    TLCheckedPhone checked=null;

    private State state = State.REQUIRE_SIGN_UP;

    public Telegram(Settings.Telegram sett, Events events,
    		TelegramAccount account)
    {
	NullCheck.notNull(events, "events");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(account, "account");
	this.account = account;
	this.events = events;
	this.sett = sett;
    this.memstate = new MemoryApiState("Telegram."+sett.getPhone("")+".raw");
    }

    private boolean init()
    {
	api = new TelegramApi(memstate, new AppInfo(APIID, "luwrain", "1.0", "1.0", "en"), new ApiCallback() {
		@Override public void onAuthCancelled(TelegramApi api) 
		{
		    NullCheck.notNull(api, "api");
		    Log.debug("chat-telegram", "onAuthCancelled:" + api);
		}
		@Override public void onUpdatesInvalidated(TelegramApi api) 
		{
		    NullCheck.notNull(api, "api");
		    Log .debug("chat-telegram", "onUpdatesInvalidated:" + api);
		}
		@Override public void onUpdate(TLAbsUpdates updates) 
		{
		    NullCheck.notNull(updates, "updates");
		    Log.debug("chat-telegram", "onUpdate:" + updates.getClass().getName()+" "+updates);
		    if (updates instanceof TLUpdateShortMessage) 
			{
				final TLUpdateShortMessage message = (TLUpdateShortMessage)updates;
				Log.debug("chat-telegram", "message:" + message.getMessage());
				events.onIncomingMessage(message.getMessage(), message.getDate(), message.getUserId()); 
			}
		}
	    });
	Log.debug("chat-telegram", "performing TLRequestHelpGetConfig()");
	try
	{ // FIXME: 2 dc by default is potential bug, but without it we can get timout error
	    tlconfig = api.doRpcCallNonAuth(new TLRequestHelpGetConfig(),TIMEOUT,2);
	    Log.debug("chat-telegram", "TLRequestHelpGetConfig() done");
	}
	catch (Exception e) 
	{
state = State.ERROR;
	    onError(e);
	    return false;
	}
	memstate.updateSettings(tlconfig);
	memstate.setPrimaryDc(tlconfig.getThisDc());
	Log.debug("chat-telegram", "getThisDc() is " + tlconfig.getThisDc());
	return true;
    }

    public void open()
    {
state = State.READY;
	if (!init())
	{
	    Log.error("chat-telegram", "init failed");
	    return;
    }
	Log.debug("chat-telegram", "init completed");
	Log.debug("chat-telegram", "tlconfig.getThisDc " + tlconfig.getThisDc());
        final TLRequestAuthCheckPhone authCheckPhone = new TLRequestAuthCheckPhone();
        authCheckPhone.setPhoneNumber(sett.getPhone(""));
        try {
	    Log.debug("chat-telegram", "trying TLRequestAuthCheckPhone for " + sett.getPhone(""));
	    checked = api.doRpcCallNonAuth(authCheckPhone,TIMEOUT,api.getState().getPrimaryDc());
		Log.debug("chat-telegram", "isPhoneRegistered:" + checked.isPhoneRegistered());
		if (checked.isPhoneRegistered()==false)
		{
		    state = State.REQUIRE_SIGN_UP;
		    return;
		}
		    if (sett.getAuthSmsCode("").trim().isEmpty() || sett.getAuthPhoneHash("").trim().isEmpty())
		    {
			state = State.REQUIRE_SIGN_IN;
			return;
		    }
		    state = State.READY;
        }
catch (RpcException e) 
{
	String errorInfo="MIGRATE check - "+e.getClass().getName() + ":" + e.getErrorCode() + ":" + e.getMessage();
	Log.debug("chat-telegram", "authorisation procedure, " + errorInfo);
	if (e.getErrorCode() == 303)
	{
		final int destDC;
		if (e.getErrorTag().startsWith("NETWORK_MIGRATE_")) 
		{
			destDC = Integer.parseInt(e.getErrorTag().substring("NETWORK_MIGRATE_".length()));
state = State.REQUIRE_SIGN_UP;
		} else 
		if (e.getErrorTag().startsWith("PHONE_MIGRATE_")) 
		{
			destDC = Integer.parseInt(e.getErrorTag().substring("PHONE_MIGRATE_".length()));
state = State.REQUIRE_SIGN_IN;
		} else 
		if (e.getErrorTag().startsWith("USER_MIGRATE_")) 
		{
			destDC = Integer.parseInt(e.getErrorTag().substring("USER_MIGRATE_".length()));
state = State.MIGRATE;
		} else 
		{
state = State.ERROR;
			events.onError(errorInfo);
			return;
		}
		Log.debug("chat-telegram ", "state:" + state);
		api.switchToDc(destDC);
		Log.debug("chat-telegram", "destDC:" + destDC);
	} else 
	{
state = State.ERROR;
		events.onError(errorInfo);
		return;
	}
} //catch()
catch (TimeoutException e) 
{
    Log.error("chat-telegram", e.getClass().getName() + ":" + e.getMessage());
			e.printStackTrace();
}
	Log.debug("chat-telegram", "telegram opened: , state=" + state);
}

    public void setReconnect()
    {
state = State.REQUIRE_SIGN_IN;
    }

    public boolean connect()
    {
    		Log.debug("chat-telegram", "connecting, original state:" + state);
        switch (state)
        {
	case READY:
	    return signWithoutSms();
	case REQUIRE_SIGN_UP:
	case REQUIRE_SIGN_IN:
	case REQUIRE_RESIGN_IN:
	    signWithSms();
	    return true;
	case MIGRATE:
	    return migrate();
	default:
	    return false;
	}
    }

    public void finish()
	{
		api.close();
	}

    private boolean signWithoutSms()
    {
	    try {
	    	signIn(sett.getAuthSmsCode(""), sett.getAuthPhoneHash(""));
			return true;
	    } 
	    catch (RpcException e) 
	    {
		if (e.getMessage().toLowerCase().equals("phone_code_expired"))
		{
state = State.REQUIRE_RESIGN_IN;
		    sett.setAuthSmsCode("");
		    sett.setAuthPhoneHash("");
		    return false;
		}
	    	onError(e);
	    	return false;
	    }
	    catch(TimeoutException e)
	    {
		onError(e);
		return false;
	    }
    }

    private void signWithSms()
    {
   	Boolean smsVoice=sett.getSmsVoice(true); 
	TLSentCode sentCode;
//   	if(smsVoice==null||smsVoice)
//   	{
////   		final TLRequestAuthSendCall
//   
//   	} else
   	{
		final TLRequestAuthSendCall authSendCode =  new TLRequestAuthSendCall();
		//authSendCode.setFlags(1<<3);
		authSendCode.setPhoneNumber(sett.getPhone(""));
        authSendCode.setApiHash(APIHASH);
        authSendCode.setApiId(APIID);
		try {
		    Log.debug("chat-telegram", "trying RpcRequestAuthSendCode "+memstate.getPrimaryDc());
		    sentCode = api.doRpcCallNonAuth(authSendCode,TIMEOUT,memstate.getPrimaryDc());
		} 
		catch (Exception e) 
		{
		    onError(e, "TLRequestAuthSendCode");
		    return;
		}
   	}
	// request twofactor sms code from user
	final String answer = events.askTwoPassAuthCode();
	if (answer == null || answer.trim().isEmpty())
	    return;
	Log.debug("chat-telegram", "phone code hash:" + sentCode.getPhoneCodeHash());
	Log.debug("chat-telegram", "user answer:" + answer);
	// do auth in server
	auth = null;
	switch (state)
	{
	case REQUIRE_SIGN_IN:
	case REQUIRE_RESIGN_IN:
	    try {
		signIn(answer, sentCode.getPhoneCodeHash());
	    } 
	    catch (Exception e) 
	    {
	    	onError(e,"TLRequestAuthSignIn");
	    	return;
	    }
	    break;
	case REQUIRE_SIGN_UP:
		final TLRequestAuthSignUp sign = new TLRequestAuthSignUp();
		sign.setFirstName(sett.getFirstName(""));
		sign.setLastName(sett.getLastName(""));
		sign.setPhoneCode(answer);
		sign.setPhoneCodeHash(sentCode.getPhoneCodeHash());
		sign.setPhoneNumber(sett.getPhone(""));
		try {
			auth = api.doRpcCallNonAuth(sign);
		} 
		catch (RpcException | TimeoutException e) {
			onError(e, "TLRequestAuthSignUp");
			return;
		} 
		Log.debug("chat-telegram", "isTemporalSession "+auth.isTemporalSession()+" user "+auth.getUser().getId());
		break;
	}
	sett.setAuthSmsCode(answer);
	sett.setAuthPhoneHash(sentCode.getPhoneCodeHash());
state = State.AUTHORIZED;
	Log.debug("chat-telegram", "getAuthKey " + Hex.encodeHex(api.getState().getAuthKey(tlconfig.getThisDc())).toString());
	Log.debug("chat-telegram", "getUserId " + api.getState().getUserId());
    }

    private void signIn(String code, String hash) throws RpcException, TimeoutException
    {
	NullCheck.notEmpty(code, "code");
	NullCheck.notEmpty(hash, "hash");
	Log.debug("chat-telegram", "signIn(" + code + ", " + hash + ")");
	final TLRequestAuthSignIn sign = new TLRequestAuthSignIn();
	sign.setPhoneCode(code);
	sign.setPhoneCodeHash(hash);
	sign.setPhoneNumber(sett.getPhone(""));
	auth = api.doRpcCallNonAuth(sign, TIMEOUT, api.getState().getPrimaryDc());
state = State.AUTHORIZED;
	Log.debug("chat-telegram", "isTemporalSession " + auth.isTemporalSession());
    }

public void getContacts() 
    {
	final TLRequestContactsGetContacts cntcs = new TLRequestContactsGetContacts();
	cntcs.setHash("");
	TLContacts rescnts;
	try {
events.onBeginAddingContact();
	    Log.debug("chat-telegram ", "getSearchContact " + api.getState().getUserId());
	    rescnts = (TLContacts) api.doRpcCallNonAuth(cntcs,TIMEOUT,api.getState().getPrimaryDc());
	    Log.debug("chat-telegram", "contacts users " + rescnts.getUsers().size());
	    Log.debug("chat-telegram", "contacts result " + rescnts.getContacts().size());
	    for(TLAbsUser o:rescnts.getUsers())
	    {
			final TLUser u=(TLUser)o;
			final TelegramContact contact = new TelegramContact(account){};
			contact.init(u.getAccessHash(),u.getId());
			contact.setUserInfo(u.getFirstName(),u.getLastName(),u.getUserName(),u.getPhone());
			events.onNewContact(contact);
	    }
	    return;
	} 
	catch (Exception e) 
	{
	    onError(e);
	    return;
	} 
    }

	public Message[] requestHistoryMessages(TelegramContact contact)
	{
	Log.debug("","request message history for: "+contact.userName+"["+contact.phone+"]");
	try {
		final TLRequestMessagesGetHistory mh = new TLRequestMessagesGetHistory();
		final TLInputPeerUser	peeruser=new TLInputPeerUser();
		peeruser.setUserId(contact.getUserId());
		mh.setPeer((TLAbsInputPeer)peeruser);
		final TLAbsMessages am = api.doRpcCallNonAuth(mh,TIMEOUT,api.getState().getPrimaryDc());
		final TLVector<TLAbsMessage> messages;
		if (am instanceof TLMessagesSlice)
			messages = ((TLMessagesSlice)am).getMessages(); else
		if (am instanceof TLMessages)
			messages=((TLMessages)am).getMessages(); else
		{
			onError(new Exception("undefined type "+am.getClass().getName()));
			return new Message[0];
		}

		Message[] tmsgs=new Message[messages.size()];
		int i=0;
		for (TLAbsMessage m: messages)
		{
			final TLMessage message = (TLMessage)m;
			// FIXME: ugly TelegramAccount must be refactored
			tmsgs[i++]=((TelegramAccount)account).makeNewMessageFrom(message.getMessage(),message.getDate(),message.getFromId(),message.isUnreadContent());
			//events.onHistoryMessage(contact, message.getMessage(), message.getDate(), message.getFromId(), message.isUnreadContent());
		}
		return tmsgs;
	}
	catch (Exception e) 
	{
	    onError(e);
	    return new Message[0];
	} 
	}

	public boolean  sendMessage(long accessHash, int userId, String text)
	{
	    NullCheck.notNull(text, "text");
	    Log.debug("chat-telegram", "sending \"" + text + "\" to " + userId);
		try {
		    final TLRequestMessagesSendMessage message=new TLRequestMessagesSendMessage();
			message.setMessage(text);
	//		System.out.println(rescnts.getUsers().get(0).getId());
			message.setRandomId(new Date().getTime());
			final TLInputPeerUser	peeruser=new TLInputPeerUser();
			peeruser.setAccessHash(accessHash);
			peeruser.setUserId(userId);
			message.setPeer(peeruser);
			final TLAbsUpdates updates=api.doRpcCallNonAuth(message);
			Log.debug("chat-telegram", "message sent successfully");
			return true;
		}
		catch (Exception e)
		{
		    onError(e);
	       	return false;
		} 
	}

    public void addContact(String phone,String firstname,String lastname,Runnable finished)
    {
	final TLRequestContactsImportContacts ic = new TLRequestContactsImportContacts();
	final TLInputPhoneContact pc=new TLInputPhoneContact();
	pc.setFirstName(firstname);
	pc.setLastName(lastname);
	pc.setPhone(phone);
	TLVector<TLInputPhoneContact> vpc=new TLVector<TLInputPhoneContact>();
	vpc.add(pc);
	ic.setContacts(vpc);
	ic.setReplace(false);
	Log.debug("chat-telegram", "trying TLRequestContactsImportContacts");
	api.doRpcCallNonAuth(ic,TIMEOUT,new RpcCallback<TLImportedContacts>()
			     {
				 @Override public void onError(int arg0,String arg1)
				 {
				     Log.error("chat-telegram", "Add contact error: "+arg1);				
				 }
				 @Override public void onResult(TLImportedContacts arg0)
				 {
				     Log.debug("chat-telegram", "Add contact success: "+arg0.getUsers().size());				
				     finished.run();
				 }});
    }

    private boolean migrate()
    {
	    try {
	    	ExportAuthorization();
	    	ImportAuthorization();
	    	return true;
	    } 
catch (Exception e) 
	    {
	    	onError(e);
	    	return false;
	    }
    }

	private void ImportAuthorization() throws Exception
	{
		TLRequestAuthImportAuthorization impauth=new  TLRequestAuthImportAuthorization(); 
		impauth.setId(197321144);
		try {
			impauth.setBytes(new TLBytes(Hex.decodeHex("3cc1b1a3763c2cad6815a5de0ffc5208e8cb6b04917d3aa88901985537edfd5d06841111785cea72a65db78f753cfe4803d5a50880cf29dd83a4a40b69a3478fea7740fe1782a945a56a49e80a8be2fcb86ed6cecc32b6ca83d46001e8e6f8ea16806c87d5c793b3e3088c598b158abdca6123fe6a915e579dc834a608ddb25456542c1e8f3290d96c12adbea2adfe7812e68dd7c9a741a1111b7e8445abc5de822abdbd9665e1c869e3ec055dce0460917785d7f8464716a50ed9a25510f51980b5cda420847ee37d7df442901330e8a03f90cd10f49e5694a3da11ccb245ac669e8c9725baae6398d8a529043624c913c2b00bf60684337165e37c5cf8c994".toCharArray()))); 
		} catch (Exception e) 
		{ // TODO Auto-generated catch block e.printStackTrace(); 
state = State.ERROR;
			events.onError(e.getMessage());
			throw e;
		} 
		try {
			System.out.println("1"); 
			auth=api.doRpcCallNonAuth(impauth, TIMEOUT, api.getState().getPrimaryDc());
			System.out.println("2"); 
			memstate.doAuth(auth); 
			System.out.println("3");
//			that.events.onAuthFinish(); 
			return; 
		} 
		catch (Exception e) {
			throw e;
		} 
	}

	private void ExportAuthorization() throws Exception
	{
	    //		final TelegramImpl that = this;
		final TLRequestAuthExportAuthorization expauth = new TLRequestAuthExportAuthorization();
		expauth.setDcId(tlconfig.getThisDc());
		try {
			TLExportedAuthorization eauth = api.doRpcCallNonAuth(expauth);
			Log.debug("chat-telegram", "getId:" + eauth.getId());
			Log.debug("chat-telegram", "getBytes "+new String(Hex.encodeHex(eauth.getBytes().getData())));
		} catch (Exception e1) {
			System.out.println("error TLRequestAuthExportAuthorization");
			e1.printStackTrace();
			throw e1;
		}
	}

    private void onError(Exception e, String comment)
    {
state = State.ERROR;
	Log.error("chat-telegram", comment + ":" + e.getClass().getName() + ":" + e.getMessage());
	e.printStackTrace();
	events.onError(e.getClass() + ":" + e.getMessage());
    }

    private void onError(Exception e)
    {
state = State.ERROR;
	Log.error("chat-telegram", e.getClass().getName() + ":" + e.getMessage());
	e.printStackTrace();
	events.onError(e.getClass() + ":" + e.getMessage());
    }

    public Telegram.State getState()
    {
	return state;
    }

	public void close()
	{
		api.close();
		api=null;
		Log.debug("chat-telegram", "account closed");
	}
}
