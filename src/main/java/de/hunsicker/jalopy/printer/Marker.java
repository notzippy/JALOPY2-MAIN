/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

/**
 * Represents a marked position in a stream. Used by the printers to implement line
 * wrapping.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class Marker
{
    //~ Instance variables ---------------------------------------------------------------

    /** The column of the marker. */
    final int column;

    /** The line of the marker. */
    final int line;
    final boolean hasIndent;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Marker object.
     *
     * @param line the line of the marker.
     * @param column the column of the marker.
     */
    public Marker(
        int line,
        int column,
        boolean hasIndent)
    {
        this.line = line;
        this.column = column;
        this.hasIndent = hasIndent;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns a string representation of this marker.
     *
     * @return a string representation of this marker.
     */
    public String toString()
    {
        return this.line + ":" /* NOI18N */ + this.column;
    }
}
