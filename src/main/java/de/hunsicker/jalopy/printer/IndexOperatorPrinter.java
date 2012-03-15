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
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for index operators [<code>INDEX_OP</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class IndexOperatorPrinter
    extends OperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new IndexOperatorPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new IndexOperatorPrinter object.
     */
    protected IndexOperatorPrinter()
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
        AST expr = printLeftHandSide(node, out);

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_BEFORE_BRACKETS,
                ConventionDefaults.SPACE_BEFORE_BRACKETS))
        {
            out.print(SPACE, out.last);
        }

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.PADDING_BRACKETS, ConventionDefaults.PADDING_BRACKETS))
        {
            out.print(BRACKET_LEFT_SPACE, JavaTokenTypes.LBRACK);
            PrinterFactory.create(expr, out).print(expr, out);
            out.print(SPACE_BRACKET_RIGHT, JavaTokenTypes.RBRACK);
        }
        else
        {
            out.print(BRACKET_LEFT, JavaTokenTypes.LBRACK);
            PrinterFactory.create(expr, out).print(expr, out);
            out.print(BRACKET_RIGHT, JavaTokenTypes.RBRACK);
        }

        /**
         * @todo print comments after LBRACK?
         */
    }
}
