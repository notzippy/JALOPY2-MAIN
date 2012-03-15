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
 * Printer for type casts.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class TypeCastPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new TypeCastPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new TypeCastPrinter object.
     */
    protected TypeCastPrinter()
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
        AST type = node.getFirstChild();

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.PADDING_CAST, ConventionDefaults.PADDING_CAST))
        {
            out.print(LPAREN_SPACE, JavaTokenTypes.LPAREN);
            PrinterFactory.create(type, out).print(type, out);
            out.print(SPACE_RPAREN, JavaTokenTypes.RPAREN);
        }
        else
        {
            out.print(LPAREN, JavaTokenTypes.LPAREN);
            PrinterFactory.create(type, out).print(type, out);
            out.print(RPAREN, JavaTokenTypes.RPAREN);
        }

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_AFTER_CAST, ConventionDefaults.SPACE_AFTER_CAST))
        {
            out.print(SPACE, out.last);
        }

        for (AST child = type.getNextSibling(); child != null;
            child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }
    }
}
