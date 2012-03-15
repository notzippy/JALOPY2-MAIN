/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import javax.swing.JLabel;

import de.hunsicker.util.ResourceBundleFactory;


/**
 * A label which displays its label text along with an integer count.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
class CountLabel
    extends JLabel
{
    //~ Instance variables ---------------------------------------------------------------

    /** Used to construct the label text. */
    private StringBuffer _buffer = new StringBuffer(20);

    /** The current count. */
    private int _count;

    /** The index in the buffer where this counter's count number starts. */
    private int _index;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new CountLabel object and an initial count of zero.
     */
    public CountLabel()
    {
        this(
            ResourceBundleFactory.getBundle(
                "de.hunsicker.jalopy.swing.Bundle" /* NOI18N */).getString(
                "LBL_COUNT" /* NOI18N */));
    }


    /**
     * Creates a new CountLabel object with an initial count of zero.
     *
     * @param text the label text.
     */
    public CountLabel(String text)
    {
        this(text, 0);
    }


    /**
     * Creates a new CountLabel object.
     *
     * @param text the label text.
     * @param initialCount the initial value of the count.
     */
    public CountLabel(
        String text,
        int    initialCount)
    {
        super(text + ": " + initialCount);
        _count = initialCount;
        _index = text.length() + 2;
        _buffer.append(getText());
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the counter to the given value.
     *
     * @param count the count to display.
     */
    public void setCount(int count)
    {
        _count = count;
        _buffer.delete(_index, _buffer.length());
        _buffer.append(count);

        super.setText(_buffer.toString());
    }


    /**
     * Sets the text
     *
     * @param text The new text
     */
    public final void setText(String text)
    {
        if (_index == 0)
        {
            super.setText(text);
        }
    }


    /**
     * Returns the current count.
     *
     * @return the current count.
     */
    public int getCount()
    {
        return _count;
    }


    /**
     * Increases the counter.
     */
    public void increase()
    {
        setCount(++_count);
    }


    /**
     * Resets the counter.
     */
    public void reset()
    {
        _count = 0;
        setCount(0);
    }
}
