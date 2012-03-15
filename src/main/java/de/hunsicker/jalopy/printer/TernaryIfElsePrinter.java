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
 * Printer for the ternary operator (<code>? :</code>) [<code>QUESTION</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
final class TernaryIfElsePrinter
    extends OperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new TernaryIfElsePrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new TernaryIfPrinter object.
     */
    private TernaryIfElsePrinter()
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
        Marker marker = out.state.markers.add();

        // print first operand
        AST secondOperand = printOperand(node.getFirstChild(), marker, out);

        Marker m = printQuestionMark(node, secondOperand, out);

        // print the second operand
        JavaNode colon = (JavaNode) printOperand(secondOperand, marker, out);

        printColon(colon, m, out);

        // print the third operand
        printOperand(colon.getNextSibling(), marker, out);

        out.state.markers.remove(marker);
    }


    /**
     * Gets the next operand of the ternary expression.
     *
     * @param node the node of the ternary expression. Either LPAREN, EXPR or an
     *        operator.
     *
     * @return the next operand of the ternary expression.
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     *
     * @since 1.b9
     */
    private JavaNode getNextOperand(AST node)
    {
        for (AST child = node; child != null; child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.LPAREN :
                    break;

                default :
                    return (JavaNode) child;
            }
        }

        throw new IllegalArgumentException("not part of operand -- " + node);
    }


    /**
     * Determines whether the given node needs parentheses to make expression precedence
     * clear.
     *
     * @param node operand node to check.
     *
     * @return <code>true</code> if the operand needs parentheses.
     *
     * @since 1.0b8
     */
    private boolean needParentheses(JavaNode node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.BXOR_ASSIGN :
            case JavaTokenTypes.BAND_ASSIGN :
            case JavaTokenTypes.BSR_ASSIGN :
            case JavaTokenTypes.SR_ASSIGN :
            case JavaTokenTypes.SL_ASSIGN :
            case JavaTokenTypes.MINUS_ASSIGN :
            case JavaTokenTypes.PLUS_ASSIGN :
            case JavaTokenTypes.MOD_ASSIGN :
            case JavaTokenTypes.DIV_ASSIGN :
            case JavaTokenTypes.STAR_ASSIGN :
            case JavaTokenTypes.ASSIGN :
            case JavaTokenTypes.COLON :
            case JavaTokenTypes.QUESTION :
            case JavaTokenTypes.LOR :
            case JavaTokenTypes.LAND :
            case JavaTokenTypes.BOR :
            case JavaTokenTypes.BXOR :
            case JavaTokenTypes.BAND :
            case JavaTokenTypes.NOT_EQUAL :
            case JavaTokenTypes.EQUAL :
            case JavaTokenTypes.GE :
            case JavaTokenTypes.LE :
            case JavaTokenTypes.GT :
            case JavaTokenTypes.LT :
            case JavaTokenTypes.SR :
            case JavaTokenTypes.SL :
            case JavaTokenTypes.MINUS :
            case JavaTokenTypes.PLUS :
            case JavaTokenTypes.MOD :
            case JavaTokenTypes.DIV :
            case JavaTokenTypes.STAR :
            case JavaTokenTypes.LNOT :
            case JavaTokenTypes.BNOT :
            case JavaTokenTypes.UNARY_MINUS :
            case JavaTokenTypes.UNARY_PLUS :
            case JavaTokenTypes.DEC :
            case JavaTokenTypes.INC :
            case JavaTokenTypes.LITERAL_instanceof :
                return true;
        }

        return false;
    }


    /**
     * Prints the colon.
     *
     * @param colon COLON node.
     * @param marker marks the position before the question mark. If this value is not
     *        <code>null</code> the colon will alignment under the question mark is
     *        forced; otherwise the current indentation scheme will be used.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b9
     */
    private void printColon(
        JavaNode   colon,
        Marker     marker,
        NodeWriter out)
      throws IOException
    {
        boolean wrapLines =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP)
            && (out.mode == NodeWriter.MODE_DEFAULT);
        boolean wrapBeforeColon =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.ALIGN_TERNARY_VALUES,
                ConventionDefaults.ALIGN_TERNARY_VALUES);

        if (out.newline) // line already wrapped, just indent
        {
            printIndentation(marker, out);
        }
        else if (wrapBeforeColon) // force line wrap/align
        {
            out.printNewline();

            if (marker == null)
            {
                printIndentation(out);
            }
            else
            {
                printIndentation(marker, out);
            }
        }
        else if (wrapLines) // check whether wrap/align necessary
        {
            TestNodeWriter tester = out.testers.get();

            AST thirdOp = getNextOperand(colon.getNextSibling());
            PrinterFactory.create(thirdOp, out).print(thirdOp, tester);

            // only wrap and align if necessary
            if (
                (tester.length + out.column) > AbstractPrinter.settings.getInt(
                    ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH))
            {
                out.printNewline();

                if (marker == null)
                {
                    printIndentation(out);
                }
                else
                {
                    printIndentation(marker, out);
                }
            }
            else
            {
                out.print(SPACE, out.last);
            }

            out.testers.release(tester);
        }
        else // line wrapping disabled
        {
            out.print(SPACE, out.last);
        }

        out.print(COLON, JavaTokenTypes.COLON);

        if (
            !printCommentsAfter(
                colon, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out)
            && !out.newline)
        {
            out.print(SPACE, JavaTokenTypes.COLON);
        }
        else
        {
            // add +2 to align the third operand under the second

            /*out.print(out.getString(((marker.column > indentLength)
                                         ? (marker.column - indentLength)
                                         : marker.column) + 2),
                      JavaTokenTypes.WS);*/
        }
    }


    /**
     * Prints an operand of the ternary expression.
     *
     * @param node the first node of the ternary expression. Either a LPAREN, EXPR or an
     *        operator.
     * @param marker marker that marks the position before the first operand.
     * @param out stream to write to.
     *
     * @return the next operand of the ternary expression. Returns <code>null</code> if
     *         the third operand was printed.
     *
     * @throws IOException if an I/O error occured.
     * @throws IllegalStateException DOCUMENT ME!
     *
     * @since 1.0b9
     */
    private AST printOperand(
        AST        node,
        Marker     marker,
        NodeWriter out)
      throws IOException
    {
        // does the operand already contain enclosing parentheses?
        boolean parentheses = false;

        for (AST child = node; child != null; child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.LPAREN :
                    parentheses = true;
                    PrinterFactory.create(child, out).print(child, out);

                    break;

                default :

                    if (parentheses || (node.getFirstChild() == child))
                    {
                        PrinterFactory.create(child, out).print(child, out);
                    }
                    else if (
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.INSERT_EXPRESSION_PARENTHESIS,
                            ConventionDefaults.INSERT_EXPRESSION_PARENTHESIS)
                        && needParentheses((JavaNode) child))
                    {
                        JavaNode operator = (JavaNode) child;

                        if (out.mode == NodeWriter.MODE_DEFAULT)
                        {
                            addParentheses(operator, out);

                            AST leftParen = operator.getPreviousSibling();
                            PrinterFactory.create(leftParen, out).print(leftParen, out);

                            //AST rightParen = operator.getNextSibling();
                            PrinterFactory.create(child, out).print(child, out);

                            //printWithParentheses(operator,leftParen, rightParen, out);
                        }
                        else
                        {
                            out.print(LPAREN, out.last);
                            PrinterFactory.create(child, out).print(child, out);

                            //out.print(RPAREN, out.last);
                        }
                    }
                    else
                    {
                        PrinterFactory.create(child, out).print(child, out);
                    }

                    for (
                        child = child.getNextSibling(); child != null;
                        child = child.getNextSibling())
                    {
                        switch (child.getType())
                        {
                            case JavaTokenTypes.RPAREN :
                                PrinterFactory.create(child, out).print(child, out);

                                break;

                            default :
                                return child;
                        }
                    }

                    // the third operand does not have a sibling
                    return null;
            }
        }

        // should never happen if the tree was correctly build
        throw new IllegalStateException();
    }


    /**
     * Prints the question mark.
     *
     * @param node QUESTION node.
     * @param secondOperand the second operand of the ternary expression.
     * @param out stream to write to.
     *
     * @return marker that marks the position before the question mark, <code>null</code>
     *         if ternary operator aligning is disabled.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b9
     */
    private Marker printQuestionMark(
        AST        node,
        AST        secondOperand,
        NodeWriter out)
      throws IOException
    {
        boolean wrapLines =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP)
            && (out.mode == NodeWriter.MODE_DEFAULT);
        boolean wrapBeforeQuestion =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.ALIGN_TERNARY_EXPRESSION,
                ConventionDefaults.ALIGN_TERNARY_EXPRESSION);

        if (out.newline) // line already wrapped, just indent
        {
            printIndentation(out);
        }
        else if (wrapBeforeQuestion) // force line wrap/align
        {
            out.printNewline();
            printIndentation(out);
        }
        else if (wrapLines) // check whether wrap/align necessary
        {
            TestNodeWriter tester = out.testers.get();

            AST secondOp = getNextOperand(secondOperand);
            PrinterFactory.create(secondOp, out).print(secondOp, tester);

            // wrap and align if necessary (+3 for the colon between the
            // second and third operator)
            if (
                (tester.length + out.column + 3) > AbstractPrinter.settings.getInt(
                    ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH))
            {
                out.printNewline();
                printIndentation(out);
            }
            else // no line wrap necessary
            {
                out.print(SPACE, out.last);
            }

            out.testers.release(tester);
        }
        else // line wrapping disabled
        {
            out.print(SPACE, out.last);
        }

        Marker marker = null;

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.ALIGN_TERNARY_OPERATOR,
                ConventionDefaults.ALIGN_TERNARY_OPERATOR)
            && (wrapLines || wrapBeforeQuestion))
        {
            marker = out.state.markers.add();
        }

        out.print(QUESTION, JavaTokenTypes.QUESTION);

        if (
            !printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out)
            && !out.newline)
        {
            out.print(SPACE, JavaTokenTypes.COLON);
        }

        return marker;
    }
}
