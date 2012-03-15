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
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for prefix operators.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
final class PrefixOperatorPrinter
    extends OperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new PrefixOperatorPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new PrefixOperatorPrinter object.
     */
    protected PrefixOperatorPrinter()
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

        out.print(node.getText(), node.getType());

        switch (node.getType())
        {
            case JavaTokenTypes.LNOT :
            case JavaTokenTypes.BNOT :
                printSpace(out);

                break;
        }

        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }
    }


    /**
     * Outputs a space if specified in the code convention.
     *
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printSpace(NodeWriter out)
      throws IOException
    {
        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_BEFORE_LOGICAL_NOT,
                ConventionDefaults.SPACE_BEFORE_LOGICAL_NOT))
        {
            out.print(SPACE, out.last);
        }
    }
}
