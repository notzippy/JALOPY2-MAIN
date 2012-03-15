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
 * Printer for bitwise operators [<code>BAND</code>, <code>BOR</code>,
 * <code>BXOR</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.7 $
 */
final class BitwiseOperatorPrinter
    extends InfixOperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton . */
    private static final Printer INSTANCE = new BitwiseOperatorPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new BitwiseOperatorPrinter object.
     */
    protected BitwiseOperatorPrinter()
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
                ConventionKeys.PADDING_BITWISE_OPERATORS,
                ConventionDefaults.PADDING_BITWISE_OPERATORS), out);
    }
}
