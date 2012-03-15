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
 * Printer for flow control statements (<code>LITERAL_break</code> and
 * <code>LITERAL_continue</code>.
 * <pre class="snippet">
 * label1:
 * <em>outer-iteration</em>
 * {
 *     <em>inner-iteration</em>
 *     {
 *         // ...
 *         <strong>break</strong>;
 *         // ...
 *         <strong>continue</strong>;
 *         // ...
 *         <strong>continue</strong> label1;
 *         // ...
 *         <strong>break</strong> label1;
 *     }
 * }
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class FlowControlPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new FlowControlPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new FlowControlPrinter object.
     */
    protected FlowControlPrinter()
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

        int offset = out.print(node.getText(), node.getType());

        trackPosition((JavaNode) node, out.line, offset, out);

        if (node.getFirstChild().getType() != JavaTokenTypes.SEMI)
        {
            out.print(SPACE, JavaTokenTypes.WS);
        }

        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }
    }
}
