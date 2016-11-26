
package org.luwrain.app.chat.im.telegram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

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

import org.luwrain.core.*;
import org.luwrain.app.chat.im.*;

public class TelegramImpl
{
    /** Timeout milliseconds */
    final int TIMEOUT = 15000;

    /** Telegram Application hash */
    final String APIHASH = "62155226f23b8565aa3aaa0fa68df878";

    /** Telegram Application id */
    final Integer APIID = 97022;

    private final Config config;
    private final Events events;
    private TelegramApi api;
    private TLAuthorization auth;
    TLConfig tlconfig;
    TelegramAccount tAccount;
    private MemoryApiState memstate;

    TLCheckedPhone checked=null;

    enum Whatdo {signup,signin,migrate,none};

 
    private Whatdo whatdo;
    
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
    
    public TelegramImpl(Config config, Events events, TelegramAccount tAccount) 
    {
	NullCheck.notNull(config, "config");
	NullCheck.notNull(events, "events");
	this.config = config;
	this.tAccount=tAccount;
	this.events = events;
    }

    private boolean init()
    {
    memstate = new MemoryApiState("Telegram."+config.phone+".raw");
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
				tAccount.receiveNewMessage(updatesh.getMessage(),updatesh.getDate(),updatesh.getUserId()); 
			}
		}
	    });
	Log.debug("chat-telegram", "performing TLRequestHelpGetConfig()");
	try {
	    //Problem is here!!
	    tlconfig = api.doRpcCallNonAuth(new TLRequestHelpGetConfig(),TIMEOUT,2);
	    Log.debug("chat-telegram", "TLRequestHelpGetConfig() done");
	} catch (Exception e) 
	{
	    Log.error("chat-telegram", e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace();
	    events.onError(e.getClass() + ":" + e.getMessage());
	    return false;
	}
	memstate.updateSettings(tlconfig);
	memstate.setPrimaryDc(tlconfig.getThisDc());
	Log.debug("chat-telegram", "getThisDc() is " + tlconfig.getThisDc());
	//���������, ���� ���� � ����������� ����������� (��������� ��� ��� � hash)
	return true;
    }

    public void run()
    {
	whatdo = Whatdo.none;
	// getAuthKey
	// 3cc1b1a3763c2cad6815a5de0ffc5208e8cb6b04917d3aa88901985537edfd5d06841111785cea72a65db78f753cfe4803d5a50880cf29dd83a4a40b69a3478fea7740fe1782a945a56a49e80a8be2fcb86ed6cecc32b6ca83d46001e8e6f8ea16806c87d5c793b3e3088c598b158abdca6123fe6a915e579dc834a608ddb25456542c1e8f3290d96c12adbea2adfe7812e68dd7c9a741a1111b7e8445abc5de822abdbd9665e1c869e3ec055dce0460917785d7f8464716a50ed9a25510f51980b5cda420847ee37d7df442901330e8a03f90cd10f49e5694a3da11ccb245ac669e8c9725baae6398d8a529043624c913c2b00bf60684337165e37c5cf8c994
	// getUserId 197321144
	if (!init())
	{
	    Log.error("chat-telegram", "init failed");
	    return;
	    }
	Log.debug("chat-telegram", "init completed");

	String phoneHash = null;
	String phoneSms = null;
	try(FileReader reader = new FileReader("Telegram."+config.phone+".txt")) {
		final BufferedReader in = new BufferedReader(reader);
		phoneSms = in.readLine();
		phoneHash = in.readLine();
	    }
	catch(IOException e)
	{
	    Log.debug("chat-telegram", e.getClass().getName() + ":" + e.getMessage());
//	    if (tlconfig.getThisDc() == 1)
//		api.switchToDc(2); else
//		api.switchToDc(1);
	    whatdo=Whatdo.signin;
	    setState(State.REGISTERED);
	}

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

        TLRequestAuthCheckPhone authCheckPhone = new TLRequestAuthCheckPhone();
        authCheckPhone.setPhoneNumber(config.phone);
        try {
	    Log.debug("chat-telegram", "trying TLRequestAuthCheckPhone");
	    checked = api.doRpcCallNonAuth(authCheckPhone,TIMEOUT,api.getState().getPrimaryDc());
		Log.debug("chat-telegram", "authsendcode:" + checked.isPhoneRegistered());
		if (checked.isPhoneRegistered()==false)
		{
			whatdo = Whatdo.signup;
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
                    whatdo = Whatdo.signup;
                    setState(State.UNREGISTERED);
                } else 
if (e.getErrorTag().startsWith("PHONE_MIGRATE_")) 
{
                    destDC = Integer.parseInt(e.getErrorTag().substring("PHONE_MIGRATE_".length()));
                    whatdo=Whatdo.signin;
                    setState(State.REGISTERED);
                } else 
if (e.getErrorTag().startsWith("USER_MIGRATE_")) 
{
                    destDC = Integer.parseInt(e.getErrorTag().substring("USER_MIGRATE_".length()));
                    whatdo=Whatdo.migrate;
                } else 
{
    events.onError(e.getClass().getName() + ":" + e.getErrorCode() + ":" + e.getMessage());
    				return;
                }
		Log.debug("chat-telegram ", "whatdo :" + whatdo);
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

	Log.debug("chat-telegram", "switch(" + whatdo + ")");
        switch (whatdo)
        {
        	case signin:
        	case signup:
		    sign();
		    break;
        	case migrate:
			try {
				ExportAuthorization();
				ImportAuthorization();
			} catch (Exception e) 
{
				// TODO Auto-generated catch block
				e.printStackTrace();
events.onError(whatdo.name()+" "+e.getMessage());
				return;
			}
        		return;
        	case none:
        		final String code=phoneSms;
        		final String hash=phoneHash;
			try {
				signIn(code, hash);
			} 
catch (Exception e) 
{
				// TODO Auto-generated catch block
events.onError(whatdo.name()+" "+e.getMessage());
				return;
			}
events.onAuthFinish(); 
        		return;
        }
        	}

    private void sign()
    {
	//Log.debug("chat-telegram", "performing signUp()");
		// request twofactor auth
		TLRequestAuthSendCode authSendCode =  new TLRequestAuthSendCode();
        authSendCode.setPhoneNumber(config.phone);
        authSendCode.setApiHash(APIHASH);
        authSendCode.setApiId(APIID);
	TLSentCode sentCode=null;
	try {
	    Log.debug("chat-telegram", "trying RpcRequestAuthSendCode "+memstate.getPrimaryDc());
sentCode = api.doRpcCallNonAuth(authSendCode,TIMEOUT,memstate.getPrimaryDc());
	} 
	catch (Exception e1) 
	{
	    Log.error("chat-telegram", e1.getClass().getName() + ":" + e1.getMessage());
	    e1.printStackTrace();
	    events.onError(whatdo.name()+" "+e1.getMessage());
	    finish();
	    return;
	}
	final String answer = events.askTwoPassAuthCode("Enter the code from SMS:");
	if (answer == null || answer.trim().isEmpty())
	{
	    finish();
	return;
	}

		final TelegramImpl that = this;
		auth = null;
		 switch (whatdo)
	        {
	        	case signin:
			try {
				signIn(answer, sentCode.getPhoneCodeHash());
			} 
catch (Exception e) 
{
events.onError(whatdo.name()+" "+e.getMessage());
				return;
			}
			break;
	        	case signup:
	        		TLRequestAuthSignUp sign = new TLRequestAuthSignUp();
	            	sign.setFirstName(config.firstName);
	            	sign.setLastName(config.lastName);
	            	sign.setPhoneCode(answer);
	            	sign.setPhoneCodeHash(sentCode.getPhoneCodeHash());
	            	sign.setPhoneNumber(config.phone);
	            	try {
	        			auth = api.doRpcCallNonAuth(sign);
	        		} catch (RpcException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		} catch (TimeoutException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
	            	System.out.println("isTemporalSession "+auth.isTemporalSession()+" user "+auth.getUser().getId());
	    			break;
	        	case migrate:
	        		//TODO ������ ��� ������
	        		break;
	        }
		 setState(State.authorized);
		//������� ��� � hash ����������� � ����
		 try(FileWriter writer = new FileWriter("Telegram."+config.phone+".txt"))
	        {
	           // ������ ���� ������
	            writer.write(answer);
	            writer.append('\n');
	            writer.write(sentCode.getPhoneCodeHash());
	            writer.append('\n');
	            writer.flush();
	        }
	        catch(IOException ex)
{
             	            System.out.println(ex.getMessage());
	        } 
		System.out.println(
				"getAuthKey " + Hex.encodeHex(api.getState().getAuthKey(that.tlconfig.getThisDc())));
		System.out.println("getUserId " + api.getState().getUserId());
		events.onAuthFinish();
    }
    
	public TelegramApi getApi() 
{
		return api;
	}

	public Events getEvents() 
{
		return events;
	}

	public MemoryApiState getMemState() 
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
    /*
	public void twoPass(String code) 
{
    NullCheck.notNull(code, "code");
    Log.debug("chat-telegram", "twoPass(" + code + ")");
		final TelegramImpl that = this;
		auth = null;
		 switch (whatdo)
	        {
	        	case signin:
			try {
				signIn(code,that.sentCode.getPhoneCodeHash());
			} 
catch (Exception e) 
{
events.onError(whatdo.name()+" "+e.getMessage());
				return;
			}
			break;
	        	case signup:
	        		SignUp(code);
	    			break;
	        	case migrate:
	        		//TODO ������ ��� ������
	        		break;
	        }
		//������� ��� � hash ����������� � ����
		 try(FileWriter writer = new FileWriter("Telegram."+config.phone+".txt"))
	        {
	           // ������ ���� ������
	            writer.write(code);
	            writer.append('\n');
	            writer.write(sentCode.getPhoneCodeHash());
	            writer.append('\n');
	            writer.flush();
	        }
	        catch(IOException ex)
{
             	            System.out.println(ex.getMessage());
	        } 
		System.out.println(
				"getAuthKey " + Hex.encodeHex(api.getState().getAuthKey(that.tlconfig.getThisDc())));
		System.out.println("getUserId " + api.getState().getUserId());
		events.onAuthFinish();
	}
    */
	
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
			  that.events.onAuthFinish(); 
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
		TLRequestAuthSignIn sign = new TLRequestAuthSignIn();
		sign.setPhoneCode(code);
		sign.setPhoneCodeHash(hash);
		sign.setPhoneNumber(that.config.phone);
		try {
		    auth = api.doRpcCallNonAuth(sign, TIMEOUT, api.getState().getPrimaryDc());
		} 
catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} 
		setState(State.authorized);
		System.out.println("isTemporalSession " + auth.isTemporalSession());
	}
	
    /*
	private void SignUp(String code)
	{
		final TelegramImpl that = this;
		TLRequestAuthSignUp sign = new TLRequestAuthSignUp();
    	sign.setFirstName("dgfgd");
    	sign.setLastName("dfgdfg");
    	sign.setPhoneCode(code);
    	sign.setPhoneCodeHash(sentCode.getPhoneCodeHash());
    	sign.setPhoneNumber(that.config.phone);
    	try {
	    auth = api.doRpcCallNonAuth(sign, TIMEOUT, 2);
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("isTemporalSession "+auth.isTemporalSession()+" user "+auth.getUser().getId());
	}
    */

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
}
