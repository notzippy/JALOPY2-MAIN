/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import antlr.collections.AST;


/**
 * Represents the current parentheses scope. Provides a space to store information needed
 * to implement sophisticated line wrapping.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class ParenthesesScope
{
    //~ Instance variables ---------------------------------------------------------------

    /**
     * The current nesting level. <code>0</code> means that we're currently not in a
     * parentheses scope.
     */
    final int level;

    /** Holds the first method call node of a method call chain. */
    AST chainCall;
    boolean wrap;

    /** The offset of the first method call of a method call chain. */
    int chainOffset;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ParenthesesScope object.
     *
     * @param level DOCUMENT ME!
     */
    public ParenthesesScope(int level)
    {
        this.level = level;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString()
    {
        return "(" + this.level + ")";
    }
}
