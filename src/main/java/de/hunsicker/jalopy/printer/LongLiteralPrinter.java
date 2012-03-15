/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;


/**
 * Printer for <code>long</code> literals.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class LongLiteralPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new LongLiteralPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a mew LongLiteralPrinter object.
     */
    protected LongLiteralPrinter()
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
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     * @param out DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void print(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        String text = node.getText();
        int index = text.indexOf('l');

        if (index == -1)
        {
            out.print(text, JavaTokenTypes.NUM_LONG);
        }
        else
        {
            out.print(text.substring(0, index), JavaTokenTypes.NUM_LONG);
            out.print(L, JavaTokenTypes.NUM_LONG);
        }
    }
}
