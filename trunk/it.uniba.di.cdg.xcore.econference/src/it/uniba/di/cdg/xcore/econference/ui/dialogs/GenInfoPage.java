package it.uniba.di.cdg.xcore.econference.ui.dialogs;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Proxy;
import java.util.Iterator;

import javax.swing.JOptionPane;


import org.eclipse.swt.widgets.DateTime;
//import java.util.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import it.uniba.di.cdg.jabber.JabberBackend;
import it.uniba.di.cdg.xcore.econference.EConferenceContext;
import it.uniba.di.cdg.xcore.econference.model.IItemList;
import it.uniba.di.cdg.xcore.econference.model.internal.DiscussionItem;
import it.uniba.di.cdg.xcore.econference.model.internal.ItemList;
import it.uniba.di.cdg.xcore.network.BackendException;
import it.uniba.di.cdg.xcore.network.IBackend;
import it.uniba.di.cdg.xcore.network.NetworkPlugin;
import it.uniba.di.cdg.xcore.network.ServerContext;
import it.uniba.di.cdg.xcore.network.UserContext;
import it.uniba.di.cdg.xcore.ui.UiPlugin;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;


public class GenInfoPage extends WizardPage implements Listener {	
	Composite composite;
	EConferenceContext context;
    private Combo backendIdCombo = null;
    private Text nameConferenceText = null;
    GridData gd;
    private String nick;
    private String password;
    private Text topicText = null;
    private Text itemText = null;
    private Text nickNameText = null;
    private Combo serviceCombo = null;
    private Text serviceText = null; 
    String media = "";
    private DateTime schedule;
    
    
	protected GenInfoPage(String arg0) {
		super(arg0);
		setTitle("General conference info");
		setDescription("Step 1: Enter conference's Information\n Fields marked with * are required");
		context = new EConferenceContext();
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

	    // create the composite to hold the widgets
		composite = new Composite(parent, SWT.NONE);
	    GridLayout gridLayout = new GridLayout(3,true);		
		composite.setLayout(gridLayout);
		new CLabel(composite, SWT.NONE).setText("Conference chair: *");
        nickNameText = new Text(composite, SWT.BORDER);
        nickNameText.setLayoutData(new GridData(143, 15));
        try{
        	nickNameText.setText(NetworkPlugin.getDefault().getRegistry().getBackend( 
            "it.uniba.di.cdg.jabber.jabberBackend" ).getUserAccount().getId());
        	media = "it.uniba.di.cdg.jabber.jabberBackend";
        }
        catch(NullPointerException e){
        	media = "it.uniba.di.cdg.skype.skypeBackend";
        }
        nickNameText.setText(NetworkPlugin.getDefault().getRegistry().getBackend(media).getUserAccount().getId());
		((InviteWizard)this.getWizard()).setMedia(media);
        new CLabel(composite, SWT.NONE).setText("");
		new CLabel(composite, SWT.NONE).setText("Media: ");
		backendIdCombo = new Combo(composite, SWT.READ_ONLY);
		backendIdCombo.setLayoutData(new GridData(129, 15));
		String[] item = {"Text only (XMPP/GTalk)","Audio (Skype)"};
		backendIdCombo.setItems(item);
		
		backendIdCombo.addSelectionListener(new org.eclipse.swt.events.SelectionListener(){
	    	@Override
	    	public void widgetDefaultSelected(SelectionEvent e) {}
	    	@Override 
	    	public void widgetSelected(SelectionEvent e) {
	    		if (!backendIdCombo.getText().equals(""))
	    			serviceCombo.setEnabled(true);
	    		// TODO Auto-generated method stub
	    		if (backendIdCombo.getText().equals("Text only (XMPP/GTalk)") || backendIdCombo.getText().equals(""))
	    			media = "it.uniba.di.cdg.jabber.jabberBackend";
		    	else
		    		media = "it.uniba.di.cdg.skype.skypeBackend";
	    	}
		});
		new CLabel(composite,SWT.NONE);
		new CLabel(composite, SWT.NONE).setText("Conference service: *");
		serviceCombo = new Combo(composite, SWT.READ_ONLY);
	    String items[] = {"ugres.di.uniba.it","0nl1ne.at",
	    		"codingteam.net","jabber-br.org","jabber-hispano.org","jabber-me.de","jabber.ccc.de",
	    		"jabber.chaotic.de","jabber.co.nz","jabber.cz","jabber.fourecks.de",
	    		"jabber.fsinf.at","jabber.hot-chilli.net","jabber.i-pobox.net",
	    		"jabber.iitsp.com","jabber.loudas.com","jabber.minus273.org","jabber.no",
	    		"jabber.org","jabber.rootbash.com","jabber.scha.de","jabber.second-home.de",
	    		"jabber.sow.as","jabber.yeahnah.co.nz","jabberbr.com","jabberd.eu",
	    		"jabberes.org","jabberim.de","jabbim.com","jabbim.cz","jabbim.pl",
	    		"jabbim.sk","jabster.pl","jaim.at","linuxlovers.at",
	    		"thiessen.it","thiessen.org","ubuntu-jabber.de","ubuntu-jabber.net","Other..."};
	    serviceCombo.addSelectionListener(new org.eclipse.swt.events.SelectionListener(){
	    	@Override
	    	public void widgetDefaultSelected(SelectionEvent e) {}
	    	@Override 
	    	public void widgetSelected(SelectionEvent e) {
	    		// TODO Auto-generated method stub
	    		if (serviceCombo.getText().equals("Other..."))	
	    			serviceText.setEnabled(true);
	    		else { 
	    			serviceText.setEnabled(false);
	    			try {
						serviceText.setText(checkConnection(serviceCombo.getText()));
					} catch (BackendException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    		}
	    	}
		});
	    serviceCombo.setEnabled(false);
	    serviceCombo.setItems(items);
	    serviceCombo.setLayoutData(new GridData(129, 15));
	    serviceText = new Text(composite, SWT.BORDER);
	    serviceText.setLayoutData(new GridData(98, 15));
	    serviceText.setEnabled(false);
	    serviceText.addFocusListener(new  FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {}
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
    			try {
					serviceText.setText(checkConnection(serviceText.getText()));
				} catch (BackendException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});   
	    new CLabel(composite, SWT.NONE).setText("Conference name:");
	    gd = new GridData(265,15);
	    gd.horizontalSpan = 2;
	    gd.grabExcessHorizontalSpace = true;
		nameConferenceText = new Text(composite, SWT.BORDER);
		nameConferenceText.setLayoutData(gd);
		new CLabel(composite, SWT.NONE).setText("Topic:");
	    gd = new GridData(265,15);
	    gd.horizontalSpan = 2;
	    gd.grabExcessHorizontalSpace = true;
		topicText = new Text(composite, SWT.BORDER);
		new CLabel(composite, SWT.NONE).setText("Schedule:");
		schedule = new DateTime(composite,SWT.NONE);
		topicText.setLayoutData(gd);
		new CLabel(composite, SWT.NONE).setText("");
		new CLabel(composite, SWT.NONE).setText("Item/s:\n(One per line)");
	    itemText = new Text(composite,SWT.BORDER | SWT.MULTI);
	    itemText.setToolTipText("Use newline to new item");
	    gd = new GridData(265,110);
	    gd.horizontalSpan = 2;
	    gd.grabExcessHorizontalSpace = true;
	    itemText.setLayoutData(gd);
		setPageComplete(true);
	    // set the composite as the control for this page
		setControl(composite);
	}

	private String checkConnection(String cases) throws BackendException{
		XMPPConnection connection ;
		boolean disconnected = false;
//		return "conference."+cases;
		try { 
			if (cases.equals(NetworkPlugin.getDefault().getRegistry().getBackend(media).getServerContext().getServerHost()))
				connection = ((JabberBackend)NetworkPlugin.getDefault().getRegistry().getBackend(media).getBackendFromProxy()).getConnection();
			else {
		    	connection = new XMPPConnection(cases);  
				ServersCredential credenziali = new ServersCredential(this.getShell(),this);
				credenziali.open();
				disconnected = true;
				connection.connect();
				connection.login(this.nick,this.password);
	//			NetworkPlugin.getDefault().getRegistry().getBackend(media).connect(new ServerContext(cases), new UserContext(nick, password));
			}
			ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
			DiscoverItems discoItems = discoManager.discoverItems(cases);
		    Iterator it = discoItems.getItems();
		    while (it.hasNext()) {
		      DiscoverItems.Item item = (DiscoverItems.Item) it.next();
		      String current = item.toXML();
		      if (current.contains("name")){
		    	  if (current.contains("Public Chatrooms") || current.contains("Multi-User Chat")){
		        	if (disconnected)
		        		connection.disconnect();
		    		  return current.split("jid=\"")[1].split("\"")[0];
		    	  }
		       }
		       else	        	  
		    	   if (current.contains("conf")){
		    		   if (disconnected)
			        		connection.disconnect();
		    	   	  return current.split("jid=\"")[1].split("\"")[0];
		    	   }
		      }
		} catch (XMPPException exe) {JOptionPane.showMessageDialog(null,"Unable to connect to "+cases+"; please select a different service.");}
		return "";
	}
	public void handleEvent(Event e){
	
	}
	
	private String setRoom(){
		if (this.nameConferenceText.getText().equals(""))
			return this.nickNameText.getText()+".room@"+this.serviceText.getText();
		return this.nameConferenceText.getText()+"@"+this.serviceText.getText();
	}
	public IWizardPage getNextPage(){ 
		// save information until dispose page
		if (this.checkData()){
			this.saveData();
		InvitePage page = ((InviteWizard)getWizard()).invitePage;
		page.setContext(this.getContext());
		return page;
		}
		else
			JOptionPane.showMessageDialog(null,"Please fill all required field");
		return this;
	}
	
	private EConferenceContext getContext(){
		return this.context;
	}
	public boolean canFlipToNextPage(){
		return true;
	}
	
	public void setNick(String value){
		this.nick = value;
	}
	
	public void setPass(String value){
		this.password = value;
	}
	
	private void saveData(){
			this.context.setBackendId(media);
			this.context.setName(this.nameConferenceText.getText());
			this.context.setRoom(this.setRoom());
			this.context.setTopic(this.topicText.getText());
			String[] items = this.itemText.getText().split(System.getProperty("line.separator"));
			IItemList il = new ItemList();
			for (int i =0 ; i< items.length;i++)
				il.addItem( new DiscussionItem( items[i] ) );
			this.context.setItemList(il);
			this.context.setSchedule(this.schedule.getDay()+""+this.schedule.getMonth()+":"+this.schedule.getYear());	
	}
	
	private boolean checkData(){
		if (nickNameText.getText().equals(""))
			return false;
		if (serviceCombo.getText().equals(""))
				return false;
		if (serviceCombo.getText().equals("Other...") && serviceText.getText().equals("") )
			return false;
		
		return true;
	}
	/*
	 * Sets the completed field on the wizard class when all the information 
	 * is entered and the wizard can be completed
	 */	 
	public boolean isPageComplete(){
		return true;
	}
}