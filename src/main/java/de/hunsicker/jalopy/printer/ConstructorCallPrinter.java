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
 * Printer for constructor calls [<code>CTOR_CALL</code>, <code>SUPER_CTOR_CALL</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class ConstructorCallPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ConstructorCallPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ConstructorCallPrinter object.
     */
    protected ConstructorCallPrinter()
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
            switch (child.getType())
            {
                case JavaTokenTypes.ELIST :
                    LeftParenthesisPrinter.getInstance().print(node, out);

                // fall through                
                default :
                    PrinterFactory.create(child, out).print(child, out);

                    break;
            }
        }
    }
}
