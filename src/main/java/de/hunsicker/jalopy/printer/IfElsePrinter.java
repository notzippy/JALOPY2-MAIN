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
 * Printer for if-else statements [<code>LITERAL_if</code>, <code>LITERAL_else</code>].
 * <pre class="snippet">
 * <strong>if</strong> (<em>Boolean-expression</em>)
 * {
 *     <em>statement</em>
 * }
 * <strong>else if</strong> (<em>Boolean-expression</em>)
 * {
 *     <em>statement</em>
 * }
 * <strong>else</strong>
 * {
 *    <em>statement</em>
 * }
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.7 $
 */
final class IfElsePrinter
    extends BlockStatementPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new IfElsePrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new IfElsePrinter object.
     */
    protected IfElsePrinter()
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

        // print space between else and if:
        // if { ... } else if { ... }
        switch (out.last)
        {
            case JavaTokenTypes.LITERAL_else :
                out.print(SPACE, JavaTokenTypes.LITERAL_if);

                break;
        }

        boolean spaceBefore =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_BEFORE_STATEMENT_PAREN,
                ConventionDefaults.SPACE_BEFORE_STATEMENT_PAREN);

        int offset = 1;

        if (spaceBefore)
        {
            offset = out.print(IF_SPACE, JavaTokenTypes.LITERAL_if);
        }
        else
        {
            offset = out.print(IF, JavaTokenTypes.LITERAL_if);
        }

        trackPosition((JavaNode) node, out.line, offset, out);

        AST lparen = node.getFirstChild();

        boolean insertBraces =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_INSERT_IF_ELSE,
                ConventionDefaults.BRACE_INSERT_IF_ELSE);

        JavaNode rparen = printExpressionList(lparen, insertBraces, out);
        AST body = rparen.getNextSibling();

        boolean leftBraceNewline =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_NEWLINE_LEFT, ConventionDefaults.BRACE_NEWLINE_LEFT);
        boolean rightBraceNewline =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_NEWLINE_RIGHT, ConventionDefaults.BRACE_NEWLINE_RIGHT);
        boolean hasBraces = body.getType() == JavaTokenTypes.SLIST;

        out.last = JavaTokenTypes.LITERAL_if;

        JavaNode next = (JavaNode) body.getNextSibling();

        if (hasBraces)
        {
            PrinterFactory.create(body, out).print(body, out);
        }
        else // no braces, single statement
        {
            if (insertBraces) // insert braces manually
            {
                if (out.pendingComment == null)
                {
                    out.printLeftBrace(
                        leftBraceNewline && !out.newline, NodeWriter.NEWLINE_YES,
                        NodeWriter.NEWLINE_YES);
                }
                else
                {
                    out.printLeftBrace(NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO);
                    rparen.setHiddenAfter(out.pendingComment);
                    printCommentsAfter(
                        rparen, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out);
                    out.pendingComment = null;
                }

                PrinterFactory.create(body, out).print(body, out);
                out.printRightBrace(rightBraceNewline || (next == null));
            }
            else
            {
                if (!out.newline)
                    out.printNewline();

                out.indent();
                PrinterFactory.create(body, out).print(body, out);
                out.unindent();
            }
        }

        // print the else-if or else part
        if (next != null)
        {
            printCommentsBefore(next, out);

            if (!out.newline && (out.last == JavaTokenTypes.RCURLY))
            {
                out.print(
                    out.getString(
                        AbstractPrinter.settings.getInt(
                            ConventionKeys.INDENT_SIZE_BRACE_RIGHT_AFTER,
                            ConventionDefaults.INDENT_SIZE_BRACE_RIGHT_AFTER)),
                    JavaTokenTypes.WS);
            }

            offset = out.print(ELSE, JavaTokenTypes.LITERAL_else);

            trackPosition(next, out.line, offset, out);

            JavaNode block = (JavaNode) next.getNextSibling();

            switch (block.getType())
            {
                case JavaTokenTypes.SLIST : // block
                    // print the endline comment for the else
                    printCommentsAfter(
                        next, NodeWriter.NEWLINE_NO, !leftBraceNewline, out);
                    // either we print a LITERAL_if or LITERAL_else but
                    // we don't care as both will lead to the same result
                    out.last = JavaTokenTypes.LITERAL_if;
                    PrinterFactory.create(block, out).print(block, out);

                    break;

                case JavaTokenTypes.LITERAL_if : // the else-if 'if'
                    out.last = JavaTokenTypes.LITERAL_else;
                    print(block, out);

                    break;

                default : // single expression

                    if (insertBraces)
                    {
                        out.pendingComment = next.getCommentAfter();

                        if (out.pendingComment == null)
                        {
                            printCommentsAfter(
                                next, NodeWriter.NEWLINE_NO, !leftBraceNewline, out);
                            out.printLeftBrace(
                                rightBraceNewline, NodeWriter.NEWLINE_NO,
                                !rightBraceNewline && !out.newline);
                            out.printNewline();
                        }
                        else
                        {
                            out.printLeftBrace(
                                NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO);
                            printCommentsAfter(
                                next, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out);
                        }

                        PrinterFactory.create(block, out).print(block, out);
                        out.printRightBrace();
                    }
                    else
                    {
                        printCommentsAfter(
                            next, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);
                        out.printNewline();
                        out.indent();
                        PrinterFactory.create(block, out).print(block, out);
                        out.unindent();
                    }
            }
        }

        // do as if always braces printed for the correct blank lines behaviour
        out.last = JavaTokenTypes.RCURLY;
    }


    /**
     * Determines if an endline comment will be printed before the given else node.
     *
     * @param elseNode else node to check for endline comments before.
     *
     * @return <code>true</code> if an endline comment will be printed before the given
     *         else node.
     */
    private boolean isCommentBefore(JavaNode elseNode)
    {
        JavaNode slist = elseNode.getPreviousSibling();
        JavaNode rcurly = null;

        for (AST child = slist.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.RCURLY :

                    if (child.getNextSibling() == null)
                    {
                        rcurly = (JavaNode) child;
                    }

                    break;
            }
        }

        if (rcurly != null)
        {
            return rcurly.hasCommentsAfter();
        }
        
        return false;
    }
}
