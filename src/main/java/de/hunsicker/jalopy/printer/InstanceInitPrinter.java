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
 * Printer for instance initialization blocks.
 * <pre class="snippet">
 * class Mugs {
 *    Mug c1;
 *    Mug c2;
 *    <strong>{</strong>
 *        c1 = new Mug();
 *        c2 = new Mug();
 *        System.out.println("c1 & c2 initialized");
 *    <strong>}</strong>
 *    // ...
 * }
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class InstanceInitPrinter
    extends BasicDeclarationPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new InstanceInitPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new InstanceInitPrinter object.
     */
    protected InstanceInitPrinter()
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
        out.last = JavaTokenTypes.INSTANCE_INIT;

        AST body = node.getFirstChild();
        PrinterFactory.create(body, out).print(body, out);
        out.last = JavaTokenTypes.INSTANCE_INIT;
    }
}
