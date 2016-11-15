package org.luwrain.app.chat.im.telegram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.binary.Hex;
import org.luwrain.app.chat.im.Messenger;
import org.luwrain.app.chat.im.Contact;
import org.luwrain.app.chat.im.Events;
import org.telegram.api.TLConfig;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLCheckedPhone;
import org.telegram.api.auth.TLExportedAuthorization;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.contact.TLContact;
import org.telegram.api.contacts.TLContacts;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.RpcException;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.functions.auth.TLRequestAuthCheckPhone;
import org.telegram.api.functions.auth.TLRequestAuthExportAuthorization;
import org.telegram.api.functions.auth.TLRequestAuthImportAuthorization;
import org.telegram.api.functions.auth.TLRequestAuthSendCode;
import org.telegram.api.functions.auth.TLRequestAuthSignIn;
import org.telegram.api.functions.auth.TLRequestAuthSignUp;
import org.telegram.api.functions.contacts.TLRequestContactsGetContacts;
import org.telegram.api.functions.help.TLRequestHelpGetConfig;
import org.telegram.api.updates.TLAbsUpdates;
import org.telegram.api.user.TLAbsUser;
import org.telegram.api.user.TLUser;
import org.telegram.bot.kernel.engine.MemoryApiState;
import org.telegram.tl.TLBytes;

public class TelegramImpl implements Messenger
{
	/** Timeout milliseconds */
	final int TIMEOUT = 30000;
	/** Telegram Application hash */
	final String APIHASH = "62155226f23b8565aa3aaa0fa68df878";
	/** Telegram Application id */
	final Integer APIID = 97022;

	private Config config;
	private Events events;
	private TelegramApi api;
	private TLAuthorization auth;
	TLConfig tlconfig;

	private MemoryApiState state;
	TLSentCode sentCode = null;
	TLCheckedPhone checked=null;
	
	enum Whatdo {signup,signin,migrate,none};
	private Whatdo whatdo;

	public TelegramImpl(Config config) {
		this.config = config;
	}

	public void go(final Events events) {
		final TelegramImpl that = this;
		this.events = events;
		state = new MemoryApiState("Telegram."+config.phone+".raw");
		whatdo=Whatdo.none;
		// getAuthKey
		// 3cc1b1a3763c2cad6815a5de0ffc5208e8cb6b04917d3aa88901985537edfd5d06841111785cea72a65db78f753cfe4803d5a50880cf29dd83a4a40b69a3478fea7740fe1782a945a56a49e80a8be2fcb86ed6cecc32b6ca83d46001e8e6f8ea16806c87d5c793b3e3088c598b158abdca6123fe6a915e579dc834a608ddb25456542c1e8f3290d96c12adbea2adfe7812e68dd7c9a741a1111b7e8445abc5de822abdbd9665e1c869e3ec055dce0460917785d7f8464716a50ed9a25510f51980b5cda420847ee37d7df442901330e8a03f90cd10f49e5694a3da11ccb245ac669e8c9725baae6398d8a529043624c913c2b00bf60684337165e37c5cf8c994
		// getUserId 197321144
		api = new TelegramApi(state, new AppInfo(97022, "console", "1.0", "1.0", "en"), new ApiCallback() {

			public void onAuthCancelled(TelegramApi api) {
				 System.out.println("*** DEBUG onAuthCancelled:"+api);
			}

			public void onUpdatesInvalidated(TelegramApi api) {
				 System.out.println("*** DEBUG onUpdatesInvalidated:"+api);
			}

			public void onUpdate(TLAbsUpdates updates) {
				 System.out.println("*** DEBUG onUpdate:"+updates);
			}
		});

		System.out.println("get config");
		try {
			tlconfig = api.doRpcCallNonAuth(new TLRequestHelpGetConfig());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			that.events.onError(e.getMessage());
			return;
		}

		state.updateSettings(tlconfig);
		state.setPrimaryDc(tlconfig.getThisDc());
		//���������, ���� ���� � ����������� ����������� (��������� ��� ��� � hash)
		String phoneHash= null;
		String phoneSms = null;
		 try(FileReader reader = new FileReader("Telegram."+config.phone+".txt"))
	        {
	           // ������ �����������
			 BufferedReader in = new BufferedReader(reader);
			 phoneSms=in.readLine();
			 phoneHash=in.readLine();
	        }
	        catch(IOException ex){
	           //��������� ������, ���� �������� ������ DC, ���� ��������� ��� �����������  
	           if (tlconfig.getThisDc()==1)
	         
	        	   api.switchToDc(2);
	           
	           else
		        	   api.switchToDc(1);
	        	   
	        }   
		//api.switchToDc(1);
		System.out.println("tlconfig.getThisDc " + tlconfig.getThisDc());


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
		// request twofactor auth

		
		TLRequestAuthSendCode m= new TLRequestAuthSendCode();
        m.setPhoneNumber(that.config.phone);
        m.setApiHash("62155226f23b8565aa3aaa0fa68df878");
        m.setApiId(97022);
        sentCode = null;
        TLRequestAuthCheckPhone cm=new TLRequestAuthCheckPhone();
        cm.setPhoneNumber(that.config.phone);
        try {
        	checked=api.doRpcCallNonAuth(cm);
        	System.out.println("authsendcode "+checked.getClassId());
        } catch (RpcException e) {
        	System.out.println("e.getErrorCode() "+e.getMessage());
            if (e.getErrorCode() == 303) {
                int destDC;
                if (e.getErrorTag().startsWith("NETWORK_MIGRATE_")) {
                    destDC = Integer.parseInt(e.getErrorTag().substring("NETWORK_MIGRATE_".length()));
                    whatdo=Whatdo.signup;
                } else if (e.getErrorTag().startsWith("PHONE_MIGRATE_")) {
                    destDC = Integer.parseInt(e.getErrorTag().substring("PHONE_MIGRATE_".length()));
                    whatdo=Whatdo.signin;
                } else if (e.getErrorTag().startsWith("USER_MIGRATE_")) {
                    destDC = Integer.parseInt(e.getErrorTag().substring("USER_MIGRATE_".length()));
                    whatdo=Whatdo.migrate;
                } else {
                	that.events.onError(e.getMessage());
    				return;
                }
                System.out.println("whatdo "+whatdo);
                api.switchToDc(destDC);
                //phone = "99966"+destDC+"2345";
                System.out.println("destDC="+destDC);
            } else {
            	that.events.onError(e.getMessage());
				return;
            }
        }
        catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        switch (whatdo)
        {
        	case signin:
        	case signup:
        		try {
					sentCode = api.doRpcCallNonAuth(m);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					that.events.onError(whatdo.name()+" "+e1.getMessage());
					return;
				}
        		that.events.on2PassAuth("input code from sms");
        		return;
        	case migrate:
			try {
				ExportAuthorization();
				ImportAuthorization();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				that.events.onError(whatdo.name()+" "+e.getMessage());
				return;
			}
        		return;
        	case none:
        		//TODO ������ ��� ������ ��� ���
        		String code=phoneSms;
        		String hash=phoneHash;
			try {
				SignIn(code,hash);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				that.events.onError(whatdo.name()+" "+e.getMessage());
				return;
			}
        		that.events.onAuthFinish(); 
        		return;
        }
        
	}

	public TelegramApi getApi() {
		return api;
	}

	public Events getEvents() {
		return events;
	}

	public MemoryApiState getState() {
		return state;
	}

	public void twoPass(String code) {
		final TelegramImpl that = this;
		auth = null;
		// TODO: ������ ����� doRpcCallNonAuth ������� �����������
			
		 switch (whatdo)
	        {
	        	case signin:
			try {
				SignIn(code,that.sentCode.getPhoneCodeHash());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				that.events.onError(whatdo.name()+" "+e.getMessage());
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
	            writer.write(that.sentCode.getPhoneCodeHash());
	            writer.append('\n');
	            writer.flush();
	        }
	        catch(IOException ex){
	             
	            System.out.println(ex.getMessage());
	        } 
		System.out.println(
				"getAuthKey " + Hex.encodeHex(api.getState().getAuthKey(that.tlconfig.getThisDc())));
		System.out.println("getUserId " + api.getState().getUserId());
		that.events.onAuthFinish();
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
			  that.events.onError(e.getMessage());
				throw e;
		  } 
		  try {
			  System.out.println("1"); 
			  auth=api.doRpcCallNonAuth(impauth);
			  System.out.println("2"); 
			  state.doAuth(auth); 
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
		TLRequestAuthExportAuthorization expauth = new TLRequestAuthExportAuthorization();
		expauth.setDcId(tlconfig.getThisDc());
		try {
			TLExportedAuthorization eauth = api.doRpcCallNonAuth(expauth);
			System.out.println("getId "+eauth.getId());
			System.out.println("getBytes "+new String(Hex.encodeHex(eauth.getBytes().getData())));

		} catch (Exception e1) {
			System.out.println("error TLRequestAuthExportAuthorization");
			e1.printStackTrace();
			throw e1;
		}
	}
	
	private void SignIn(String code,String hash) throws Exception
	{
		final TelegramImpl that = this;
		System.out.println("signin");
		TLRequestAuthSignIn sign = new TLRequestAuthSignIn();
		sign.setPhoneCode(code);
		sign.setPhoneCodeHash(hash);
		sign.setPhoneNumber(that.config.phone);
		try {
			auth = api.doRpcCallNonAuth(sign);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} 
		System.out.println("isTemporalSession " + auth.isTemporalSession());
	}
	
	private void SignUp(String code)
	{
		final TelegramImpl that = this;
		TLRequestAuthSignUp sign = new TLRequestAuthSignUp();
    	sign.setFirstName("dgfgd");
    	sign.setLastName("dfgdfg");
    	sign.setPhoneCode("45612");
    	sign.setPhoneCodeHash(sentCode.getPhoneCodeHash());
    	sign.setPhoneNumber(that.config.phone);
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
	}
	public void checkContacts(String q) {
		TLRequestContactsGetContacts cntcs = new TLRequestContactsGetContacts();
		cntcs.setHash("");
		TLContacts rescnts;
		try {
			System.out.println("getSeachContact "+getApi().getState().getUserId());
			rescnts = (TLContacts) getApi().doRpcCallNonAuth(cntcs);
			System.out.println("contacts users " + rescnts.getUsers().size());
			System.out.println("contacts result " + rescnts.getContacts().size());
			for(TLAbsUser o:rescnts.getUsers())
			{
				TLUser u=(TLUser)o;
				TelegramContactImpl contact=new TelegramContactImpl(){};
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

	@Override public void finish()
	{
		api.close();
		
	}
}
