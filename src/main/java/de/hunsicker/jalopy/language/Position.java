/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

/**
 * Represents a position in a Java Source file.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 *
 * @since 1.0b9
 */
public final class Position
{
    //~ Instance variables ---------------------------------------------------------------

    public int column = 1;
    public int line = 1;

    //~ Constructors ---------------------------------------------------------------------

    Position(
        int line,
        int column)
    {
        this.line = line;
        this.column = column;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the column information of this position.
     *
     * @return The column information of this position.
     */
    public int getColumn()
    {
        return this.column;
    }


    /**
     * Returns the line information of this position.
     *
     * @return The line information of this position.
     */
    public int getLine()
    {
        return this.line;
    }


    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return this.line + ":" + this.column;
    }
}
