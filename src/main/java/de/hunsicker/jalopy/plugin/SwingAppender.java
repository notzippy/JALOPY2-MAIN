/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin;

import org.apache.log4j.Appender;


/**
 * Common interface for appenders which output log statements in a visual component.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.1 $
 */
public interface SwingAppender
    extends Appender
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Clears all messages currently being displayed.
     */
    public void clear();


    /**
     * Notifies the appender that a run was finished.
     *
     * @since 1.0b9
     */
    public void done();
}
