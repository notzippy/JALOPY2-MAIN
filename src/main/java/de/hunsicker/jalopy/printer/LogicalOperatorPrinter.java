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
 * Printer for the logical operators <code>&amp;, ||</code>.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class LogicalOperatorPrinter
    extends InfixOperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new LogicalOperatorPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new LogicalOperatorPrinter object.
     */
    protected LogicalOperatorPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return the sole instance of this class.
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
                ConventionKeys.PADDING_LOGICAL_OPERATORS,
                ConventionDefaults.PADDING_LOGICAL_OPERATORS), out);
    }
}
