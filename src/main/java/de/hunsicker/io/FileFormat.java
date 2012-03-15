/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.io;

/**
 * Represents the file format of a platform. The file format is defined by the line
 * separator the platform uses.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public final class FileFormat
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Represents the platform default file format. */
    public static final FileFormat DEFAULT =
        new FileFormat(
            "DEFAULT" /* NOI18NB */, System.getProperty("line.separator" /* NOI18NB */));

    /** Indicates that the file format should be auto-detected. */
    public static final FileFormat AUTO =
        new FileFormat(
            "AUTO" /* NOI18NB */, System.getProperty("line.separator" /* NOI18NB */));

    /** Represents  the DOS file format (&quot;\r\n&quot;). */
    public static final FileFormat DOS =
        new FileFormat("DOS" /* NOI18NB */, "\r\n" /* NOI18NB */);

    /** Represents  the Mac file format (&quot;\r&quot;). */
    public static final FileFormat MAC =
        new FileFormat("MAC" /* NOI18NB */, "\r" /* NOI18NB */);

    /** Represents  the Unix file format (&quot;\n&quot;). */
    public static final FileFormat UNIX =
        new FileFormat("UNIX" /* NOI18NB */, "\n" /* NOI18NB */);

    /** Represents an unknown or not yet determined file format. */
    public static final FileFormat UNKNOWN =
        new FileFormat(
            "UNKNOWN" /* NOI18NB */, System.getProperty("line.separator" /* NOI18NB */));

    //~ Instance variables ---------------------------------------------------------------

    private final String _name;
    private final String _separator;

    //~ Constructors ---------------------------------------------------------------------

    private FileFormat(
        String name,
        String lineSeparator)
    {
        _name = name.intern();
        _separator = lineSeparator.intern();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the characteristic line separator of the file format. Note that both
     * {@link #UNKNOWN} and {@link #AUTO} does not have distinct line separators and
     * therefore rather return the platform default separator.
     *
     * @return the line separator of the file format. Either &quot;\r\n&quot; or
     *         &quot;\n&quot; or &quot;\r&quot;
     */
    public String getLineSeparator()
    {
        return _separator;
    }


    /**
     * Returns the descriptive name of the file format.
     *
     * @return the name of the file format.
     */
    public String getName()
    {
        return _name;
    }


    /**
     * Returns the FileFormat object for the given file format string.
     *
     * @param format a valid format string. Either &quot;\r\n&quot; or &quot;DOS&quot;,
     *        &quot;\n&quot; or &quot;UNIX&quot;, &quot;\r&quot; or &quot;MAC&quot;,
     *        &quot;auto&quot; or &quot;default&quot; (case-insensitive).
     *
     * @return the FileFormat object for the given format string. Returns {@link
     *         #DEFAULT} if <em>format</em> == null or <em>format</em> does not denote a
     *         valid file format string.
     */
    public static FileFormat valueOf(String format)
    {
        FileFormat result = FileFormat.DEFAULT;

        if (format != null)
        {
            format = format.toUpperCase().intern();

            if ((DOS._name == format) || (DOS._separator == format))
            {
                result = DOS;
            }
            else if ((UNIX._name == format) || (UNIX._separator == format))
            {
                result = UNIX;
            }
            else if ((MAC._name == format) || (MAC._separator == format))
            {
                result = MAC;
            }
            else if (AUTO._name == format)
            {
                result = AUTO;
            }
            else if (UNKNOWN._name == format)
            {
                result = UNKNOWN;
            }
        }

        return result;
    }


    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     *
     * @see #getLineSeparator
     */
    public String toString()
    {
        return _separator;
    }
}
