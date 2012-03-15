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


/**
 * Printer for the Java modifiers [LITERAL_public, LITERAL_protected, LITERAL_private,
 * LITERAL_static, LITERAL_final, LITERAL_abstract, LITERAL_native, LITERAL_transient,
 * LITERAL_synchronized, LITERAL_volatile, LITERAL_strictfp].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class ModifierPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ModifierPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ModifierPrinter object.
     */
    protected ModifierPrinter()
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
        printCommentsBefore(node, NodeWriter.NEWLINE_NO, out);

        int offset = out.print(node.getText(), node.getType());

        trackPosition((JavaNode) node, out.line, offset, out);

        printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

        if (!out.newline)
        {
            out.print(SPACE, node.getType());
        }
    }
}
