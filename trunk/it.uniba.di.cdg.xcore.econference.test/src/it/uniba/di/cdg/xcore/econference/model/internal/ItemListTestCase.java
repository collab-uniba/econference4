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
package it.uniba.di.cdg.xcore.econference.model.internal;

import static it.uniba.di.cdg.xcore.econference.model.IItemList.ENCODING_SEPARATOR;
import it.uniba.di.cdg.xcore.econference.model.DiscussionItem;
import it.uniba.di.cdg.xcore.econference.model.IDiscussionItem;
import it.uniba.di.cdg.xcore.econference.model.IItemList;
import it.uniba.di.cdg.xcore.econference.model.IItemListListener;
import it.uniba.di.cdg.xcore.econference.model.ItemList;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * jUnit test case for {@see it.uniba.di.cdg.xcore.econference.model.internal.ItemList}. 
 */
public class ItemListTestCase extends MockObjectTestCase {

    private ItemList itemList;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.itemList = new ItemList();
    }

    /**
     * Ensure that items are added to the item list and order is preserved.
     */
    public void testAddItems() {
        IDiscussionItem item1 = new DiscussionItem( "item1" );
        IDiscussionItem item2 = new DiscussionItem( "item2" );

        Mock mock = mock( IItemListListener.class );
        mock.expects( once() ).method( "itemAdded" ).with( eq( item1 ) );
        mock.expects( once() ).method( "itemAdded" ).with( eq( item2 ) );
        IItemListListener listener = (IItemListListener) mock.proxy();

        itemList.addListener( listener );
        
        assertEquals( 0, itemList.size() ); // Ensure it is empty
        
        itemList.addItem( item1 );
        itemList.addItem( item2 );
        
        assertEquals( 2, itemList.size() );

        IDiscussionItem[] items = new IDiscussionItem[] { item1, item2 };

        int i = 0;
        for (int j=0; j<itemList.size(); j++)
            assertEquals( items[i++], (IDiscussionItem)itemList.getItem(j) );
    }

    public void testRemoveItems() {
        IDiscussionItem item1 = new DiscussionItem( "item1" );
        IDiscussionItem item2 = new DiscussionItem( "item2" );
        
        Mock mock = mock( IItemListListener.class );
        mock.stubs().method( "itemAdded" ); // Don't bother 
        mock.expects( once() ).method( "itemRemoved" ).with( eq( item1 ) );
        mock.expects( once() ).method( "itemRemoved" ).with( eq( item2 ) );
        IItemListListener listener = (IItemListListener) mock.proxy();

        itemList.addListener( listener );

        assertEquals( 0, itemList.size() ); // Ensure it is empty
        
        itemList.addItem( item1 );
        itemList.addItem( item2 );
        assertEquals( 2, itemList.size() ); // Ensure it is empty
        
        try {
            itemList.removeItem( 3 );
            fail( "Removing an invalid index should throw an exception" );
        } catch (IndexOutOfBoundsException e) {
            // Ok
        }

        itemList.removeItem( 0 ); // remove item1
        assertEquals( 1, itemList.size() ); // Ensure only one item remains
        assertEquals( item2, itemList.getItem( 0 ) ); // Item2 is in first position now
        
        itemList.removeItem( 0 ); // remove item2
        assertEquals( 0, itemList.size() ); // No elements left
    }

    public void testSetCurrentItem() {
        assertEquals( IItemList.NO_ITEM_SELECTED, itemList.getCurrentItemIndex() );
        
        IDiscussionItem item1 = new DiscussionItem( "item1" );
        IDiscussionItem item2 = new DiscussionItem( "item2" );

        Mock mock = mock( IItemListListener.class );
        mock.stubs().method( "itemAdded" ); // Don't bother 
        mock.stubs().method( "itemRemoved" ); // Don't bother 
        mock.expects( once() ).method( "currentSelectionChanged" ).with( eq( 0 ) );
        IItemListListener listener = (IItemListListener) mock.proxy();
        
        itemList.addListener( listener );

        itemList.addItem( item1 );
        itemList.addItem( item2 );
        
        itemList.setCurrentItemIndex( 0 );

        try {
            itemList.setCurrentItemIndex( 4 );
            fail( "Setting the current item index to an out-of-range item should throw an exception" );
        } catch (IllegalArgumentException e) {
            // Ok
        }

        try {
            itemList.setCurrentItemIndex( -2 );
            fail( "Setting the current item index to an out-of-range item should throw an exception" );
        } catch (IllegalArgumentException e) {
            // Ok
        }
        
        itemList.removeItem( 0 );
    }
    
    public void testDecoded() {
        String encodedItems = "item 1" + ENCODING_SEPARATOR + 
        "item 2" + ENCODING_SEPARATOR +
        "another item" + ENCODING_SEPARATOR + 
        "last item";
        
        itemList.decode( encodedItems );
        
        assertEquals( 4, itemList.size() );
        
        // Order is honored
        assertEquals( "item 1", itemList.getItem( 0 ).getText() );
        assertEquals( "item 2", itemList.getItem( 1 ).getText() );
        assertEquals( "another item", itemList.getItem( 2 ).getText() );
        assertEquals( "last item", itemList.getItem( 3 ).getText() );
    }
    
    public void testEncoded() {
        // By default, an empty item list is encoded as enmpty string
        assertEquals( "", itemList.encode() );
        
        itemList.addItem( new DiscussionItem( "item 1" ) );
        itemList.addItem( new DiscussionItem( "item 2" ) );
        itemList.addItem( new DiscussionItem( "last item" ) );
    
        String expected = "item 1" + ENCODING_SEPARATOR + "item 2" + ENCODING_SEPARATOR
            + "last item" + ENCODING_SEPARATOR;
        
        assertEquals( expected, itemList.encode() );
    }
    
    /**
     * Empty encoded strings must be rejected or have no effect on the item list.
     */
    public void testDecodeEmptyString() {
        Mock mock = mock( IItemListListener.class );
        mock.expects( never() ).method( "contentChanged" ).with( eq( itemList ) );
        itemList.addListener( (IItemListListener) mock.proxy() );
        
        itemList.decode( "" );
        assertEquals( "", itemList.encode() );
        
        itemList.decode( ENCODING_SEPARATOR );
        assertEquals( "", itemList.encode() );
    }
    
    public void testDecodedItemsWillReplaceExistingOnes() {
        Mock mock = mock( IItemListListener.class );
        mock.stubs().method( "itemAdded" );
        mock.expects( once() ).method( "contentChanged" ).with( eq( itemList ) );
        itemList.addListener( (IItemListListener) mock.proxy() );

        itemList.addItem( "this a pre-existing item" );
        
        String encodedItems = "enc item 1" + ENCODING_SEPARATOR + 
        "enc item 2" + ENCODING_SEPARATOR;
        
        itemList.decode( encodedItems );
        
        assertEquals( 2, itemList.size() );
        assertEquals( "enc item 1", itemList.getItem( 0 ).getText() );
        assertEquals( "enc item 2", itemList.getItem( 1 ).getText() );
    }
}
