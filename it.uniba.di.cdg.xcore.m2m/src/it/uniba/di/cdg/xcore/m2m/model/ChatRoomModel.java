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
package it.uniba.di.cdg.xcore.m2m.model;

import it.uniba.di.cdg.aspects.GetSafety;
import it.uniba.di.cdg.aspects.SetSafety;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@see it.uniba.di.cdg.xcore.m2m.model.IChatRoom}.
 */
public class ChatRoomModel implements IChatRoomModel {
    /**
     * The tracked participants.
     */
    private Map<String, IParticipant> participants;

    /**
     * Reference to the local participant (this user).
     */
    private IParticipant localUser;
    
    /**
     * The listeners for chat room events.
     */
    private List<IChatRoomModelListener> listeners;
    
    /**
     * The  current subject.
     */
    private String subject;

    /**
     * Create a new participant room.
     */
    public ChatRoomModel() {
        this.participants = new HashMap<String, IParticipant>();
        this.listeners = new ArrayList<IChatRoomModelListener>();
        this.subject = "";
    }
    
    @Override
    @GetSafety
    public IParticipant getParticipant( String participantId ) {
        return participants.get( participantId );
    }
    
    @Override
    @GetSafety
    public IParticipant getParticipantByNickName(String nick) {
    	Iterator<String> pIDs = participants.keySet().iterator();
    	boolean found = false;
    	IParticipant p = null;
    	while (pIDs.hasNext() && !found) {
			String id = (String) pIDs.next();
			p = participants.get(id);
			if (p.getNickName().equals(nick) || p.getId().equals(nick))
				found = true;						
		}
    	return found == true ? p : null;
    }

    @Override
    @SetSafety
    public void addParticipant( IParticipant participant ) {
        participants.put( participant.getId(), participant );
        participant.setChatRoom( this );
        
        for (IChatRoomModelListener l : listeners)
            l.added( participant );
    }

    @Override
    @SetSafety
    public void removeParticipant( IParticipant participant ) {
        participants.remove( participant.getId() );
        participant.setChatRoom( null );
        
        for (IChatRoomModelListener l : listeners)
            l.removed( participant );
    }

    @Override
    @SetSafety
    public void addListener( IChatRoomModelListener listener ) {
        listeners.add( listener );
    }

    /* (non-Javadoc)
     * @see it.uniba.di.cdg.xcore.m2m.model.IChatRoom#removeListeners(it.uniba.di.cdg.xcore.m2m.model.IChatRoom.IChatRoomListener)
     */
    @SetSafety
    public void removeListener( IChatRoomModelListener listener ) {
        listeners.remove( listener );
    }

    /* (non-Javadoc)
     * @see it.uniba.di.cdg.xcore.m2m.model.IParticipant.IParticipantListener#changed(it.uniba.di.cdg.xcore.m2m.model.IParticipant)
     */
    @GetSafety
    public void changed( IParticipant participant ) {
        for (IChatRoomModelListener l : listeners)
            l.changed( participant );
    }

    /* (non-Javadoc)
     * @see it.uniba.di.cdg.xcore.m2m.model.IChatRoom#getParticipants()
     */
    @GetSafety
    public IParticipant[] getParticipants() {
        return participants.values().toArray( new IParticipant[participants.size()] );
    }

    /* (non-Javadoc)
     * @see it.uniba.di.cdg.xcore.m2m.model.IChatRoom#getSubject()
     */
    @GetSafety
    public String getSubject() {
        return subject;
    }

    /* (non-Javadoc)
     * @see it.uniba.di.cdg.xcore.m2m.model.IChatRoomModel#setSubject(java.lang.String, java.lang.String)
     */
    @SetSafety
    public void setSubject( String subject, String who ) {
        this.subject = subject;

        for (IChatRoomModelListener l : listeners)
            l.subjectChanged( who );
    }

    /* (non-Javadoc)
     * @see it.uniba.di.cdg.xcore.m2m.model.IChatRoom#setLocalUser(it.uniba.di.cdg.xcore.m2m.model.IParticipant)
     */
    @SetSafety
    public void setLocalUser( IParticipant p ) {
        this.localUser = p;

        for (IChatRoomModelListener l : listeners)
            l.localUserChanged();
    }

    /* (non-Javadoc)
     * @see it.uniba.di.cdg.xcore.m2m.model.IChatRoom#getLocalUser()
     */
   
    @GetSafety
    public IParticipant getLocalUser() {
        return localUser;
    }

    /* (non-Javadoc)
     * @see it.uniba.di.cdg.xcore.m2m.model.IChatRoomModel#getLocalUserOrParticipant(java.lang.String)
     */
    @GetSafety
    public IParticipant getLocalUserOrParticipant( String id ) {
        IParticipant p = getLocalUser();
        if (p == null || (p != null && !id.equalsIgnoreCase( p.getId() )))
            p = getParticipant( id );
        return p;
    }
    
    /**
     * Returns an iterator to the model listeners so that derived class can notify their own events.
     * 
     * @return an iterator to listeners.
     */
    @GetSafety
    protected List<IChatRoomModelListener> listeners() {
        return listeners;
    }
    
    /**
     * Provides internal thread synchronization.
     */

    
}
