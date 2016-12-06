
package org.luwrain.app.chat.im.telegram;

//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Date;
//import java.util.concurrent.TimeoutException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

import org.luwrain.core.*;
import org.luwrain.app.chat.im.*;

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
import org.telegram.api.functions.messages.TLRequestMessagesSendMessage;
import org.telegram.api.input.TLInputPhoneContact;
import org.telegram.api.input.peer.TLInputPeerUser;
import org.telegram.api.updates.TLAbsUpdates;
import org.telegram.api.updates.TLUpdateShortMessage;
import org.telegram.api.user.TLAbsUser;
import org.telegram.api.user.TLUser;
import org.telegram.bot.kernel.engine.MemoryApiState;
import org.telegram.tl.TLBytes;
import org.telegram.tl.TLVector;
import org.luwrain.app.chat.Settings;

public class Telegram extends TelegramLoggerControl
{
    private enum Task {NONE, SIGN_UP, SIGN_IN, MIGRATE, AUTHORIZED, ERROR};

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
    private final Account account;
    private final MemoryApiState memstate;

    TLCheckedPhone checked=null;

    private Task task;

    public Telegram(Settings.Telegram sett, Events events,
		    Account account)
    {
	NullCheck.notNull(events, "events");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(account, "account");
	this.account = account;
	this.events = events;
	this.sett = sett;
    this.memstate = new MemoryApiState("Telegram."+sett.getPhone("")+".raw");
    
    System.out.println("DEBUG: "+System.getProperty("file.encoding"));
    System.out.println("DEBUG: "+Charset.defaultCharset());
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
	   	task=Task.ERROR;
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
	task = Task.NONE;
	if (!init())
	{
	    Log.error("chat-telegram", "init failed");
	    return;
    }
	Log.debug("chat-telegram", "init completed");
	//api.switchToDc(1);
	Log.debug("chat-telegram", "tlconfig.getThisDc " + tlconfig.getThisDc());
//		TLRequestHelpGetNearestDc rndc = new TLRequestHelpGetNearestDc();
//		TLNearestDc ndc;
//		try {
//			ndc = api.doRpcCallNonAuth(rndc);
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			events.onError(e.getMessage());
//			return;
//		}
//		System.out.println("getNearestDc " + ndc.getNearestDc() + ", getThisDc " + ndc.getThisDc());
//		api.switchToDc(ndc.getNearestDc());
        final TLRequestAuthCheckPhone authCheckPhone = new TLRequestAuthCheckPhone();
        authCheckPhone.setPhoneNumber(sett.getPhone(""));
        try {
	    Log.debug("chat-telegram", "trying TLRequestAuthCheckPhone for " + sett.getPhone(""));
	    checked = api.doRpcCallNonAuth(authCheckPhone,TIMEOUT,api.getState().getPrimaryDc());
		Log.debug("chat-telegram", "isPhoneRegistered:" + checked.isPhoneRegistered());
		if (checked.isPhoneRegistered()==false)
		{
			task = Task.SIGN_UP;
		} else
		{
			String smsCode=sett.getAuthSmsCode("");
			if(smsCode==null||smsCode.isEmpty())
				task = Task.SIGN_IN;
			else
				task = Task.NONE;
		}
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
			task = Task.SIGN_UP;
		} else 
		if (e.getErrorTag().startsWith("PHONE_MIGRATE_")) 
		{
			destDC = Integer.parseInt(e.getErrorTag().substring("PHONE_MIGRATE_".length()));
			task = Task.SIGN_IN;
		} else 
		if (e.getErrorTag().startsWith("USER_MIGRATE_")) 
		{
			destDC = Integer.parseInt(e.getErrorTag().substring("USER_MIGRATE_".length()));
			task = Task.MIGRATE;
		} else 
		{
		   	task=Task.ERROR;
			events.onError(errorInfo);
			return;
		}
		Log.debug("chat-telegram ", "task:" + task);
		api.switchToDc(destDC);
		Log.debug("chat-telegram", "destDC:" + destDC);
	} else 
	{
	   	task=Task.ERROR;
		events.onError(errorInfo);
		return;
	}
} //catch()
catch (TimeoutException e) 
{
    Log.error("chat-telegram", e.getClass().getName() + ":" + e.getMessage());
			e.printStackTrace();
}
//    if(task==Task.NONE)
//    {
//    	// get current status of smscode and hash
//    	String smsCode=sett.getAuthSmsCode("");
//    	if(smsCode==null||smsCode.isEmpty())
//    		task=Task.SIGN_IN;
//    }
	Log.debug("chat-telegram", "telegram opened: , task=" + task);
}

    public void setReconnect()
    {
    	task=Task.SIGN_IN;
    }
    public void connect()
    {
    	
	Log.debug("chat-telegram", "connecting, task:" + task);
        switch (task)
        {
	case SIGN_IN:
	case SIGN_UP:
	    sign();
	    break;
	case MIGRATE:
	    try {
	    	ExportAuthorization();
	    	ImportAuthorization();
	    	return;
	    } catch (Exception e) 
	    {
	    	onError(e);
	    	return;
	    }
	case NONE:
	    final String code = sett.getAuthSmsCode("");
	    final String hash = sett.getAuthPhoneHash("");
	    try {
	    	signIn(code, hash);
			return;
	    } 
	    catch (Exception e) 
	    {
	    	onError(e);
	    	return;
	    }
	}
    }

    private void sign()
    {
	//Log.debug("chat-telegram", "performing signUp()");
	// request twofactor auth sms from server
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
	switch (task)
	{
	case SIGN_IN:
	    try {
		signIn(answer, sentCode.getPhoneCodeHash());
	    } 
	    catch (Exception e) 
	    {
	    	onError(e,"TLRequestAuthSignIn");
	    	return;
	    }
	    break;
	case SIGN_UP:
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
	task=Task.AUTHORIZED;
	Log.debug("chat-telegram", "getAuthKey " + Hex.encodeHex(api.getState().getAuthKey(tlconfig.getThisDc())).toString());
	Log.debug("chat-telegram", "getUserId " + api.getState().getUserId());
    }

private TelegramApi getApi() 
{
		return api;
	}

private Events getEvents() 
{
		return events;
	}

    private MemoryApiState getMemState() 
{
		return memstate;
	}

    synchronized public State getState() 
	{
    	if(task==null)
    		return State.ERROR;
    	switch(task)
    	{
    		case NONE:
    		case SIGN_IN:
    		{
    	    	String smsCode=sett.getAuthSmsCode("");
    	    	if(smsCode==null||smsCode.isEmpty())
    	    		return State.UNREGISTERED;
    	    	else
    	    		return State.UNAUTHORIZED;
    		}
    		case SIGN_UP:
    			return State.UNREGISTERED;
    		case MIGRATE:
    			return State.UNAUTHORIZED;
    		case AUTHORIZED:
    			return State.AUTHORIZED;
    		default:
    			return State.ERROR;
    	}
	}

	private void ImportAuthorization() throws Exception
	{
		//		final TelegramImpl that = this;
		TLRequestAuthImportAuthorization impauth=new  TLRequestAuthImportAuthorization(); 
		impauth.setId(197321144);
		try {
			impauth.setBytes(new TLBytes(Hex.decodeHex("3cc1b1a3763c2cad6815a5de0ffc5208e8cb6b04917d3aa88901985537edfd5d06841111785cea72a65db78f753cfe4803d5a50880cf29dd83a4a40b69a3478fea7740fe1782a945a56a49e80a8be2fcb86ed6cecc32b6ca83d46001e8e6f8ea16806c87d5c793b3e3088c598b158abdca6123fe6a915e579dc834a608ddb25456542c1e8f3290d96c12adbea2adfe7812e68dd7c9a741a1111b7e8445abc5de822abdbd9665e1c869e3ec055dce0460917785d7f8464716a50ed9a25510f51980b5cda420847ee37d7df442901330e8a03f90cd10f49e5694a3da11ccb245ac669e8c9725baae6398d8a529043624c913c2b00bf60684337165e37c5cf8c994".toCharArray()))); 
		} catch (Exception e) 
		{ // TODO Auto-generated catch block e.printStackTrace(); 
			task=Task.ERROR;
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

	private void signIn(String code,String hash) throws Exception
	{
	    //		final TelegramImpl that = this;
		Log.debug("chat-telegram", "signIn(" + code + ", " + hash + ")");
		final TLRequestAuthSignIn sign = new TLRequestAuthSignIn();
		sign.setPhoneCode(code);
		sign.setPhoneCodeHash(hash);
		sign.setPhoneNumber(sett.getPhone(""));
		try {
		    auth = api.doRpcCallNonAuth(sign, TIMEOUT, api.getState().getPrimaryDc());
		} 
		catch (Exception e) {
			throw e;
		} 
		task=Task.AUTHORIZED;
		Log.debug("chat-telegram", "isTemporalSession " + auth.isTemporalSession());
	}

    public void getContacts() 
    {
	final TLRequestContactsGetContacts cntcs = new TLRequestContactsGetContacts();
	cntcs.setHash("");
	TLContacts rescnts;
	try {
	    getEvents().onBeginAddingContact();
	    Log.debug("chat-telegram ", "getSearchContact "+getApi().getState().getUserId());
	    rescnts = (TLContacts) api.doRpcCallNonAuth(cntcs,TIMEOUT,api.getState().getPrimaryDc());
	    Log.debug("chat-telegram", "contacts users " + rescnts.getUsers().size());
	    Log.debug("chat-telegram", "contacts result " + rescnts.getContacts().size());
	    for(TLAbsUser o:rescnts.getUsers())
	    {
		final TLUser u=(TLUser)o;
		final TelegramContactImpl contact = new TelegramContactImpl(account){};
		contact.init(u.getAccessHash(),u.getId());
		contact.setUserInfo(u.getFirstName(),u.getLastName(),u.getUserName(),u.getPhone());
		getEvents().onNewContact(contact);	
	    }
	    Log.debug("chat-telegram", "list of contacts received");
	    return;
	} 
	catch (Exception e) 
	{
	    onError(e);
	    return;
	} 
    }

    public void finish()
	{
		api.close();
	}

	public boolean  sendNewMessage(long accessHash, int userId, String text)
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

	public void addNewContact(String phone,String firstname,String lastname,Runnable finished)
	{
//		TLRequestUsersGetUsers gu=new TLRequestUsersGetUsers();
//		TLVector<TLAbsInputUser> ids=new TLVector<TLAbsInputUser>();
//		TLInputUser iu=new TLInputUser();
//		iu.setUserId(userId);
//		ids.add(iu);
//		gu.setId(ids);
//		TLInputPhoneContact pc=new TLInputPhoneContact();
//		pc.setClientId(userId);
////		try
////		{
//			 TLVector<TLAbsUser> au=api.doRpcCallNonAuth(gu,TIMEOUT,new RpcCallback<TL>()
//				{
//
//					@Override public void onError(int arg0,String arg1)
//					{
//						// TODO Auto-generated method stub
//						
//					}
//
//					@Override public void onResult(TLUser arg0)
//					{
//						// TODO Auto-generated method stub
//						
//					}});
//			 for(TLAbsUser o:au)
//				{
//					TLUser u=(TLUser)o;
//					if (u.getId()==userId)
//					{
//						pc.setFirstName(u.getFirstName());
//						pc.setLastName(u.getLastName());
//						pc.setPhone(u.getPhone());
//					}
//				}
////		} catch(TimeoutException | IOException e)
////		{
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////			events.onError(e.getMessage());
////			return;
////		}

		final TLRequestContactsImportContacts ic = new TLRequestContactsImportContacts();
		final TLInputPhoneContact pc=new TLInputPhoneContact();
		//pc.setClientId(userId);
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

    private void onError(Exception e, String comment)
    {
   	task=task.ERROR;
	Log.error("chat-telegram", comment + ":" + e.getClass().getName() + ":" + e.getMessage());
	e.printStackTrace();
	events.onError(e.getClass() + ":" + e.getMessage());
    }


    private void onError(Exception e)
    {
   	task=task.ERROR;
	Log.error("chat-telegram", e.getClass().getName() + ":" + e.getMessage());
	e.printStackTrace();
	events.onError(e.getClass() + ":" + e.getMessage());
    }
}
