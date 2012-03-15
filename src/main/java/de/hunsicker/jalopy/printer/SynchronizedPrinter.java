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
 * Printer for synchronized blocks (<code>SYNBLOCK</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class SynchronizedPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new SynchronizedPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new SynchronizedPrinter object.
     */
    protected SynchronizedPrinter()
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
        printCommentsBefore(node, out);

        out.print(SYNCHRONIZED, JavaTokenTypes.LITERAL_synchronized);

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_BEFORE_STATEMENT_PAREN,
                ConventionDefaults.SPACE_BEFORE_STATEMENT_PAREN))
        {
            out.print(SPACE, JavaTokenTypes.LITERAL_synchronized);
        }

        AST lparen = node.getFirstChild();
        PrinterFactory.create(lparen, out).print(lparen, out);

        AST expr = lparen.getNextSibling();
        PrinterFactory.create(expr, out).print(expr, out);

        AST rparen = expr.getNextSibling();
        PrinterFactory.create(rparen, out).print(rparen, out);

        AST body = rparen.getNextSibling();
        PrinterFactory.create(body, out).print(body, out);

        out.last = JavaTokenTypes.RCURLY;
    }
}
