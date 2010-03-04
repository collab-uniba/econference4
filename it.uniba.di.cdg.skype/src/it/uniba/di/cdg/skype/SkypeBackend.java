package it.uniba.di.cdg.skype;

import it.uniba.di.cdg.skype.action.SkypeCallAction;
import it.uniba.di.cdg.skype.action.SkypeChatServiceAction;
import it.uniba.di.cdg.skype.action.SkypeMultiCallAction;
import it.uniba.di.cdg.skype.action.SkypeMultiChatServiceAction;
import it.uniba.di.cdg.skype.util.ExtensionConstants;
import it.uniba.di.cdg.skype.util.XmlUtil;
import it.uniba.di.cdg.xcore.m2m.events.InvitationEvent;
import it.uniba.di.cdg.xcore.network.BackendException;
import it.uniba.di.cdg.xcore.network.IBackend;
import it.uniba.di.cdg.xcore.network.INetworkBackendHelper;
import it.uniba.di.cdg.xcore.network.ServerContext;
import it.uniba.di.cdg.xcore.network.UserContext;
import it.uniba.di.cdg.xcore.network.action.ICallAction;
import it.uniba.di.cdg.xcore.network.action.IChatServiceActions;
import it.uniba.di.cdg.xcore.network.action.IMultiCallAction;
import it.uniba.di.cdg.xcore.network.action.IMultiChatServiceActions;
import it.uniba.di.cdg.xcore.network.events.BackendStatusChangeEvent;
import it.uniba.di.cdg.xcore.network.events.IBackendEvent;
import it.uniba.di.cdg.xcore.network.events.call.CallEvent;
import it.uniba.di.cdg.xcore.network.events.chat.ChatComposingtEvent;
import it.uniba.di.cdg.xcore.network.events.chat.ChatExtensionProtocolEvent;
import it.uniba.di.cdg.xcore.network.events.chat.ChatMessageReceivedEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatExtensionProtocolEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatInvitationDeclinedEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatMessageEvent;
import it.uniba.di.cdg.xcore.network.messages.IMessage;
import it.uniba.di.cdg.xcore.network.messages.SystemMessage;
import it.uniba.di.cdg.xcore.network.model.IBuddyRoster;
import it.uniba.di.cdg.xcore.network.services.ICapabilities;
import it.uniba.di.cdg.xcore.network.services.ICapability;
import it.uniba.di.cdg.xcore.network.services.INetworkService;
import it.uniba.di.cdg.xcore.network.services.INetworkServiceContext;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.skype.Call;
import com.skype.CallListener;
import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ChatMessageListener;
import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

public class SkypeBackend implements IBackend {

	private INetworkBackendHelper helper;
	private SkypeBuddyRoster skypeBuddyRoster;
	private SkypeMultiChatServiceAction skypeMultiChatServiceAction;
	private SkypeCallAction skypeCallAction;
	private SkypeMultiCallAction skypeMultiCallAction;
	private boolean connected = false;
	
	static{
		Connector.useJNIConnector(true);
	}
	
	int count = 0;
	
	private ChatMessageListener chatMessageListener =  new ChatMessageListener() {
		
		@Override
		public void chatMessageSent(ChatMessage arg0) throws SkypeException {
		}
		
		@Override
		public void chatMessageReceived(ChatMessage chatMessage) throws SkypeException {
			processMessageReceived(chatMessage.getContent(), chatMessage.getSenderId(),
					chatMessage.getSenderDisplayName(), chatMessage.getChat());			
		}
	};
	
	public void processMessageReceived(String content, String senderId, String senderName, Chat chat) throws SkypeException{
		
		if (content.equals(""))
			return;
		
		//is Skype internal message
		if(XmlUtil.isSkypeXmlMessage(content))
			return;
		
		String extensionName = XmlUtil.extansionName(content);
		//� presente un estensione al protocollo
		if(extensionName != null){
			if(XmlUtil.chatType(content).equals(ExtensionConstants.ONE_TO_ONE)) // chat one2one
			{
				if(extensionName.equals(ExtensionConstants.CHAT_COMPOSING)){
					IBackendEvent event = new ChatComposingtEvent(
							senderId, getBackendId());
					getHelper().notifyBackendEvent(event);
				}
				
				else if(extensionName.equals(ExtensionConstants.ROOM_INVITE)){
					IMessage m = new SystemMessage("");
					HashMap<String, String> param = XmlUtil.readXmlExtension(content);
					String reason = param.get(ExtensionConstants.REASON);
					if (reason == null) reason = "";
					String roomId = chat.getId();
					
					skypeMultiChatServiceAction.putWaitingRoom(chat.getId(), senderId);
					IBackendEvent event = new InvitationEvent( getBackendId(), roomId,
							senderId, reason, "", m);
					getHelper().notifyBackendEvent(event);
				}
				
				else if(extensionName.equals(ExtensionConstants.ROOM_INVITE_ACCEPT)){
					skypeMultiChatServiceAction.sendChatRoom(senderId);
				}
				
				else if(extensionName.equals(ExtensionConstants.ROOM_INVITE_DECLINE)){
					HashMap<String, String> param = XmlUtil.readXmlExtension(content);
					String reason = param.get(ExtensionConstants.REASON);
					if (reason == null) reason = "";
					IBackendEvent event = new MultiChatInvitationDeclinedEvent(
							senderId, reason, getBackendId());
					getHelper().notifyBackendEvent(event);
				}
				
				else if(extensionName.equals(ExtensionConstants.CHAT_MESSAGE)){
					HashMap<String, String> param = XmlUtil.readXmlExtension(content);
					String msg = param.get(ExtensionConstants.MESSAGE);
					IBackendEvent event = new ChatMessageReceivedEvent(
							getRoster().getBuddy(senderId), msg, getBackendId());
					getHelper().notifyBackendEvent(event);
				}
				
				//� un estensione gestita dal core
				else{
					HashMap<String, String> param;
					param = XmlUtil.readXmlExtension(content);
					IBackendEvent event = new ChatExtensionProtocolEvent(
							senderId, extensionName, param, getBackendId());
					getHelper().notifyBackendEvent(event);
				}
			}else{ //chat m2m
				
				if(extensionName.equals(ExtensionConstants.CHAT_ROOM)){
					skypeMultiChatServiceAction.updateChatRoom(chat);
				}
				
				else if(extensionName.equals(ExtensionConstants.CHAT_MESSAGE)){
					HashMap<String, String> param = XmlUtil.readXmlExtension(content);
					String msg = param.get(ExtensionConstants.MESSAGE);
					IBackendEvent event = new MultiChatMessageEvent(
							getBackendId(), msg, getUserId());
					getHelper().notifyBackendEvent(event);
				}
				
				else{
					HashMap<String, String> param;
					param = XmlUtil.readXmlExtension(content);
					IBackendEvent event = new MultiChatExtensionProtocolEvent(
							param, extensionName, getBackendId(), senderId);
					getHelper().notifyBackendEvent(event);
				}
			}
		}
		
		//� un normale messaggio di testo
		else{
			
			if(XmlUtil.chatType(content).equals(ExtensionConstants.ONE_TO_ONE)){ // chat one2one
				IBackendEvent event = new ChatMessageReceivedEvent(
						getRoster().getBuddy(senderId), content, getBackendId());
				getHelper().notifyBackendEvent(event);
			}else{
				IBackendEvent event = new MultiChatMessageEvent(
						getBackendId(), content, senderName);
				getHelper().notifyBackendEvent(event);
			}
		}
	}
	
	private CallListener callListener = new CallListener() {
		
		@Override
		public void callReceived(Call call) throws SkypeException {
			if(call.getParticipantsCount().equals("2")){
				IBackendEvent event = new CallEvent(getBackendId(), call.getPartnerId());
				getHelper().notifyBackendEvent(event);
				skypeCallAction.addCall(call.getPartnerId(), call);
			}else{
				IBackendEvent event = new CallEvent(getBackendId(), call.getConferenceId());
				getHelper().notifyBackendEvent(event);
				skypeCallAction.addCall(call.getConferenceId(), call);
				skypeMultiCallAction.addCall(call.getConferenceId(), call);
			}
		}
		
		@Override
		public void callMaked(Call arg0) throws SkypeException {
			// TODO Auto-generated method stub
		}
	};
	
	 public SkypeBackend getBackendFromProxy(){
	    	return this;
	    }
	 
    /**
     * This backend's unique id.
     */
	public static final String ID = "it.uniba.di.cdg.skype.skypeBackend";

	
	public SkypeBackend() {
		super();
		skypeBuddyRoster = new SkypeBuddyRoster(this);
		skypeMultiChatServiceAction = new SkypeMultiChatServiceAction(this);
		skypeCallAction = new SkypeCallAction();
		skypeMultiCallAction = new SkypeMultiCallAction(this);
	}

	@Override
	public void changePassword(String newpasswd) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void connect(ServerContext ctx, UserContext userAccount)
			throws BackendException {
		
		Connector.Status status = null;
		
		Connector conn = Connector.getInstance();
		
		try {
			status = conn.connect();
		} catch (ConnectorException e1) {
			e1.printStackTrace();
			throw new BackendException(e1.getMessage());
		}

		if(status != Connector.Status.ATTACHED)
			throw new BackendException(new Exception("E' necessario installare ed avviare Skype\nE' possibile scaricarlo da www.skype.com"));
	
		//notifico l'avvenuta connessione
		helper.notifyBackendEvent(new BackendStatusChangeEvent( ID, true ));
		
		//notifico l'aggiornamento del roster
		skypeBuddyRoster.updateRoster();
		
		//aggiungo i listeners di Skype4Java
		try {
			Skype.addChatMessageListener(chatMessageListener);
			Skype.addCallListener(callListener);
		} catch (SkypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		connected = true;
	}

	@Override
	public INetworkService createService(ICapability service,
			INetworkServiceContext context) throws BackendException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnect() {
		//notifico la disconnessione del roster
		skypeBuddyRoster.disconnectRoster();
		
		//notifico l'avvenuta disconnessione
		helper.notifyBackendEvent(new BackendStatusChangeEvent( ID, false ));
	
		//rimuovo i listeners di Skype4Java
		Skype.removeChatMessageListener(chatMessageListener);
		
		connected = false;
	}

	@Override
	public ICapabilities getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Job getConnectJob() {
        final Job connectJob = new Job( "Connecting ..." ) {
            @Override
            protected IStatus run( IProgressMonitor monitor ) {
                try {
                    connect( null, null );
                } catch (BackendException e) {
                    e.printStackTrace();
                    return Status.CANCEL_STATUS;
                }
                return Status.OK_STATUS;
            }
        };
        return connectJob;
	}

	@Override
	public INetworkBackendHelper getHelper() {
		return this.helper;
	}

	@Override
	public IBuddyRoster getRoster() {
		// TODO da sistemare
		return skypeBuddyRoster;
	}

	@Override
	public ServerContext getServerContext() {
		return new ServerContext("Skype", true, false, 0, "Skype");
	}

	@Override
	public UserContext getUserAccount() {
		// da sistemare
		UserContext userContect = new UserContext(getUserId(),"");
		try {
			userContect.setName(Skype.getProfile().getFullName());
		} catch (SkypeException e) {
			e.printStackTrace();
		}
		return userContect;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void setHelper(INetworkBackendHelper helper) {
		this.helper = helper;
	}

	@Override
	public String getBackendId() {
		return ID;
	}

	@Override
	public IChatServiceActions getChatServiceAction() {
		return new SkypeChatServiceAction();
	}

	@Override
	public IMultiChatServiceActions getMultiChatServiceAction() {
		return skypeMultiChatServiceAction;
	}

	@Override
	public String getUserId() {
		try {
			return Skype.getProfile().getId();
		} catch (SkypeException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ICallAction getCallAction() {
		return skypeCallAction;
	}

	@Override
	public IMultiCallAction getMultiCallAction() {
		return skypeMultiCallAction;
	}

	@Override
	public void registerNewAccount(String userId, String password,
			ServerContext server, Map<String, String> attributes)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
