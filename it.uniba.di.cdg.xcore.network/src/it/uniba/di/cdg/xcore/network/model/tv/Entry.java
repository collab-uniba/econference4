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
package it.uniba.di.cdg.xcore.network.model.tv;

import java.util.Calendar;
import java.util.Date;

public class Entry {
    public static final String NO_WHO = "__NO_WHO__";

    /**
     * Specify what type of entry is this. An entry can be of three main
     * types: a system message (<code>SYSTEM_MSG</code>), a normal
     * chat message (<code>CHAT_MSG</code>) or a private message (PRIVATE_MSG).
     * In case it's not possible to define a senseful entry type, the
     * type <code>UNKNOWN</code> must be used.
     *
     */
    public static enum EntryType { UNKNOWN, SYSTEM_MSG, CHAT_MSG, PRIVATE_MSG };
    
    /**
     * The timestamp indicating when this message has been logged.
     */
    private Date timestamp;
        
    private String who;

    private String text;
    
    private EntryType entryType;

    public Entry() {
        this( null, null, null, EntryType.UNKNOWN );
    }

    public Entry( Date timestamp, String who, String text, EntryType entryType ) {
        super();
        this.timestamp = timestamp;
        this.who = who;
        this.text = text;
        this.entryType = entryType;
    }

    /**
     * Convenience constructor for adding chat message: the timestamp is set to "now",
     * it's only required to specify the sender and the message
     * 
     * @param who
     * @param text
     */
    public Entry( String who, String text ) {
        this( Calendar.getInstance().getTime(), who, text, EntryType.CHAT_MSG );
    }

    /**
     * Convenience constructor for adding system message: the timestamp is set to "now" 
     * and the "who" field set to <code>NO_WHO</code>.
     *
     * @param text
     */
    public Entry( String text ) {
        this( Calendar.getInstance().getTime(), NO_WHO, text, EntryType.UNKNOWN );
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp( Date timestamp ) {
        this.timestamp = timestamp;
    }

    public String getWho() {
        return who;
    }

    public void setWho( String who ) {
        this.who = who;
    }

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }

    /**
     * Change the entry type
     */
    public void setType(EntryType entryType) {
        this.entryType = entryType;
    }

    /**
     * Returns the entry type
     * @return an <code>EntryType</code> member
     */
    public EntryType getType() {
        return this.entryType;
    }

    /**
     * Helper method that tests if <code>who == NO_WHO</code>.
     * 
     * @return <code>true</code> if the entry is a system message entry, <code>false</code> otherwise
     */
    public boolean isSystemEntry() {
        return NO_WHO.equals( getWho() );
    }

    @Override
    public String toString() {
        if (NO_WHO.equals( getWho() ))
            return String.format( "%s", getText() );
        return String.format( "[%1$tH:%1$tM:%1$tS] %2$s: %3$s", getTimestamp(), getWho(), getText() );
    }
}
