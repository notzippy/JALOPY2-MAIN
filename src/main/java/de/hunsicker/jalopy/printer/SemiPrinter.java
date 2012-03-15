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
 * Printer for the Java statement delimeter (<code>SEMI</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class SemiPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new SemiPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new SemiPrinter object.
     */
    protected SemiPrinter()
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
        boolean innerClass = (out.last == JavaTokenTypes.CLASS_DEF);
        out.print(SEMI, JavaTokenTypes.SEMI);

        if (!printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out))
        {
            out.printNewline();
        }

        // if we've printed an anonymous inner class we adjust the type
        // to get the right behaviour for the newline handling
        if (innerClass)
        {
            out.last = JavaTokenTypes.CLASS_DEF;
        }
        else
        {
            out.last = JavaTokenTypes.SEMI;
        }
    }
}
