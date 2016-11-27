
package org.luwrain.app.chat.im.telegram;

//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Date;
//import java.util.concurrent.TimeoutException;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.luwrain.core.*;
import org.luwrain.app.chat.im.*;
import org.luwrain.app.chat.TelegramAccount;

import org.apache.commons.codec.binary.Hex;

import org.telegram.api.TLConfig;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLCheckedPhone;
import org.telegram.api.auth.TLExportedAuthorization;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.contact.TLContact;
import org.telegram.api.contacts.TLContacts;
import org.telegram.api.contacts.TLImportedContacts;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.RpcCallback;
import org.telegram.api.engine.RpcCallbackEx;
import org.telegram.api.engine.RpcException;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.functions.auth.TLRequestAuthCheckPhone;
import org.telegram.api.functions.auth.TLRequestAuthExportAuthorization;
import org.telegram.api.functions.auth.TLRequestAuthImportAuthorization;
import org.telegram.api.functions.auth.TLRequestAuthSendCode;
import org.telegram.api.functions.auth.TLRequestAuthSignIn;
import org.telegram.api.functions.auth.TLRequestAuthSignUp;
import org.telegram.api.functions.contacts.TLRequestContactsGetContacts;
import org.telegram.api.functions.contacts.TLRequestContactsImportContacts;
import org.telegram.api.functions.help.TLRequestHelpGetConfig;
import org.telegram.api.functions.messages.TLRequestMessagesSendMessage;
import org.telegram.api.functions.users.TLRequestUsersGetUsers;
import org.telegram.api.input.TLInputPhoneContact;
import org.telegram.api.input.peer.TLInputPeerUser;
import org.telegram.api.input.user.TLAbsInputUser;
import org.telegram.api.input.user.TLInputUser;
import org.telegram.api.updates.TLAbsUpdates;
import org.telegram.api.updates.TLUpdateShortMessage;
import org.telegram.api.user.TLAbsUser;
import org.telegram.api.user.TLUser;
import org.telegram.bot.kernel.engine.MemoryApiState;
import org.telegram.tl.TLBytes;
import org.telegram.tl.TLObject;
import org.telegram.tl.TLVector;
import org.luwrain.app.chat.TelegramAccount;
import org.luwrain.app.chat.Settings;

public class TelegramImpl
{
    private enum Task {SIGN_UP, SIGN_IN, MIGRATE, NONE};

    /** Timeout milliseconds */
    private final int TIMEOUT = 15000;

    /** Telegram Application hash */
    final String APIHASH = "62155226f23b8565aa3aaa0fa68df878";

    /** Telegram Application id */
    final Integer APIID = 97022;

    private final Config config;
    private final Settings.Telegram sett;
    private final Events events;
    private TelegramApi api;
    private TLAuthorization auth;
    TLConfig tlconfig;
    TelegramAccount tAccount;
    private final MemoryApiState memstate;

    TLCheckedPhone checked=null;

    private Task task;
    private State state = State.none;

    private static Object autorun=autoRun(); 
    private static Object autoRun()
    {
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
	return null;
    }

    public TelegramImpl(Config config, Events events,
TelegramAccount tAccount, Settings.Telegram sett)
    {
	NullCheck.notNull(config, "config");
	NullCheck.notNull(events, "events");
	NullCheck.notNull(sett, "sett");
	this.config = config;
	//	this.tAccount=tAccount;
	this.events = events;
	this.sett = sett;
    this.memstate = new MemoryApiState("Telegram."+config.phone+".raw");
    }

    private boolean init()
    {
	api = new TelegramApi(memstate, new AppInfo(APIID, "console", "1.0", "1.0", "en"), new ApiCallback() {
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
				TLUpdateShortMessage updatesh=(TLUpdateShortMessage)updates;
				events.receiveNewMessage(updatesh.getMessage(),updatesh.getDate(),updatesh.getUserId()); 
			}
		}
	    });
	Log.debug("chat-telegram", "performing TLRequestHelpGetConfig()");
	try {
	    tlconfig = api.doRpcCallNonAuth(new TLRequestHelpGetConfig(),TIMEOUT,2);
	    Log.debug("chat-telegram", "TLRequestHelpGetConfig() done");
	}
catch (Exception e) 
	{
	    onError(e);
	    return false;
	}
	memstate.updateSettings(tlconfig);
	memstate.setPrimaryDc(tlconfig.getThisDc());
	Log.debug("chat-telegram", "getThisDc() is " + tlconfig.getThisDc());
	//���������, ���� ���� � ����������� ����������� (��������� ��� ��� � hash)
	return true;
    }

    public void open()
    {
	task = Task.NONE;
	// getAuthKey
	// 3cc1b1a3763c2cad6815a5de0ffc5208e8cb6b04917d3aa88901985537edfd5d06841111785cea72a65db78f753cfe4803d5a50880cf29dd83a4a40b69a3478fea7740fe1782a945a56a49e80a8be2fcb86ed6cecc32b6ca83d46001e8e6f8ea16806c87d5c793b3e3088c598b158abdca6123fe6a915e579dc834a608ddb25456542c1e8f3290d96c12adbea2adfe7812e68dd7c9a741a1111b7e8445abc5de822abdbd9665e1c869e3ec055dce0460917785d7f8464716a50ed9a25510f51980b5cda420847ee37d7df442901330e8a03f90cd10f49e5694a3da11ccb245ac669e8c9725baae6398d8a529043624c913c2b00bf60684337165e37c5cf8c994
	// getUserId 197321144
	if (!init())
	{
	    Log.error("chat-telegram", "init failed");
	    return;
	    }
	Log.debug("chat-telegram", "init completed");
		//api.switchToDc(1);
	Log.debug("chat-telegram", "tlconfig.getThisDc " + tlconfig.getThisDc());
		// �������� ���������� DC
//		TLRequestHelpGetNearestDc rndc = new TLRequestHelpGetNearestDc();
//		TLNearestDc ndc;
//		try {
//			ndc = api.doRpcCallNonAuth(rndc);
//		} catch (Exception e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//			that.events.onError(e2.getMessage());
//			return;
//		}
//		System.out.println("getNearestDc " + ndc.getNearestDc() + ", getThisDc " + ndc.getThisDc());
//		api.switchToDc(ndc.getNearestDc());
        final TLRequestAuthCheckPhone authCheckPhone = new TLRequestAuthCheckPhone();
        authCheckPhone.setPhoneNumber(config.phone);
        try {
	    Log.debug("chat-telegram", "trying TLRequestAuthCheckPhone for " + config.phone);
	    checked = api.doRpcCallNonAuth(authCheckPhone,TIMEOUT,api.getState().getPrimaryDc());
		Log.debug("chat-telegram", "isPhoneRegistered:" + checked.isPhoneRegistered());
		if (checked.isPhoneRegistered()==false)
		{
			task = Task.SIGN_UP;
			setState(State.UNREGISTERED);
		}
        } 
catch (RpcException e) 
{
    Log.error("chat-telegram", "unable to do TLRequestAuthCheckPhone:" + e.getClass().getName() + ":" + e.getErrorCode() + ":" + e.getMessage());
            if (e.getErrorCode() == 303)
{
                final int destDC;
                if (e.getErrorTag().startsWith("NETWORK_MIGRATE_")) 
{
                    destDC = Integer.parseInt(e.getErrorTag().substring("NETWORK_MIGRATE_".length()));
                    task = Task.SIGN_UP;
                    setState(State.UNREGISTERED);
                } else 
if (e.getErrorTag().startsWith("PHONE_MIGRATE_")) 
{
                    destDC = Integer.parseInt(e.getErrorTag().substring("PHONE_MIGRATE_".length()));
                    task = Task.SIGN_IN;
                    setState(State.REGISTERED);
                } else 
if (e.getErrorTag().startsWith("USER_MIGRATE_")) 
{
                    destDC = Integer.parseInt(e.getErrorTag().substring("USER_MIGRATE_".length()));
                    task = Task.MIGRATE;
                } else 
{
    events.onError(e.getClass().getName() + ":" + e.getErrorCode() + ":" + e.getMessage());
    				return;
                }
		Log.debug("chat-telegram ", "task:" + task);
                api.switchToDc(destDC);
                //phone = "99966"+destDC+"2345";
		Log.debug("chat-telegram", "destDC:" + destDC);
} else 
{
    events.onError(e.getClass() + ":" + e.getErrorCode() + ":" + e.getMessage());
				return;
            }
} //catch()
        catch (TimeoutException e) 
{
    Log.error("chat-telegram", e.getClass().getName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	Log.debug("chat-telegram", "telegram opened: state=" + state + ", task=" + task);
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
	// request twofactor auth
	final TLRequestAuthSendCode authSendCode =  new TLRequestAuthSendCode();
        authSendCode.setPhoneNumber(config.phone);
        authSendCode.setApiHash(APIHASH);
        authSendCode.setApiId(APIID);
	final TLSentCode sentCode;
	final String phoneCodeHash;
	try {
	    Log.debug("chat-telegram", "trying RpcRequestAuthSendCode "+memstate.getPrimaryDc());
	    sentCode = api.doRpcCallNonAuth(authSendCode,TIMEOUT,memstate.getPrimaryDc());
	    phoneCodeHash = sentCode.getPhoneCodeHash();
	} 
	catch (Exception e) 
	{
	    onError(e, "TLRequestAuthSendCode");
	    return;
	}
	final String answer = events.askTwoPassAuthCode("Enter the code from SMS:");
	if (answer == null || answer.trim().isEmpty())
	    return;
	Log.debug("chat-telegram", "phone code hash:" + phoneCodeHash);
	Log.debug("chat-telegram", "user answer:" + answer);
	auth = null;
	switch (task)
	{
	case SIGN_IN:
	    try {
		signIn(answer, phoneCodeHash);
	    } 
	    catch (Exception e) 
	    {
		onError(e);
		return;
	    }
	    break;
	case SIGN_UP:
	    final TLRequestAuthSignUp sign = new TLRequestAuthSignUp();
	    sign.setFirstName(config.firstName);
	    sign.setLastName(config.lastName);
	    sign.setPhoneCode(answer);
	    sign.setPhoneCodeHash(sentCode.getPhoneCodeHash());
	    sign.setPhoneNumber(config.phone);
	    try {
		auth = api.doRpcCallNonAuth(sign);
	    } 
	    catch (RpcException | TimeoutException e) 
	    {
		onError(e, "TLRequestAuthSignUp");
		return;
	    } 
	    Log.debug("chat-telegram", "isTemporalSession "+auth.isTemporalSession()+" user "+auth.getUser().getId());
	    break;
	}
	setState(State.authorized);
	sett.setAuthSmsCode(answer);
	sett.setAuthPhoneHash(phoneCodeHash);
	Log.debug("chat-telegram", "getAuthKey " + Hex.encodeHex(api.getState().getAuthKey(tlconfig.getThisDc())));
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
	    return state;
	}

	synchronized public void setState(State state) 
	{
			this.state=state;
		}

	private void ImportAuthorization() throws Exception
	{
		final TelegramImpl that = this;
		  TLRequestAuthImportAuthorization impauth=new  TLRequestAuthImportAuthorization(); 
		  impauth.setId(197321144);
		  try {
		  impauth.setBytes(new TLBytes(Hex.decodeHex("3cc1b1a3763c2cad6815a5de0ffc5208e8cb6b04917d3aa88901985537edfd5d06841111785cea72a65db78f753cfe4803d5a50880cf29dd83a4a40b69a3478fea7740fe1782a945a56a49e80a8be2fcb86ed6cecc32b6ca83d46001e8e6f8ea16806c87d5c793b3e3088c598b158abdca6123fe6a915e579dc834a608ddb25456542c1e8f3290d96c12adbea2adfe7812e68dd7c9a741a1111b7e8445abc5de822abdbd9665e1c869e3ec055dce0460917785d7f8464716a50ed9a25510f51980b5cda420847ee37d7df442901330e8a03f90cd10f49e5694a3da11ccb245ac669e8c9725baae6398d8a529043624c913c2b00bf60684337165e37c5cf8c994".toCharArray()))); 
		  } catch (Exception e) 
		  { // TODO Auto-generated catch block e.printStackTrace(); 
events.onError(e.getMessage());
				throw e;
		  } 
		  try {
			  System.out.println("1"); 
			  auth=api.doRpcCallNonAuth(impauth, TIMEOUT, api.getState().getPrimaryDc());
			  System.out.println("2"); 
			  memstate.doAuth(auth); 
			  System.out.println("3");
			  //			  that.events.onAuthFinish(); 
			  return; 
			  } 
		  catch (Exception e) {
			  throw e;
		  } 
	}

	private void ExportAuthorization() throws Exception
	{
		final TelegramImpl that = this;
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
		final TelegramImpl that = this;
		Log.debug("chat-telegram", "signIn(" + code + ", " + hash + ")");
		final TLRequestAuthSignIn sign = new TLRequestAuthSignIn();
		sign.setPhoneCode(code);
		sign.setPhoneCodeHash(hash);
		sign.setPhoneNumber(that.config.phone);
		try {
		    auth = api.doRpcCallNonAuth(sign, TIMEOUT, api.getState().getPrimaryDc());
		} 
catch (Exception e) 
{
    onError(e, "TLRequestAuthSignIn");
			throw e;
		} 
		setState(State.authorized);
		Log.debug("chat-telegram", "isTemporalSession " + auth.isTemporalSession());
	}

	public void checkContacts() {
		TLRequestContactsGetContacts cntcs = new TLRequestContactsGetContacts();
		cntcs.setHash("");
		TLContacts rescnts;
		try {
			getEvents().onBeginAddingContact();
			System.out.println("getSeachContact "+getApi().getState().getUserId());
			rescnts = (TLContacts) api.doRpcCallNonAuth(cntcs,TIMEOUT,api.getState().getPrimaryDc());
			System.out.println("contacts users " + rescnts.getUsers().size());
			System.out.println("contacts result " + rescnts.getContacts().size());
			for(TLAbsUser o:rescnts.getUsers())
			{
				TLUser u=(TLUser)o;
				TelegramContactImpl contact=new TelegramContactImpl(tAccount){};
				contact.init(u.getAccessHash(),u.getId(),new TelegramMessageListImpl());
				contact.setUserInfo(u.getFirstName(),u.getLastName(),u.getUserName(),u.getPhone());
				getEvents().onNewContact(contact);	
			}
//			TLRequestMessagesSendMessage message=new TLRequestMessagesSendMessage();
//			message.setMessage("hello");
//			System.out.println(rescnts.getUsers().get(0).getId());
//			message.setRandomId(rescnts.getUsers().get(0).getId());
//			message.setPeer(new TLAbsInputPeer() {
//				
//				@Override
//				public String toString() {
//					// TODO Auto-generated method stub
//					return null;
//				}
//				
//				@Override
//				public int getClassId() {
//					// TODO Auto-generated method stub
//					return 0;
//				}
//			});
//			getApi().doRpcCall(message);
			System.out.println("ОК");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			getEvents().onError(e.getMessage());
        	return;
		} 
	}

    public void finish()
	{
		api.close();
	}

	public void sendNewMessage(long accessHash,int userId,String text)
	{
		try {
			TLRequestMessagesSendMessage message=new TLRequestMessagesSendMessage();
			message.setMessage(text);
	//		System.out.println(rescnts.getUsers().get(0).getId());
			message.setRandomId(new Date().getTime());
			TLInputPeerUser	peeruser=new TLInputPeerUser();
			peeruser.setAccessHash(accessHash);
			peeruser.setUserId(userId);
			message.setPeer(peeruser);
			TLAbsUpdates updates=api.doRpcCallNonAuth(message);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			events.onError(e.getMessage());
	       	return;
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
		
		
		TLRequestContactsImportContacts ic=new TLRequestContactsImportContacts();
		TLInputPhoneContact pc=new TLInputPhoneContact();
		//pc.setClientId(userId);
		pc.setFirstName(firstname);
		pc.setLastName(lastname);
		pc.setPhone(phone);
		TLVector<TLInputPhoneContact> vpc=new TLVector<TLInputPhoneContact>();
		vpc.add(pc);
		ic.setContacts(vpc);
		ic.setReplace(false);
		api.doRpcCallNonAuth(ic,TIMEOUT,new RpcCallback<TLImportedContacts>()
		{

			@Override public void onError(int arg0,String arg1)
			{
				System.out.println("Add contact error: "+arg1);				
			}

			@Override public void onResult(TLImportedContacts arg0)
			{
				System.out.println("Add contact success: "+arg0.getUsers().size());				

				finished.run();
			}});
	}

    private void onError(Exception e, String comment)
    {
	Log.error("chat-telegram", comment + ":" + e.getClass().getName() + ":" + e.getMessage());
	e.printStackTrace();
	events.onError(e.getClass() + ":" + e.getMessage());
    }


    private void onError(Exception e)
    {
	Log.error("chat-telegram", e.getClass().getName() + ":" + e.getMessage());
	e.printStackTrace();
	events.onError(e.getClass() + ":" + e.getMessage());
    }
}
