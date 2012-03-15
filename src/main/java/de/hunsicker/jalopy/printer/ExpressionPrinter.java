/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;


/**
 * Printer for expressions (<code>EXPR</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class ExpressionPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ExpressionPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ExpressionPrinter object.
     */
    protected ExpressionPrinter()
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
        printBlankLinesBefore((JavaNode) node, out);

        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }

        switch (out.last)
        {
            case JavaTokenTypes.CLASS_DEF :

                switch (((JavaNode) node).getParent().getType())
                {
                    // anonymous inner class is a VARIABLE_DEF assignment, let
                    // VariableDeclarationPrinter.java handle the masquerading
                    case JavaTokenTypes.ASSIGN :
                        break;

                    // anonymous inner class is a return expression
                    case JavaTokenTypes.LITERAL_return :
                        break;

                    // anonymous inner class is a METHOD_CALL param expression
                    case JavaTokenTypes.SLIST :
                    case JavaTokenTypes.ELIST :
                        break;

                    // anonymous inner class is local assignment
                    default :
                        out.last = JavaTokenTypes.RCURLY;
                        out.printNewline();

                        break;
                }

                break;
        }
    }
}
