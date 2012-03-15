/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for assignment operators [<code>PLUS_ASSIGN</code>, <code>MINUS_ASSIGN</code>,
 * <code>STAR_ASSIGN</code>, <code>DIV_ASSIGN</code>,  <code>MOD_ASSIGN</code>,
 * <code>BAND_ASSIGN</code>,  <code>BOR_ASSIGN</code>, <code>BXOR_ASSIGN</code>,
 * <code>SL_ASSIGN</code>, <code>SR_ASSIGN</code>, <code>BSR_ASSIGN</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class AssignmentOperatorPrinter
    extends InfixOperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new AssignmentOperatorPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new AssignmentOperatorPrinter object.
     */
    protected AssignmentOperatorPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return class instance.
     */
    public static final Printer getInstance()
    {
        return INSTANCE;
    }


    /**
     * {@inheritDoc}
     */
    public void print(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        super.print(
            node,
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.PADDING_ASSIGNMENT_OPERATORS,
                ConventionDefaults.PADDING_ASSIGNMENT_OPERATORS), out);
    }
}
