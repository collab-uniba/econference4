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
package it.uniba.di.cdg.xcore.econference;

import it.uniba.di.cdg.xcore.econference.internal.EConferenceHelper;

import org.jmock.MockObjectTestCase;

/**
 * jUnit test for {@see it.uniba.di.cdg.xcore.econference.EConferenceHelper}. 
 */
public class EConferenceHelperTestCase extends MockObjectTestCase {
    
    private EConferenceHelper helper;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
//        this.helper = new EConferenceHelper( UiPlugin.getUIHelper(), NetworkPlugin.getDefault().getHelper() );
    }

    /**
     * Create the conference with an ad hoc context that must succeed. 
     */
    public void testOpenWithGoodContext() {
        // TODO 
    }

    /**
     * Create the conference. 
     */
    public void testOpenWithBadContext() {
        // TODO 
    }

    public void testOpenFromFile() {
        // TODO 
    }
    
    public void testOpenFromUnexistingFile() {
        // TODO 
    }
}