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
 * Base class for infix operator printers.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.11 $
 */
class InfixOperatorPrinter
    extends OperatorPrinter
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new InfixOperatorPrinter object.
     */
    protected InfixOperatorPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void print(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        print(node, true, out);
    }


    /**
     * Outputs the given node to the given stream.
     *
     * @param node node to print.
     * @param paddOperator if <code>true</code> a space will be printed before and after
     *        the operator.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    public void print(
        AST        node,
        boolean    paddOperator,
        NodeWriter out)
      throws IOException
    {
        AST lhsLeftParen = null;
        AST lhsRightParen = null;

        JavaNode lhs = null;
        JavaNode rhs = null; // rhs, maybe another left parenthesis

        int count = 0; // number of brace pairs

ITERATE:

        // determine whether the operands already have or need parentheses
        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.LPAREN :

                    if (count == 0) // first left parenthesis
                    {
                        lhsLeftParen = child;
                    }

                    count++;

                    break;

                case JavaTokenTypes.RPAREN :
                    count--;

                    if (count == 0) // last right parenthesis
                    {
                        lhsRightParen = child;

                        rhs = (JavaNode) child.getNextSibling();

                        break ITERATE;
                    }

                    break;

                default :
                    lhs = (JavaNode) child;

                    if (count == 0) // no parentheses
                    {
                        rhs = (JavaNode) child.getNextSibling();

                        break ITERATE;
                    }

                    break;
            }
        }

        AST rhsLeftParen = null;
        AST rhsRightParen = null;

        count = 0;

        for (AST child = rhs; child != null; child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.LPAREN :

                    if (count == 0) // first left parenthesis
                    {
                        rhsLeftParen = child;
                    }

                    count++;

                    break;

                case JavaTokenTypes.RPAREN :
                    count--;

                    if (count == 0) // last right parenthesis
                    {
                        rhsRightParen = child;
                    }

                    break;

                default :
                    rhs = (JavaNode) child;

                    break;
            }
        }

        boolean wrapLines =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP);
        boolean wrap = false; // actually perform line wrapping

        // both operands have parentheses
        if ((lhsLeftParen != null) && (rhsLeftParen != null))
        {
            // only perform line wrapping for certain operators
            wrap = canWrap(node, out);

            printImpl(
                (JavaNode) node, lhs, rhs, lhsLeftParen, lhsRightParen, rhsLeftParen,
                rhsRightParen, wrapLines && wrap, paddOperator, out);
        }
        else
        {
            boolean insertParentheses =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.INSERT_EXPRESSION_PARENTHESIS,
                    ConventionDefaults.INSERT_EXPRESSION_PARENTHESIS);

            // should we add parentheses to make precedence obvious?
            if (insertParentheses)
            {
                if (lhsLeftParen == null)
                {
                    if (getPrecedence(lhs) > getPrecedence(node))
                    {
                        if (out.mode == NodeWriter.MODE_DEFAULT)
                        {
                            addParentheses(lhs, out);
                            lhsLeftParen = lhs.getPreviousSibling();
                            lhsRightParen = lhs.getNextSibling();
                        }
                        else
                        {
                            // we can't add the parentheses if in test mode
                            // because the node would then be wrongly treated in
                            // default mode, just spit out two chars to get the
                            // length right
                            out.print(LPAREN, out.last);
                            out.print(RPAREN, out.last);
                        }
                    }
                }

                if (rhsLeftParen == null)
                {
                    if (getPrecedence(rhs) > getPrecedence(node))
                    {
                        if (out.mode == NodeWriter.MODE_DEFAULT)
                        {
                            addParentheses(rhs, out);
                            rhsLeftParen = rhs.getPreviousSibling();
                            rhsRightParen = rhs.getNextSibling();
                        }
                        else
                        {
                            // we can't add the parentheses if in test mode
                            // because the node would then be wrongly treated in
                            // default mode, just spit out two chars to get the
                            // length right
                            out.print(LPAREN, out.last);
                            out.print(RPAREN, out.last);
                        }
                    }
                }
            }

            // only perform line wrapping for certain operators
            if (wrapLines)
            {
                wrap = canWrap(node, out);
            }

            printImpl(
                (JavaNode) node, lhs, rhs, lhsLeftParen, lhsRightParen, rhsLeftParen,
                rhsRightParen, wrapLines && wrap, paddOperator, out);
        }
    }
    private void wrapNewLine(JavaNode lhs, NodeWriter out, boolean islhs) throws IOException {
        if (
                (out.mode == NodeWriter.MODE_DEFAULT)
                &&out.newline==false
                &&lhs.getFirstChild()!=null 
                && AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_PAREN_GROUPING,
                    ConventionDefaults.LINE_WRAP_PAREN_GROUPING)) {
//        	JavaNode parent = getParentByTypes((JavaNode) node,JavaTokenTypes.LITERAL_if);
        	JavaNode parent = lhs.getParent();
        	while(true) {
	        	switch(parent.getType()) {
	        		case JavaTokenTypes.LOR:
	        		case JavaTokenTypes.LAND:
	        			parent = parent.getParent();
	        			continue;
	        	}
	        	break;
        	}
        	boolean allow=false;
        	switch(lhs.getPreviousSibling().getType()) {
        		case JavaTokenTypes.LPAREN:
        		case JavaTokenTypes.LAND:
        		case JavaTokenTypes.LOR:
        			allow=true;
        	}

            if (parent.getParent().getType()==JavaTokenTypes.LITERAL_if 
            	//&& lhs.getPreviousSibling().getType()==JavaTokenTypes.LPAREN
            	&& allow)
            {
		    	out.printNewline();
            }
        }
    	    	
    }

    /**
     * Does the actual printing.
     *
     * @param operator operator node to print.
     * @param lhs left hand side expression.
     * @param rhs right hand side expression.
     * @param lhsLeftParen DOCUMENT ME!
     * @param lhsRightParen DOCUMENT ME!
     * @param rhsLeftParen DOCUMENT ME!
     * @param rhsRightParen DOCUMENT ME!
     * @param wrapLines if <code>true</code> performs line wrapping, if necessary.
     * @param paddOperator if <code>true</code> adds padding whitespace around the
     *        operator.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printImpl(
        JavaNode   operator,
        JavaNode   lhs,
        JavaNode   rhs,
        AST        lhsLeftParen,
        AST        lhsRightParen,
        AST        rhsLeftParen,
        AST        rhsRightParen,
        boolean    wrapLines,
        boolean    paddOperator,
        NodeWriter out)
      throws IOException
    {

        if (lhsLeftParen == null)
        {
            PrinterFactory.create(lhs, out).print(lhs, out);
        }
        else
        {
            printWithParentheses(lhs, lhsLeftParen, lhsRightParen, out);
        }

        boolean continuation = out.continuation;
        boolean continuationIndent =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_CONTINUATION_OPERATOR,
                ConventionDefaults.INDENT_CONTINUATION_OPERATOR);

        if (continuationIndent && !continuation)
        {
            switch (operator.getType())
            {
                case JavaTokenTypes.PLUS :
                case JavaTokenTypes.MINUS :
                    break;

                default :
                    out.continuation = true;

                    break;
            }
        }

        boolean wrapped = false;

        if (out.newline)
        {
            wrapped = true;

            printIndentation(out);
        }

        boolean wrapBeforeOperator =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_BEFORE_OPERATOR,
                ConventionDefaults.LINE_WRAP_BEFORE_OPERATOR);
        boolean commentAfter = operator.hasCommentsAfter();

        // no line wrap before operator means that we maybe have to add
        // whitespace around the operator
        if (!wrapBeforeOperator)
        {
            if (printCommentsBefore(operator,NodeWriter.NEWLINE_NO, out) && out.newline)
            {
                printIndentation(out);
            }

            if (paddOperator)
            {
                out.print(SPACE, JavaTokenTypes.WS);
                out.print(operator.getText(), operator.getType());
            }
            else
            {
                out.print(operator.getText(), operator.getType());
            }
        }

        if (commentAfter)
        {
            printCommentsAfter(
                operator, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out);
            wrapped = true;

            printIndentation(out);
        }
        else if (wrapLines)
        {
            boolean wrapAll = false;

            if (!out.state.parenScope.isEmpty())
            {
                ParenthesesScope scope =
                    (ParenthesesScope) out.state.parenScope.getFirst();
                wrapAll = scope.wrap;
            }

            if (wrapAll) // force wrapping for all operators
            {
                switch (operator.getType())
                {
                    case JavaTokenTypes.PLUS :
                    case JavaTokenTypes.MINUS :

                        if (!wrapped)
                        {
                            TestNodeWriter tester = out.testers.get();
                            PrinterFactory.create(rhs, out).print(rhs, tester);
                            wrapped =
                                performWrap(
                                    tester.length + ((rhsLeftParen != null) ? 2
                                                                            : 0), operator,
                                    wrapBeforeOperator, paddOperator, out);
                            out.testers.release(tester);
                        }

                        break;

                    default :
                        out.printNewline();
                        printIndentation(out);
                        wrapped = true;

                        break;
                }
            }
            else
            {
                if (!wrapped)
                {
                    TestNodeWriter tester = out.testers.get();
                    PrinterFactory.create(rhs, out).print(rhs, tester);
                    wrapped =
                        performWrap(
                            tester.length + ((rhsLeftParen != null) ? 2
                                                                    : 0), operator,
                            wrapBeforeOperator, paddOperator, out);
                    out.testers.release(tester);
                }
            }
        }

        if (wrapBeforeOperator)
        {
            if (printCommentsBefore(operator, NodeWriter.NEWLINE_NO, out) && out.newline)
            {
                printIndentation(out);
            }

            if (paddOperator)
            {
                if (!wrapped)
                {
                    out.print(SPACE, JavaTokenTypes.WS);
                }

                out.print(operator.getText(), operator.getType());
                out.print(SPACE, JavaTokenTypes.WS);
            }
            else
            {
                out.print(operator.getText(), operator.getType());
            }
        }
        else if (paddOperator && !wrapped)
        {
            out.print(SPACE, JavaTokenTypes.WS);
        }
        
        if (out.mode == NodeWriter.MODE_DEFAULT) {
    	wrapNewLine(lhs, out, true);
        // Similar to lhs check to see if a new line is required on rhs
    	wrapNewLine(rhs, out, false);
        }
        

        if (rhsLeftParen == null)
        {
            PrinterFactory.create(rhs, out).print(rhs, out);
        }
        else
        {
            printWithParentheses(rhs, rhsLeftParen, rhsRightParen, out);
        }

        // if the rhs expression is followed by an enline comment, we have to
        // take care that any non-operator element that follows, will be
        // correctly indented, e.g.
        //
        // String test = "multi" + // comment 1
        //               "line"    // comment 2
        //               ;
        //
        if (rhs.hasCommentsAfter())
        {
            JavaNode parent = operator.getParent();

            switch (parent.getType())
            {
                case JavaTokenTypes.EXPR: // means this is the last operator
                    AST next = parent.getNextSibling();

                    if (next != null)
                    {
                        switch (next.getType())
                        {
                            case JavaTokenTypes.RPAREN:
                                break;

                            default:
                                printIndentation(out);
                                break;
                        }
                    }
                    else
                    {
                        printIndentation(out);
                    }

                    break;
            }
        }

        if (continuationIndent && !continuation)
        {
            out.continuation = false;
        }
    }
}
