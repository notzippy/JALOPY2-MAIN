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
 * Printer for assertions [<code>LITERAL_assert</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class AssertionPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new AssertionPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new AssertionPrinter object.
     */
    protected AssertionPrinter()
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
        out.print(ASSERT_SPACE, JavaTokenTypes.LITERAL_assert);

        AST condition = node.getFirstChild();
        PrinterFactory.create(condition, out).print(condition, out);

        AST next = condition.getNextSibling();

        switch (next.getType())
        {
            // print the message expression
            case JavaTokenTypes.EXPR :
                out.print(SPACE_COLON_SPACE, JavaTokenTypes.COLON);
                PrinterFactory.create(next, out).print(next, out);

                AST semi = next.getNextSibling();
                PrinterFactory.create(semi, out).print(semi, out);

                break;

            // print the delimiting semi
            case JavaTokenTypes.SEMI :
                PrinterFactory.create(next, out).print(next, out);

                break;

            default :
                break;
        }
    }
}
