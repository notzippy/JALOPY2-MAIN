/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hunsicker.util.ChainingRuntimeException;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;


/**
 * Skeleton implementation of an appender which outputs messages in a visual component.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
public abstract class AbstractAppender
    extends AppenderSkeleton
    implements SwingAppender
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Position of the filename in the regex result. */
    public static final int POS_FILENAME = 1;

    /** Position of the lineno in the regex result. */
    public static final int POS_LINE = 2;

    /** Position of the message text in the regex result. */
    public static final int POS_TEXT = 4;

    /** Position of the message text in the regex result. */
    public static final int POS_COLUMN = 3;

    /** Name of the appender for output messages. */
    private static final String APPENDER_NAME = "JalopyAppender" /* NOI18N */;

    /**
     * Regex to apply for Emacs-style messages. Messages must comply to the
     * filename:line:column:text pattern.
     */
    private static final String PATTERN = "(.+?):(\\d+):(\\d+):\\s*(.+)" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /**
     * The regex to parse the messages. If messages have a format similiar to Emacs
     * messages (<code>filename:lineno:text</code>) the pattern will match.
     */
    protected final Pattern regex;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new AbstractAppender object.
     *
     * @throws ChainingRuntimeException If an error occurs
     */
    public AbstractAppender()
    {
        this.name = APPENDER_NAME;
        this.layout = new SwingLayout();
        setThreshold(Level.DEBUG);
		regex = Pattern.compile(PATTERN);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Does the actual outputting.
     *
     * @param ev logging event.
     */
    public abstract void append(LoggingEvent ev);


    /**
     * Sets the name of the appender. Overidden so that the initial name can't be
     * changed.
     *
     * @param name appender name (ignored).
     */
    public final void setName(String name)
    {
    }


    /**
     * {@inheritDoc}
     */
    public void close()
    {
    }


    /**
     * {@inheritDoc}
     */
    public void done()
    {
    }


    /**
     * Parses the given message. To access the parsed information one may typically use:
     * <pre class="snippet">
     * MatchResult result = parseMessage(message);
     * if (result == null)
     * {
     *     // handle plain message
     *     ...
     * }
     * else
     * {
     *     // this is an Emacs style message, you can easily access the
     *     // information
     *     String filename = result.group(POS_FILENAME);
     *     String line = result.group(POS_LINE);
     *     String column = result.group(POS_COLUMN);
     *     String text = result.group(POS_TEXT);
     *     ...
     * }
     * </pre>
     *
     * @param ev logging event.
     *
     * @return parsing result. Returns <code>null</code> if the message doesn't match the
     *         Emacs format <code>filename:line:column:text</code>.
     */
    public Matcher parseMessage(LoggingEvent ev)
    {
		Matcher matcher = regex.matcher(this.layout.format(ev));
        if (matcher.matches())
        {
            return matcher;
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @return always <code>true</code>.
     */
    public boolean requiresLayout()
    {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @return always <code>true</code>.
     */
    protected boolean checkEntryConditions()
    {
        return true;
    }
}
