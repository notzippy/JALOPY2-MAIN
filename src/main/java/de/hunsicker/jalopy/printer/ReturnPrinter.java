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
 * Printer for return statements (<code>LITERAL_return</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class ReturnPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ReturnPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ReturnPrinter object.
     */
    protected ReturnPrinter()
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

        int offset = out.print(RETURN, JavaTokenTypes.LITERAL_return);

        trackPosition((JavaNode) node, out.line, offset, out);

        AST next = node.getFirstChild();

        printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

        if (!out.newline && next.getType() != JavaTokenTypes.SEMI)
        {
            out.print(SPACE, JavaTokenTypes.WS);
        }

        Marker marker = out.state.markers.add();

        for (AST child = next; child != null; child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }

        out.state.markers.remove(marker);
    }
}
