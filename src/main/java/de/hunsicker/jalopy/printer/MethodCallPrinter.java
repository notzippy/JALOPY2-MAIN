/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.JavaNodeHelper;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for method calls [<code>METHOD_CALL</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.7 $
 */
final class MethodCallPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new MethodCallPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new MethodCallPrinter object.
     */
    protected MethodCallPrinter()
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
     * Determines the last method call in the given method call chain.
     *
     * @param node the first method call in the method call chain.
     *
     * @return the last element in the chain.
     *
     * @since 1.0b7
     */
    public static JavaNode getLastMethodCall(JavaNode node)
    {
        JavaNode parent = node.getParent();

        switch (parent.getType())
        {
            case JavaTokenTypes.DOT :
                return getLastMethodCall(parent.getParent());

            default :
                return node;
        }
    }


    /**
     * {@inheritDoc}
     */
    public void print(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        // we need to keep track of the current continuation indentation state
        boolean continuation = out.continuation;

        boolean lastInChain = false;
        ParenthesesScope scope = null;

        AST first = node.getFirstChild();

        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            boolean wrapLines =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP);
            boolean forceWrappingForChainedCalls =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_CHAINED_METHOD_CALL,
                    ConventionDefaults.LINE_WRAP_AFTER_CHAINED_METHOD_CALL);

            if (
                (wrapLines || forceWrappingForChainedCalls)
                && JavaNodeHelper.isChained(first))
            {
                AST firstLink = JavaNodeHelper.getFirstChainLink(node);
                scope = (ParenthesesScope) out.state.parenScope.getFirst();

                // if no chained call for the current parentheses scope exists,
                // determine the offset of the first 'dot' and store it
                if (scope.chainCall == null)
                {
                    // store the node so wrapping can be performed
                    // (it will be done in DotPrinter.java)
                    scope.chainCall = firstLink;

                    AST child = firstLink.getFirstChild();

                    switch (child.getType())
                    {
                        // means qualified name, align under the last dot
                        case JavaTokenTypes.DOT :
                        {
                            TestNodeWriter tester = out.testers.get();
                            AST identifier = child.getFirstChild();
                            PrinterFactory.create(identifier, out).print(identifier, tester);
                            scope.chainOffset = out.column - 1 + tester.length;
                            out.testers.release(tester);

                            break;
                        }

                        // no qualification, standard indent
                        default :
                        {
                            scope.chainOffset = out.column - 1 + out.indentSize;

                            break;
                        }
                    }

                    if (out.newline)
                    {
                        /**
                         * @todo maybe we need to take care of the markers here?
                         */
                        scope.chainOffset += out.getIndentLength();
                    }

                    lastInChain = true;
                }
            }
        }

        // did we print an anonymous inner class?
        boolean innerClass = false;
        Marker marker = null;

        JavaNode parent = ((JavaNode) node).getParent();

        switch (parent.getType())
        {
            // add the marker just before the method call
            default :
                marker = out.state.markers.add();

                break;

            // we can't place the marker just before the method call because
            // then we would align our parameters too deeply. Instead we have
            // to determine the length of the type cast and substract it from
            // the current column position in order to place the marker
            // *before* the type cast
            //
            //      (JDialog)SwingUtilities.getWindowAncestor(
            //              ^            this),
            //
            //      (JDialog)SwingUtilities.getWindowAncestor(
            //      ^           this),
            case JavaTokenTypes.TYPECAST :

                AST next = parent.getFirstChild();
                AST type = null;

                /**
                 * @todo handle enclosing parentheses properly
                 */
                switch (next.getType())
                {
                    case JavaTokenTypes.LPAREN :
                        type = PrinterHelper.advanceToFirstNonParen(next);

                        break;

                    default :
                        type = next;

                        break;
                }

                AST identifier = type.getFirstChild();
                int length = 0;

                switch (identifier.getType())
                {
                    case JavaTokenTypes.DOT : 
                        length++;
                        AST temp = identifier.getFirstChild();
                        // Count all children
                        while (temp!=null && temp!=identifier) {
                            if (temp.getType() == JavaTokenTypes.DOT) {
                                temp = temp.getFirstChild();
                                length ++;
                            }
                            else {
                                // Add text length
                                length += temp.getText().length();
                                
                                // If no sibling go back up the chain
                                if (temp.getNextSibling()==null) {
                                    while(temp!=identifier) {
                                        temp = ((JavaNode)temp).getParent();
                                        if (temp.getNextSibling()!=null) {
                                            temp = temp.getNextSibling();
                                            break;
                                        }
                                    }
                                }
                                else {
                                    // Assign to next sibling
                                    temp = temp.getNextSibling();
                                }
                            }
                        }
                    case JavaTokenTypes.IDENT :
                        length = identifier.getText().length();

                        break;

                    case JavaTokenTypes.ARRAY_DECLARATOR :
                        length = identifier.getFirstChild().getText().length();

                        if (
                            AbstractPrinter.settings.getBoolean(
                                ConventionKeys.SPACE_BEFORE_BRACKETS_TYPES,
                                ConventionDefaults.SPACE_BEFORE_BRACKETS_TYPES))
                        {
                            length += 1;
                        }

                        break;

                    case JavaTokenTypes.LITERAL_boolean :
                        length = 7;

                        break;

                    case JavaTokenTypes.LITERAL_char :
                    case JavaTokenTypes.LITERAL_long :
                    case JavaTokenTypes.LITERAL_byte :
                        length = 4;

                        break;

                    case JavaTokenTypes.LITERAL_float :
                    case JavaTokenTypes.LITERAL_short :
                        length = 5;

                        break;

                    case JavaTokenTypes.LITERAL_int :
                        length = 3;

                        break;

                    case JavaTokenTypes.LITERAL_double :
                        length = 6;

                        break;

                    default :
                        throw new RuntimeException("unexpected TYPE, was " + type);
                }
                

                if (
                    AbstractPrinter.settings.getBoolean(
                        ConventionKeys.PADDING_CAST, ConventionDefaults.PADDING_CAST))
                {
                    length += 2;
                }

                if (
                    AbstractPrinter.settings.getBoolean(
                        ConventionKeys.SPACE_AFTER_CAST,
                        ConventionDefaults.SPACE_AFTER_CAST))
                {
                    length += 1;
                }

                // -3 because 1 for the usual 'step one back'
                //            2 for the parentheses
                marker = out.state.markers.add(out.line, (out.column - 3) - length);

                break;
        }

        logIssues(node, out);

        PrinterFactory.create(first, out).print(first, out);

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_BEFORE_METHOD_CALL_PAREN,
                ConventionDefaults.SPACE_BEFORE_METHOD_CALL_PAREN))
        {
            out.print(SPACE, JavaTokenTypes.WS);
        }

        LeftParenthesisPrinter.getInstance().print(node, out);

        AST elist = first.getNextSibling();

        PrinterFactory.create(elist, out).print(elist, out);

        // another trick to track inner class definitions
        if (out.last == JavaTokenTypes.CLASS_DEF)
        {
            innerClass = true;
        }

        AST rparen = elist.getNextSibling();
        PrinterFactory.create(rparen, out).print(rparen, out);

        // for the correct blank lines behaviour: we want blank lines
        // after inner classes but not after method calls
        if (innerClass)
        {
            out.last = JavaTokenTypes.CLASS_DEF;
        }

        out.state.markers.remove(marker);

        if (lastInChain && (scope != null))
        {
            scope.chainCall = null;
        }

        // reset continuation indentation if necessary (it could have been set
        // in DotPrinter.java)
        if (out.continuation && !continuation)
        {
            out.continuation = false;
        }
    }


    /**
     * Determines the topmost parent of the given expression node (contained in a chained
     * method call).
     *
     * @param node an EXPR node.
     *
     * @return the root node of the chain.
     *
     * @since 1.0b8
     */
    static AST getChainParent(JavaNode node)
    {
        JavaNode parent = node.getParent();

        switch (parent.getType())
        {
            case JavaTokenTypes.EXPR :

            /**
             * @todo maybe more operators make sense?
             */
            case JavaTokenTypes.PLUS :
                //case JavaTokenTypes.NOT_EQUAL:
                //case JavaTokenTypes.EQUAL:
                return getChainParent(parent);

            default :
                return parent;
        }
    }


    /**
     * Determines wether the given method call is the outmost method call or contained
     * within another method call chain.
     *
     * @param node a METHOD_CALL node.
     *
     * @return <code>true</code> if the given method call is the outmost method call.
     *
     * @since 1.0b7
     */
    static boolean isOuterMethodCall(AST node)
    {
        JavaNode call = getLastMethodCall((JavaNode) node);

        if (call.getPreviousSibling().getType() == JavaTokenTypes.LPAREN)
        {
            return false;
        }

        JavaNode expr = call.getParent();

        // if the topmost parent is no ELIST we know we can savely wrap and
        // align
        return getChainParent(expr).getType() != JavaTokenTypes.ELIST;
    }
}
