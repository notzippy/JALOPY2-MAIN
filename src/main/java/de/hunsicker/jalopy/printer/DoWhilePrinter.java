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
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for do-while loops [<code>LITERAL_do</code>].
 * <pre class="snippet">
 * <strong>do </strong>
 * {
 *     <em>statement</em>
 * } <strong>while</strong> (<em>Boolean-expression</em>);
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
final class DoWhilePrinter
    extends BlockStatementPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new DoWhilePrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new DoWhilePrinter object.
     */
    protected DoWhilePrinter()
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
        super.print(node, out);

        int offset = out.print(DO, JavaTokenTypes.IDENT);

        trackPosition((JavaNode) node, out.line, offset, out);

        printCommentsAfter(node, out);

        AST body = node.getFirstChild();

        switch (body.getType())
        {
            case JavaTokenTypes.SLIST :
                out.last = JavaTokenTypes.LITERAL_do;
                PrinterFactory.create(body, out).print(body, out);

                break;

            default :

                // insert braces manually
                if (
                    AbstractPrinter.settings.getBoolean(
                        ConventionKeys.BRACE_INSERT_DO_WHILE,
                        ConventionDefaults.BRACE_INSERT_DO_WHILE))
                {
                    out.printLeftBrace(
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.BRACE_NEWLINE_LEFT,
                            ConventionDefaults.BRACE_NEWLINE_LEFT), NodeWriter.NEWLINE_YES);
                    out.last = JavaTokenTypes.IDENT;
                    PrinterFactory.create(body, out).print(body, out);
                    out.printRightBrace(
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.BRACE_NEWLINE_RIGHT,
                            ConventionDefaults.BRACE_NEWLINE_RIGHT));
                }
                else
                {
                    out.printNewline();
                    out.indent();
                    out.last = JavaTokenTypes.IDENT;
                    PrinterFactory.create(body, out).print(body, out);
                    out.unindent();
                }
        }

        if (out.last == JavaTokenTypes.RCURLY)
        {
            out.print(
                out.getString(
                    AbstractPrinter.settings.getInt(
                        ConventionKeys.INDENT_SIZE_BRACE_RIGHT_AFTER,
                        ConventionDefaults.INDENT_SIZE_BRACE_RIGHT_AFTER)),
                JavaTokenTypes.WS);
        }

        AST keyword = body.getNextSibling();
        printCommentsBefore(keyword, NodeWriter.NEWLINE_NO, out);

        offset = out.print(WHILE, JavaTokenTypes.LITERAL_while);

        trackPosition((JavaNode) keyword, out.line, offset, out);

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_BEFORE_STATEMENT_PAREN,
                ConventionDefaults.SPACE_BEFORE_STATEMENT_PAREN))
        {
            out.print(SPACE, JavaTokenTypes.LITERAL_while);
        }

        AST lparen = keyword.getNextSibling();
        AST rparen = printExpressionList(lparen, false, out);
        AST semi = rparen.getNextSibling();

        PrinterFactory.create(semi, out).print(semi, out);

        out.last = JavaTokenTypes.RCURLY;
    }
}
