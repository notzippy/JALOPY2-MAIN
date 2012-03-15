/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.util.StringHelper;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;


/**
 * A custom Log4J layout which reformats muliple line messages and takes care of
 * throwable information.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
final class SwingLayout
    extends Layout
{
    //~ Static variables/initializers ----------------------------------------------------

    // TODO private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final int MAX_LINE_LENGTH = 100;

    //~ Instance variables ---------------------------------------------------------------

    private Convention _settings = Convention.getInstance();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new SwingLayout object.
     */
    public SwingLayout()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Activate the options that were previously set with calls to option setters.
     * Actaally does nothing as the option setters are deprecated and no longer used.
     */
    public void activateOptions()
    {
    }


    /**
     * Returns the log statement. Adds throwable information if available and enabled in
     * the code convention. Multiple line messages will be reformatted to fit into the
     * given maximal line length.
     *
     * @param event the logging event.
     *
     * @return the formatted message.
     */
    public String format(LoggingEvent event)
    {
        if (ignoresThrowable())
        {
            String message = event.getRenderedMessage();

            // max. line length exeeded, perform reformatting
            if (message.length() > MAX_LINE_LENGTH)
            {
                return StringHelper.wrapString(message, MAX_LINE_LENGTH, true);
            }
            return message;
        }
        StringBuffer buf = new StringBuffer(100);

        if (event.getThrowableStrRep() != null)
        {
            String[] lines = event.getThrowableStrRep();
            String message = event.getRenderedMessage();

            // first the message
            buf.append(message);
            buf.append('\n');

            // append the stacktrace (if available)
            for (int i = 0; i < lines.length; i++)
            {
                buf.append(lines[i]);
                buf.append('\n');
            }

            // remove the last separator
            buf.setLength(buf.length() - 1);
        }
        else
        {
            buf.append(event.getRenderedMessage());
        }

        return buf.toString();
    }


    /**
     * Indicates whether throwable information should be included in the output.
     *
     * @return <code>true</code> if throwable information should be included in the
     *         output.
     */
    public boolean ignoresThrowable()
    {
        return !_settings.getBoolean(
            ConventionKeys.MSG_SHOW_ERROR_STACKTRACE,
            ConventionDefaults.MSG_SHOW_ERROR_STACKTRACE);
    }
}
