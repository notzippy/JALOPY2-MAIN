/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin;

/**
 * Provides access to the status bar of a graphical Java application.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public interface StatusBar
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the text to display.
     *
     * @param text text to display.
     */
    public void setText(String text);
}
