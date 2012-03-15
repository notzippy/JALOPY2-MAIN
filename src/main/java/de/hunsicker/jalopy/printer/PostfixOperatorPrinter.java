/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;


/**
 * Printer for postfix operators.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class PostfixOperatorPrinter
    extends OperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new PostfixOperatorPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new PostfixOperatorPrinter object.
     */
    protected PostfixOperatorPrinter()
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
        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }

        out.print(node.getText(), node.getType());
    }
}
