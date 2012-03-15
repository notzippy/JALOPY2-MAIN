/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;
import de.hunsicker.jalopy.language.JavaNodeHelper;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Base class for operator printers.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.11 $
 */
abstract class OperatorPrinter
    extends AbstractPrinter
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new OperatorPrinter object.
     */
    protected OperatorPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Performs a line wrap, if necessary.
     *
     * @param length the length of the text that will be printed.
     * @param node the operator node
     * @param wrapBefore <code>true</code> indicates that the line wrap is performed
     *        before the operator, <code>false</code> means operator the operator.
     * @param paddOperator <code>true</code> indicates that operator padding is enabled.
     * @param out stream to write to.
     *
     * @return <code>true</code> if a line wrap was performed.
     *
     * @throws IOException if an I/O exception occured.
     */
    protected boolean performWrap(
        int        length,
        AST        node,
        boolean    wrapBefore,
        boolean    paddOperator,
        NodeWriter out)
      throws IOException
    {
        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            int offset = out.column + length + (paddOperator ? 2
                                                             : 0);

            if (wrapBefore)
            {
                offset += node.getText().length();
            }

            int lineLength =
                AbstractPrinter.settings.getInt(
                    ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);

            if ((out.column >= lineLength) || (offset > lineLength))
            {
                out.printNewline();
                printIndentation(out);

                return true;
            }
        }

        return false;
    }


    /**
     * Prints the left-hand side of the given assignment.
     *
     * @param node an ASSIGN node.
     * @param out stream to write to.
     *
     * @return the first node of the right-hand side for the assignment.
     *
     * @throws IOException if an I/O occured.
     * @throws IllegalStateException DOCUMENT ME!
     *
     * @since 1.0b9
     */
    protected AST printLeftHandSide(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        AST first = node.getFirstChild();

        switch (first.getType())
        {
            case JavaTokenTypes.LPAREN :

                int count = 0; // number of enclosing parentheses

                for (AST child = first; child != null; child = child.getNextSibling())
                {
                    PrinterFactory.create(child, out).print(child, out);

                    switch (child.getType())
                    {
                        case JavaTokenTypes.LPAREN :
                            count++;

                            break;

                        case JavaTokenTypes.RPAREN :
                            count--;

                            // we've printed the last right parenthesis
                            if (count == 0)
                            {
                                return child.getNextSibling();
                            }

                            break;
                    }
                }

                throw new IllegalStateException("missing closing parenthesis");

            default :
                PrinterFactory.create(first, out).print(first, out);

                return first.getNextSibling();
        }
    }


    /**
     * Prints the right-hand side of an assignment.
     *
     * @param node the first node of the right-hand side.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O occured.
     *
     * @since 1.0b9
     */
    protected void printRightHandSide(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        for (AST child = node; child != null; child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }
    }


    /**
     * Returns the precedence level of the given node as an int value. Higher values
     * means a higher precedence level.
     *
     * @param node a node.
     *
     * @return the precedence level of the given node. If the given node does not denote
     *         an operator node, this method always returns <code>0</code>.
     *
     * @since 1.0b9
     */
    int getPrecedence(AST node)
    {
        int result = 0;

        // the following switch is clearly modeled
        // after the Java operator precedences table:
        //
        // lowest
        // (13)  = *= /= %= += -= <<= >>= >>>= &= ^= |=
        // (12)  ?:
        // (11)  ||
        // (10)  &&
        // ( 9)  |
        // ( 8)  ^
        // ( 7)  &
        // ( 6)  == !=
        // ( 5)  < <= > >=
        // ( 4)  << >>
        // ( 3)  +(binary) -(binary)
        // ( 2)  * / %
        // ( 1)  ++ -- +(unary) -(unary) ~ ! (type)
        // highest
        switch (node.getType())
        {
            case JavaTokenTypes.BOR_ASSIGN :
                result = 1;

                break;

            case JavaTokenTypes.BXOR_ASSIGN :
                result = 2;

                break;

            case JavaTokenTypes.BAND_ASSIGN :
                result = 3;

                break;

            case JavaTokenTypes.BSR_ASSIGN :
                result = 4;

                break;

            case JavaTokenTypes.SR_ASSIGN :
                result = 5;

                break;

            case JavaTokenTypes.SL_ASSIGN :
                result = 6;

                break;

            case JavaTokenTypes.MINUS_ASSIGN :
                result = 7;

                break;

            case JavaTokenTypes.PLUS_ASSIGN :
                result = 8;

                break;

            case JavaTokenTypes.MOD_ASSIGN :
                result = 9;

                break;

            case JavaTokenTypes.DIV_ASSIGN :
                result = 10;

                break;

            case JavaTokenTypes.STAR_ASSIGN :
                result = 11;

                break;

            case JavaTokenTypes.ASSIGN :
                result = 12;

                break;

            case JavaTokenTypes.COLON :
                result = 13;

                break;

            case JavaTokenTypes.QUESTION :
                result = 14;

                break;

            case JavaTokenTypes.LOR :
                result = 15;

                break;

            case JavaTokenTypes.LAND :
                result = 16;

                break;

            case JavaTokenTypes.BOR :
                result = 17;

                break;

            case JavaTokenTypes.BXOR :
                result = 18;

                break;

            case JavaTokenTypes.BAND :
                result = 19;

                break;

            case JavaTokenTypes.NOT_EQUAL :
                result = 20;

                break;

            case JavaTokenTypes.EQUAL :
                result = 21;

                break;

            case JavaTokenTypes.GE :
                result = 22;

                break;

            case JavaTokenTypes.LE :
                result = 23;

                break;

            case JavaTokenTypes.GT :
                result = 24;

                break;

            case JavaTokenTypes.LT :
                result = 25;

                break;

            case JavaTokenTypes.SR :
                result = 26;

                break;

            case JavaTokenTypes.SL :
                result = 27;

                break;

            case JavaTokenTypes.MINUS :
                result = 28;

                break;

            case JavaTokenTypes.PLUS :
                result = 29;

                break;

            case JavaTokenTypes.MOD :
                result = 30;

                break;

            case JavaTokenTypes.DIV :
                result = 31;

                break;

            case JavaTokenTypes.STAR :
                result = 32;

                break;

            /*case JavaTokenTypes.LNOT:
                result = 33;

                break;

            case JavaTokenTypes.BNOT:
                result = 34;

                break;*/
            /*case JavaTokenTypes.UNARY_MINUS:
                result = 35;
                break;
            case JavaTokenTypes.UNARY_PLUS:
                result = 36;
                break;
            case JavaTokenTypes.DEC :
                result = 37;
                break;
            case JavaTokenTypes.INC :
                result = 38;
                break;*/
        }

        return result;
    }


    /**
     * Indicates whether the given operator may cause a line wrap.
     *
     * @param operator operator node.
     * @param out stream to write to.
     *
     * @return <code>true</code> if the given operator can cause a line wrap.
     */
    static boolean canWrap(
        AST        operator,
        NodeWriter out)
    {
        if (out.mode != NodeWriter.MODE_DEFAULT)
        {
            return false;
        }

        //if (out.state.paramList || out.state.paramLevel < 2 || isHigherLevel((JavaNode)operator))
        //{
        // only allow certain operators to wrap
        switch (operator.getType())
        {
            // Mathematical operators
            case JavaTokenTypes.PLUS :
            case JavaTokenTypes.MINUS :

            // Logical operators
            case JavaTokenTypes.LAND :
            case JavaTokenTypes.LOR :

            // Bitwise operators
            case JavaTokenTypes.BAND :
            case JavaTokenTypes.BOR :
            case JavaTokenTypes.BXOR :
                // Prefix operators
                //case JavaTokenTypes.BNOT :
                return true;
        }

        //}
        return false;
    }


    boolean isTopMost(JavaNode node)
    {
        JavaNode parent = node.getParent();

        switch (parent.getType())
        {
            case JavaTokenTypes.EXPR :
                return true;
        }

        return false;
    }


    /**
     * Adds parentheses to the given operand.
     *
     * @param operand an operand of an operator.
     *
     * @since 1.0b9
     */
    void addParentheses(JavaNode operand, NodeWriter out)
    {
        JavaNode parent = operand.getParent();
        JavaNode lparen = (JavaNode) out.getJavaNodeFactory().create(JavaTokenTypes.LPAREN, LPAREN);
        JavaNode rparen = (JavaNode) out.getJavaNodeFactory().create(JavaTokenTypes.RPAREN, RPAREN);

        if (parent.getFirstChild() == operand)
        {
            parent.setFirstChild(lparen);
            lparen.setPreviousSibling(parent);
        }

        lparen.setParent(parent);
        lparen.setNextSibling(operand);

        rparen.setParent(parent);
        rparen.setPreviousSibling(operand);
        rparen.setNextSibling(operand.getNextSibling());

        operand.setPreviousSibling(lparen);
        operand.setNextSibling(rparen);
    }


    /**
     * Prints the given operator node, which has enclosing parentheses.
     *
     * @param node an operator node.
     * @param leftParen the left parenthesis.
     * @param rightParen the right parenthesis.
     * @param out stream to write to.
     *
     * @return <code>true</code> if an endline comment was printed behind the right
     *         parenthesis.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    boolean printWithParentheses(
        JavaNode   node,
        AST        leftParen,
        AST        rightParen,
        NodeWriter out)
      throws IOException
    {
        JavaNode n = JavaNodeHelper.getLastChild(node);

        if ((n == null) || n.hasCommentsAfter())
        {
            LeftParenthesisPrinter.getInstance().print(leftParen, out);

            if (out.mode == NodeWriter.MODE_DEFAULT)
            {
                out.state.parenScope.addFirst(new ParenthesesScope(out.state.paramLevel));
            }

            Marker marker = out.state.markers.add();
            PrinterFactory.create(node, out).print(node, out);
            out.state.markers.remove(marker);

            /*if (!isTopMost(node))
            {
                Marker marker = out.state.markers.add();
                out.state.paramLevel++;
                PrinterFactory.create(node).print(node, out);
                out.state.paramLevel--;
                out.state.markers.remove(marker);
            }
            else
            {
                PrinterFactory.create(node).print(node, out);
            }*/
            RightParenthesisPrinter.getInstance().print(rightParen, out);

            if (out.mode == NodeWriter.MODE_DEFAULT)
            {
                out.state.parenScope.removeFirst();
            }

            return false;
        }
        // the last node contains an endline comment, so we have to
        // print the closing parenthesis before the comment
        CommonHiddenStreamToken t = n.getHiddenAfter();
        n.setHiddenAfter(null);

        Marker marker = out.state.markers.add();
        LeftParenthesisPrinter.getInstance().print(leftParen, out);

        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            out.state.parenScope.addFirst(new ParenthesesScope(out.state.paramLevel));
        }


        PrinterFactory.create(node, out).print(node, out);

        /*if (!isTopMost(node))
        {
            Marker marker = out.state.markers.add();
            out.state.paramLevel++;
            PrinterFactory.create(node).print(node, out);
            out.state.paramLevel--;
            out.state.markers.remove(marker);
            RightParenthesisPrinter.getInstance().print(rightParen, out);

        }
        else
        {
            PrinterFactory.create(node).print(node, out);
        }*/
        RightParenthesisPrinter.getInstance().print(rightParen, out);
        out.state.markers.remove(marker);

        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            out.state.parenScope.removeFirst();
        }

        n.setHiddenAfter(t);
        printCommentsAfter(n, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

        return true;
    }


    /**
     * Indicates whether the given operator node is at a higher level than some other
     * operator, i.e. the node has no parent that is itself an operator node.
     *
     * @param operator the operator node to check.
     *
     * @return <code>true</code> if the given operator is at a higher level than some
     *         other operator.
     *
     * @since 1.0b9
     */
//    TODO private static boolean isHigherLevel(JavaNode operator)
//    {
//        switch (operator.getParent().getType())
//        {
//            case JavaTokenTypes.BXOR_ASSIGN :
//            case JavaTokenTypes.BAND_ASSIGN :
//            case JavaTokenTypes.BSR_ASSIGN :
//            case JavaTokenTypes.SR_ASSIGN :
//            case JavaTokenTypes.SL_ASSIGN :
//            case JavaTokenTypes.MINUS_ASSIGN :
//            case JavaTokenTypes.PLUS_ASSIGN :
//            case JavaTokenTypes.MOD_ASSIGN :
//            case JavaTokenTypes.DIV_ASSIGN :
//            case JavaTokenTypes.STAR_ASSIGN :
//            case JavaTokenTypes.ASSIGN :
//            case JavaTokenTypes.COLON :
//            case JavaTokenTypes.QUESTION :
//            case JavaTokenTypes.LOR :
//            case JavaTokenTypes.LAND :
//            case JavaTokenTypes.BOR :
//            case JavaTokenTypes.BXOR :
//            case JavaTokenTypes.BAND :
//            case JavaTokenTypes.NOT_EQUAL :
//            case JavaTokenTypes.EQUAL :
//            case JavaTokenTypes.GE :
//            case JavaTokenTypes.LE :
//            case JavaTokenTypes.GT :
//            case JavaTokenTypes.LT :
//            case JavaTokenTypes.SR :
//            case JavaTokenTypes.SL :
//            case JavaTokenTypes.MINUS :
//            case JavaTokenTypes.PLUS :
//            case JavaTokenTypes.MOD :
//            case JavaTokenTypes.DIV :
//            case JavaTokenTypes.STAR :
//            case JavaTokenTypes.LNOT :
//            case JavaTokenTypes.BNOT :
//            case JavaTokenTypes.UNARY_MINUS :
//            case JavaTokenTypes.UNARY_PLUS :
//            case JavaTokenTypes.DEC :
//            case JavaTokenTypes.INC :
//
//                for (
//                    AST child = operator.getFirstChild(); child != null;
//                    child = child.getNextSibling())
//                {
//                    switch (child.getType())
//                    {
//                        case JavaTokenTypes.BXOR_ASSIGN :
//                        case JavaTokenTypes.BAND_ASSIGN :
//                        case JavaTokenTypes.BSR_ASSIGN :
//                        case JavaTokenTypes.SR_ASSIGN :
//                        case JavaTokenTypes.SL_ASSIGN :
//                        case JavaTokenTypes.MINUS_ASSIGN :
//                        case JavaTokenTypes.PLUS_ASSIGN :
//                        case JavaTokenTypes.MOD_ASSIGN :
//                        case JavaTokenTypes.DIV_ASSIGN :
//                        case JavaTokenTypes.STAR_ASSIGN :
//                        case JavaTokenTypes.ASSIGN :
//                        case JavaTokenTypes.COLON :
//                        case JavaTokenTypes.QUESTION :
//                        case JavaTokenTypes.LOR :
//                        case JavaTokenTypes.LAND :
//                        case JavaTokenTypes.BOR :
//                        case JavaTokenTypes.BXOR :
//                        case JavaTokenTypes.BAND :
//                        case JavaTokenTypes.NOT_EQUAL :
//                        case JavaTokenTypes.EQUAL :
//                        case JavaTokenTypes.GE :
//                        case JavaTokenTypes.LE :
//                        case JavaTokenTypes.GT :
//                        case JavaTokenTypes.LT :
//                        case JavaTokenTypes.SR :
//                        case JavaTokenTypes.SL :
//                        case JavaTokenTypes.MINUS :
//                        case JavaTokenTypes.PLUS :
//                        case JavaTokenTypes.MOD :
//                        case JavaTokenTypes.DIV :
//                        case JavaTokenTypes.STAR :
//                            //case JavaTokenTypes.LNOT :
//                            //case JavaTokenTypes.BNOT :
//                            //case JavaTokenTypes.UNARY_MINUS :
//                            //case JavaTokenTypes.UNARY_PLUS :
//                            //case JavaTokenTypes.DEC :
//                            //case JavaTokenTypes.INC :
//                            return true;
//                    }
//                }
//
//                return false;
//
//            default : // no parent operator
//
//                return true;
//        }
//    }


    /**
     * Indicates whether the given operator node is at a lower level that some containing
     * expression statement, i.e. the given node has at least one child that is itself
     * an operator node.
     *
     * @param operator the operator node to check.
     *
     * @return <code>true</code> if the given node is a lower level node.
     */
//    TODO private static boolean isLowerLevel(JavaNode operator)
//    {
//        for (
//            AST child = operator.getFirstChild(); child != null;
//            child = child.getNextSibling())
//        {
//            switch (child.getType())
//            {
//                case JavaTokenTypes.BXOR_ASSIGN :
//                case JavaTokenTypes.BAND_ASSIGN :
//                case JavaTokenTypes.BSR_ASSIGN :
//                case JavaTokenTypes.SR_ASSIGN :
//                case JavaTokenTypes.SL_ASSIGN :
//                case JavaTokenTypes.MINUS_ASSIGN :
//                case JavaTokenTypes.PLUS_ASSIGN :
//                case JavaTokenTypes.MOD_ASSIGN :
//                case JavaTokenTypes.DIV_ASSIGN :
//                case JavaTokenTypes.STAR_ASSIGN :
//                case JavaTokenTypes.ASSIGN :
//                case JavaTokenTypes.COLON :
//                case JavaTokenTypes.QUESTION :
//                case JavaTokenTypes.LOR :
//                case JavaTokenTypes.LAND :
//                case JavaTokenTypes.BOR :
//                case JavaTokenTypes.BXOR :
//                case JavaTokenTypes.BAND :
//                case JavaTokenTypes.NOT_EQUAL :
//                case JavaTokenTypes.EQUAL :
//                case JavaTokenTypes.GE :
//                case JavaTokenTypes.LE :
//                case JavaTokenTypes.GT :
//                case JavaTokenTypes.LT :
//                case JavaTokenTypes.SR :
//                case JavaTokenTypes.SL :
//                case JavaTokenTypes.MINUS :
//                case JavaTokenTypes.PLUS :
//                case JavaTokenTypes.MOD :
//                case JavaTokenTypes.DIV :
//                case JavaTokenTypes.STAR :
//                case JavaTokenTypes.LNOT :
//                case JavaTokenTypes.BNOT :
//                case JavaTokenTypes.UNARY_MINUS :
//                case JavaTokenTypes.UNARY_PLUS :
//                case JavaTokenTypes.DEC :
//                case JavaTokenTypes.INC :
//                    return true;
//            }
//        }
//
//        return false;
//    }
}
