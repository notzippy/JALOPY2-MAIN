/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;


/**
 * A class that wraps some application specific annotation data. It can be used to track
 * the position information for things like debugger breakpoints, erroneous lines, and
 * so on.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 *
 * @see de.hunsicker.jalopy.language.JavaRecognizer#attachAnnotations
 * @since 1.0b9
 */
public final class Annotation
{
    //~ Instance variables ---------------------------------------------------------------

    /** The application specific annotation data. */
    private final Object _data;

    /** The line number where the annotation belongs to. */
    private int _line;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Annotation object.
     *
     * @param line the line number to which the annotation is attached.
     * @param data the application specific annotation data.
     */
    public Annotation(
        int    line,
        Object data)
    {
        _line = line;
        _data = data;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the application specific annotation data.
     *
     * @return annotation data.
     */
    public Object getData()
    {
        return _data;
    }


    /**
     * Sets the line number where this annotation belongs.
     *
     * @param line line number (<code>&gt;= 1</code>).
     *
     * @throws IllegalArgumentException if <code><em>line</em> &lt; 1</code>
     */
    public void setLine(int line)
    {
        if (line < 1)
        {
            throw new IllegalArgumentException();
        }

        _line = line;
    }


    /**
     * Returns the (1-based) line number where this annotation belongs.
     *
     * @return the line number where this annotation belongs (<code>&gt;= 1</code>).
     */
    public int getLine()
    {
        return _line;
    }
}
