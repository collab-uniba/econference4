/**
 * This file is part of the eConference project and it is distributed under the 
 * terms of the MIT Open Source license.
 * 
 * The MIT License
 * Copyright (c) 2005 Collaborative Development Group - Dipartimento di Informatica, 
 *                    University of Bari, http://cdg.di.uniba.it
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, 
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies 
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package it.uniba.di.cdg.skype;

import it.uniba.di.cdg.skype.action.SkypeCallAction;
import it.uniba.di.cdg.skype.action.SkypeChatServiceAction;
import it.uniba.di.cdg.skype.action.SkypeMultiCallAction;
import it.uniba.di.cdg.skype.action.SkypeMultiChatServiceAction;
import it.uniba.di.cdg.skype.recorder.win32.FreeRecorder;
import it.uniba.di.cdg.skype.util.ExtensionConstants;
import it.uniba.di.cdg.skype.util.XmlUtil;
import it.uniba.di.cdg.xcore.m2m.events.InvitationEvent;
import it.uniba.di.cdg.xcore.network.BackendException;
import it.uniba.di.cdg.xcore.network.IBackend;
import it.uniba.di.cdg.xcore.network.INetworkBackendHelper;
import it.uniba.di.cdg.xcore.network.IUserStatus;
import it.uniba.di.cdg.xcore.network.ServerContext;
import it.uniba.di.cdg.xcore.network.UserContext;
import it.uniba.di.cdg.xcore.network.action.ICallAction;
import it.uniba.di.cdg.xcore.network.action.IChatServiceActions;
import it.uniba.di.cdg.xcore.network.action.IMultiCallAction;
import it.uniba.di.cdg.xcore.network.action.IMultiChatServiceActions;
import it.uniba.di.cdg.xcore.network.events.BackendStatusChangeEvent;
import it.uniba.di.cdg.xcore.network.events.IBackendEvent;
import it.uniba.di.cdg.xcore.network.events.call.CallEvent;
import it.uniba.di.cdg.xcore.network.events.chat.ChatComposingEvent;
import it.uniba.di.cdg.xcore.network.events.chat.ChatExtensionProtocolEvent;
import it.uniba.di.cdg.xcore.network.events.chat.ChatMessageReceivedEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatComposingEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatExtensionProtocolEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatInvitationDeclinedEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatMessageEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatUserLeftEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatVoiceGrantedEvent;
import it.uniba.di.cdg.xcore.network.events.multichat.MultiChatVoiceRevokedEvent;
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

	/**
	 * This backend's unique id.
	 */
	public static final String ID = "it.uniba.di.cdg.skype.skypeBackend";
	//private static final String RECORDER_ID = "it.uniba.di.cdg.skype.recorder";


	private INetworkBackendHelper helper;
	private SkypeBuddyRoster skypeBuddyRoster;
	private SkypeMultiChatServiceAction skypeMultiChatServiceAction;
	private SkypeCallAction skypeCallAction;
	private SkypeMultiCallAction skypeMultiCallAction;
	private boolean connected = false;

	static {
		Connector.useJNIConnector(true);
	}

	int count = 0;

	private ChatMessageListener chatMessageListener = new ChatMessageListener() {

		@Override
		public void chatMessageSent(ChatMessage arg0) throws SkypeException {
		}

		@Override
		public void chatMessageReceived(ChatMessage chatMessage)
		throws SkypeException {
			processMessageReceived(chatMessage.getContent(),
					chatMessage.getSenderId(),
					chatMessage.getSenderDisplayName(), chatMessage.getChat());
		}
	};

	public void processMessageReceived(String content, String senderId,
			String senderName, Chat chat) throws SkypeException {

		if (content.equals(""))
			return;

		// is Skype internal message
		if (XmlUtil.isSkypeXmlMessage(content))
			return;

		String extensionName = XmlUtil.extensionName(content);
		// � presente un estensione al protocollo
		if (extensionName != null) {
			if (XmlUtil.chatType(content).equals(ExtensionConstants.ONE_TO_ONE)) // chat
				// one2one
			{
				if (extensionName.equals(ExtensionConstants.CHAT_COMPOSING)) {
					IBackendEvent event = new ChatComposingEvent(senderId,
							getBackendId());
					getHelper().notifyBackendEvent(event);
				}

				else if (extensionName.equals(ExtensionConstants.ROOM_INVITE)) {					
					HashMap<String, String> param = XmlUtil
					.readXmlExtension(content);
					String reason = param.get(ExtensionConstants.REASON);
					if (reason == null)
						reason = "";
					String roomId = chat.getId();

					skypeMultiChatServiceAction.putWaitingRoom(chat.getId(),
							senderId);
					IBackendEvent event = new InvitationEvent(getBackendId(),
							roomId, senderId, "schedule n/a", reason, "");
					getHelper().notifyBackendEvent(event);
				}

				else if (extensionName
						.equals(ExtensionConstants.ROOM_INVITE_ACCEPT)) {
					skypeMultiChatServiceAction.sendChatRoom(senderId);
					if(skypeMultiChatServiceAction.getModerator().equals(getUserId())){
						HashMap<String, String> param = new HashMap<String, String>();
						param.put(ExtensionConstants.USER, getUserId());
						skypeMultiChatServiceAction.SendExtensionProtocolMessage(ExtensionConstants.MODERATOR, param);
					}
				}

				else if (extensionName
						.equals(ExtensionConstants.ROOM_INVITE_DECLINE)) {
					HashMap<String, String> param = XmlUtil
					.readXmlExtension(content);
					String reason = param.get(ExtensionConstants.REASON);
					if (reason == null)
						reason = "";
					IBackendEvent event = new MultiChatInvitationDeclinedEvent(
							senderId, reason, getBackendId());
					getHelper().notifyBackendEvent(event);
				}

				else if (extensionName.equals(ExtensionConstants.CHAT_MESSAGE)) {
					HashMap<String, String> param = XmlUtil
					.readXmlExtension(content);
					String msg = param.get(ExtensionConstants.MESSAGE);
					IBackendEvent event = new ChatMessageReceivedEvent(
							getRoster().getBuddy(senderId), msg, getBackendId());
					getHelper().notifyBackendEvent(event);
				}

				else if (extensionName.equals(ExtensionConstants.CALL_FINISHED)) {
					if(getMultiCallAction().isCalling())
						getMultiCallAction().finishCall();
				}

				// � un estensione gestita dal core
				else {
					HashMap<String, String> param;
					param = XmlUtil.readXmlExtension(content);
					IBackendEvent event = new ChatExtensionProtocolEvent(
							senderId, extensionName, param, getBackendId());
					getHelper().notifyBackendEvent(event);
				}
			} else { // chat m2m

				if (extensionName.equals(ExtensionConstants.CHAT_ROOM)) {
					skypeMultiChatServiceAction.updateChatRoom(chat);
				}
				else if (extensionName.equals(ExtensionConstants.CHAT_COMPOSING)) {
					IBackendEvent event = new MultiChatComposingEvent(senderId, getBackendId());
					getHelper().notifyBackendEvent(event);
				}
				else if (extensionName.equals(ExtensionConstants.PRESENCE_MESSAGE)) {
					HashMap<String, String> param = XmlUtil
					.readXmlExtension(content);
					String type = param.get(ExtensionConstants.PRESENCE_TYPE);
					if (ExtensionConstants.PRESENCE_UNAVAILABLE.equals(type)) {
						System.out.println("Received presence unvailable update");
						IBackendEvent event = new MultiChatUserLeftEvent(getBackendId(), senderId, senderName);
						getHelper().notifyBackendEvent(event);
					}
				}
				else if (extensionName.equals(ExtensionConstants.CHAT_MESSAGE)) {
					HashMap<String, String> param = XmlUtil
					.readXmlExtension(content);
					String msg = param.get(ExtensionConstants.MESSAGE);
					IBackendEvent event = new MultiChatMessageEvent(
							getBackendId(), msg, senderId);
					getHelper().notifyBackendEvent(event);
				}

				else if (extensionName.equals(ExtensionConstants.REVOKE_VOICE)){
					HashMap<String, String> param = XmlUtil
					.readXmlExtension(content);
					String userId = param.get(ExtensionConstants.USER);
					IBackendEvent event = new MultiChatVoiceRevokedEvent(
							getBackendId(), userId);
					getHelper().notifyBackendEvent(event);
				}

				else if (extensionName.equals(ExtensionConstants.GRANT_VOICE)){
					HashMap<String, String> param = XmlUtil
					.readXmlExtension(content);
					String userId = param.get(ExtensionConstants.USER);
					IBackendEvent event = new MultiChatVoiceGrantedEvent(
							getBackendId(), userId);
					getHelper().notifyBackendEvent(event);
				}

				else if (extensionName.equals(ExtensionConstants.MODERATOR)){
					HashMap<String, String> param = XmlUtil
					.readXmlExtension(content);
					String user = param.get(ExtensionConstants.USER);
					skypeMultiChatServiceAction.setModerator(user);
					skypeMultiChatServiceAction.updateChatRoom(chat);
				}

				// � un estensione gestita dal core
				else {
					HashMap<String, String> param;
					param = XmlUtil.readXmlExtension(content);
					IBackendEvent event = new MultiChatExtensionProtocolEvent(
							param, extensionName, getBackendId(), senderId);
					getHelper().notifyBackendEvent(event);
				}
			}
		}

		// it's just a regular skype text-based msg
		else {
			String chatMsg = null;
			if(XmlUtil.isSkypeXmlMessage(content))
				chatMsg = XmlUtil.chatType(content);
			// we assume it's always a one2one chat in case we get a regular msg
			if (null == chatMsg // null means a regular message has no extensions
					|| chatMsg.equals(ExtensionConstants.ONE_TO_ONE)) {
				IBackendEvent event = new ChatMessageReceivedEvent(getRoster()
						.getBuddy(senderId), content, getBackendId());
				getHelper().notifyBackendEvent(event);
			} else {
				// check: this else would probably be never reached
				IBackendEvent event = new MultiChatMessageEvent( // chat m2m
						getBackendId(), content, senderName);
				getHelper().notifyBackendEvent(event);
			}
		}
	}

	private CallListener callListener = new CallListener() {

		@Override
		public void callReceived(Call call) throws SkypeException {
			if (call.getParticipantsCount().equals("2")) {
				IBackendEvent event = new CallEvent(getBackendId(),
						call.getPartnerId());
				getHelper().notifyBackendEvent(event);
				skypeCallAction.addCall(call.getPartnerId(), call);
			} else {
				IBackendEvent event = new CallEvent(getBackendId(),
						call.getConferenceId());
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

	public SkypeBackend getBackendFromProxy() {
		return this;
	}

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

		if (status != Connector.Status.ATTACHED)
			throw new BackendException(
					new Exception(
					"You have to install and run Skype before running eConference.\nPlease download  Skype from www.skype.com"));

		// aggiungo i listeners di Skype4Java
		try {
			Skype.addChatMessageListener(chatMessageListener);
			Skype.addCallListener(callListener);

			Skype.getProfile().setStatus(com.skype.Profile.Status.ONLINE);
		} catch (SkypeException e) {
			e.printStackTrace();
		}

		// notifico l'avvenuta connessione
		helper.notifyBackendEvent(new BackendStatusChangeEvent(ID, true));

		// notifico l'aggiornamento del roster
		skypeBuddyRoster.reload();

		connected = true;
		
		//runRecorderExtension();
		FreeRecorder rec = new FreeRecorder();
		rec.recorderStartConfirmDialog();
		
	}

	/*
	private void runRecorderExtension() {		
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(RECORDER_ID);
		try {
			for (IConfigurationElement e : config) {
				System.out.println("Evaluating extension skype recorder");
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ISkypeRecorder) {
					ISafeRunnable runnable = new ISafeRunnable() {
						@Override
						public void handleException(Throwable exception) {
							System.out.println("Exception in extension skype recorder");
						}
						@Override
						public void run() throws Exception {
							((ISkypeRecorder) o).recorderStartConfirmDialog();						
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}
	}
*/
	
	@Override
	public INetworkService createService(ICapability service,
			INetworkServiceContext context) throws BackendException {
		return null;
	}

	@Override
	public void disconnect() {
		// notifico la disconnessione del roster
		skypeBuddyRoster.disconnectRoster();

		// notifico l'avvenuta disconnessione
		helper.notifyBackendEvent(new BackendStatusChangeEvent(ID, false));

		// rimuovo i listeners di Skype4Java
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
		final Job connectJob = new Job("Connecting ...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					connect(null, null);
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
		UserContext userContect = new UserContext(getUserId(), "");

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
	}

	@Override
	public void setUserStatus(int status) {

		switch (status){
		case IUserStatus.AVAILABLE:
			try {
				Skype.getProfile().setStatus(com.skype.Profile.Status.ONLINE);
			} catch (SkypeException e) {

				e.printStackTrace();
			}
			break;

		case IUserStatus.AWAY:
			try {
				Skype.getProfile().setStatus(com.skype.Profile.Status.AWAY);
			} catch (SkypeException e) {

				e.printStackTrace();
			}
			break;

		case IUserStatus.BUSY:
			try {
				Skype.getProfile().setStatus(com.skype.Profile.Status.DND);
			} catch (SkypeException e) {

				e.printStackTrace();
			}
			break;

		case IUserStatus.OFFLINE:
			try {
				Skype.getProfile().setStatus(com.skype.Profile.Status.OFFLINE);

				disconnect();
			} catch (SkypeException e) {

				e.printStackTrace();
			}
			break;
		}


	}

}
