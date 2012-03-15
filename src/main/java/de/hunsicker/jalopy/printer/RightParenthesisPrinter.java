/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;
import java.util.List;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for the right parenthesis [<code>RPAREN</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 *
 * @since 1.0b9
 */
final class RightParenthesisPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final RightParenthesisPrinter INSTANCE = new RightParenthesisPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new RightParenthesisPrinter object.
     */
    private RightParenthesisPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return the sole instance of this class.
     */
    public static RightParenthesisPrinter getInstance()
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
        if (
            (out.mode == NodeWriter.MODE_DEFAULT)
            && AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_PAREN_GROUPING,
                ConventionDefaults.LINE_WRAP_PAREN_GROUPING))
        {
            List parentheses = out.state.parentheses;

            for (int i = 0, size = parentheses.size(); i < size; i++)
            {
                Object parenthesis = parentheses.get(i);

                if (parenthesis == node)
                {
                    out.printNewline();

                    if (
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP))
                    {
                        out.state.markers.remove(out.state.markers.getLast());
                    }
                    else
                    {
                        out.unindent();
                    }

                    printIndentation(out);
                    parentheses.remove(i);

                    break;
                }
            }
        }

        printCommentsBefore(node, NodeWriter.NEWLINE_NO, out);

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.PADDING_PAREN, ConventionDefaults.PADDING_PAREN))
        {
            out.print(SPACE_RPAREN, JavaTokenTypes.RPAREN);
        }
        else
        {
            out.print(RPAREN, JavaTokenTypes.RPAREN);
        }

        printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);
    }
}
