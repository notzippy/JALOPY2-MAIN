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
 * Printer for case blocks [<code>CASESLIST</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
final class CaseBlockPrinter
    extends BlockPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new CaseBlockPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new CaseBlockPrinter object.
     */
    protected CaseBlockPrinter()
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
        boolean indent = false;
        JavaNode first = (JavaNode) node.getFirstChild();

        if (first != null)
        {
            // only increase indentation if we're not followed by a SLIST
            // which takes care for itself
            switch (first.getType())
            {
                case JavaTokenTypes.SLIST :
                    break;

                default :
                    indent = true;

                    break;
            }
        }
        else
    	{
                out.printNewline();
    	}

        if (indent)
        {
            out.indent();
        }

        for (AST child = first; child != null; child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }

        if (indent)
        {
            out.unindent();
        }

        out.last = JavaTokenTypes.RCURLY;
    }
}
