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
 * Printer for relational operators.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class RelationalOperatorPrinter
    extends InfixOperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new RelationalOperatorPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new RelationalOperatorPrinter object.
     */
    protected RelationalOperatorPrinter()
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
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     * @param out DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void print(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        switch (node.getType())
        {
            case JavaTokenTypes.LITERAL_instanceof :
                super.print(node, true, out);

                break;

            default :
                super.print(
                    node,
                    AbstractPrinter.settings.getBoolean(
                        ConventionKeys.PADDING_RELATIONAL_OPERATORS,
                        ConventionDefaults.PADDING_RELATIONAL_OPERATORS), out);
        }
    }
}
