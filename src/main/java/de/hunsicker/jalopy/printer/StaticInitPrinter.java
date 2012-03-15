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
 * Printer for static initializers (<code>STATIC_INIT</code>).
 * <pre class="snippet">
 * class Spoon {
 *     static int i;
 *     <strong>static {</strong>
 *         i = 47;
 *     <strong>}</strong>
 *     // ...
 * }
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class StaticInitPrinter
    extends BasicDeclarationPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new StaticInitPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new StaticInitPrinter object.
     */
    protected StaticInitPrinter()
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
        out.print(STATIC, JavaTokenTypes.LITERAL_static);
        printCommentsAfter(node, out);
        out.last = JavaTokenTypes.LITERAL_static;

        AST child = node.getFirstChild();
        PrinterFactory.create(child, out).print(child, out);
        out.last = JavaTokenTypes.STATIC_INIT;
    }
}
