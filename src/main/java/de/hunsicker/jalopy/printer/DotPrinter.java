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
 * Printer for dot separated stuff (like qualified identifiers, chained method calls...)
 * [<code>DOT</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.8 $
 */
final class DotPrinter
    extends OperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new DotPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new DotPrinter object.
     */
    protected DotPrinter()
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
        AST rhs = printLeftHandSide(node, out);

        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            /**
             * @todo add switch to disable wrapping along dots alltogether
             */
            boolean wrapLines =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP);
            boolean forceWrappingForChainedCalls =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_CHAINED_METHOD_CALL,
                    ConventionDefaults.LINE_WRAP_AFTER_CHAINED_METHOD_CALL);

            if (wrapLines || forceWrappingForChainedCalls)
            {
                align(node, out);
            }
        }

        out.print(DOT, JavaTokenTypes.DOT);

        printRightHandSide(rhs, out);
    }


    /**
     * Determines the length of a single method call contained in a method call chain.
     *
     * @param dot the DOT node.
     * @param call the parent of the DOT node, a METHOD_CALL node.
     * @param lastCall the last METHOD_CALL node of the chain (but the first METHOD_CALL
     *        code of the AST tree!).
     * @param testers The testers
     *
     * @return the length of the chained method call.
     *
     * @throws IOException if an I/O error occurred.
     *
     * @since 1.0b8
     */
    private int getLengthOfChainedCall(
                                       NodeWriter out,
        AST         dot,
        JavaNode    call,
        AST         lastCall,
        WriterCache testers)
      throws IOException
    {
        TestNodeWriter tester = testers.get();
        tester.reset(out,false);

        if (lastCall != call)
        {
            AST elist = dot.getNextSibling();
            PrinterFactory.create(elist, tester).print(elist, tester);
        }
        else
        {
            AST elist = lastCall.getFirstChild().getNextSibling();
            PrinterFactory.create(elist, tester).print(elist, tester);
        }

        AST child = dot.getFirstChild();

        switch (child.getType())
        {
            case JavaTokenTypes.METHOD_CALL :

                AST next = child.getNextSibling();
                PrinterFactory.create(next, tester).print(next, tester);

                break;

            default : // means the last node in the AST (but the first call in
                      // the chain)
                PrinterFactory.create(child, tester).print(child, tester);

                break;
        }

        // add +1 for the dot
        int result = tester.maxColumn + 1;
        if (tester.line>1) {
            result = -1;
        }

        testers.release(tester);

        return result;
    }


    /**
     * Aligns the rhs node, if necessary. This is currently only implemented for chained
     * method calls like <code>scrollPane.getViewport().setBackground(Color.red)</code>
     * or <code>resultSetRow[i].field[0].substring(0, 2)</code>.
     *
     * @param node a DOT node.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b7
     */
    private void align(
        AST        node,
        NodeWriter out)
      throws IOException
    {

        boolean continuationIndent =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_CONTINUATION_OPERATOR,
                ConventionDefaults.INDENT_CONTINUATION_OPERATOR);

        // was a chained call detected in the current scope?
        // (the detection happens in MethodCallPrinter.java)
        if (out.state.parenScope.size()>0 &&  ((ParenthesesScope) out.state.parenScope.getFirst()).chainCall != null)
        {
            ParenthesesScope scope = (ParenthesesScope) out.state.parenScope.getFirst();
            JavaNode parent = ((JavaNode) node).getParent();

            switch (parent.getType())
            {
                //case JavaTokenTypes.INDEX_OP:
                case JavaTokenTypes.METHOD_CALL :

                    boolean align =
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.ALIGN_METHOD_CALL_CHAINS,
                            ConventionDefaults.ALIGN_METHOD_CALL_CHAINS);

                    if (parent != scope.chainCall)
                    {
                        // force wrap after each call?
                        if (
                            AbstractPrinter.settings.getBoolean(
                                ConventionKeys.LINE_WRAP_AFTER_CHAINED_METHOD_CALL,
                                ConventionDefaults.LINE_WRAP_AFTER_CHAINED_METHOD_CALL))
                        {
                            if (MethodCallPrinter.isOuterMethodCall(parent))
                            {
                                // simply wrap and align all chained calls
                                // under the first one
                                out.printNewline();

                                int indentLength = out.getIndentLength();

                                if (align)
                                {
                                    out.print(
                                        out.getString(
                                            (scope.chainOffset > indentLength)
                                            ? (scope.chainOffset - indentLength)
                                            : scope.chainOffset), JavaTokenTypes.WS);
                                }
                                else if (continuationIndent)
                                {
                                    out.continuation = true;
                                    printIndentation(out);
                                }
                                else
                                {
                                    printIndentation(out);
                                }

                                return;
                            }
                        }
                        else
                        {
                            int lineLength =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.LINE_LENGTH,
                                    ConventionDefaults.LINE_LENGTH);

                            // we're already beyond the maximal line length,
                            // time to wrap
                            if (out.column > lineLength)
                            {
                                out.printNewline();

                                if (continuationIndent)
                                {
                                    out.continuation = true;
                                }

                                indent(align, scope, out);

                                return;
                            }

                            AST first = MethodCallPrinter.getLastMethodCall(parent);

                            // if this is the last node in the chain
                            if (first == parent && false)
                            {
                                AST c = node.getFirstChild().getNextSibling();

                                TestNodeWriter tester = out.testers.get();
                                tester.reset(out,false);
                                PrinterFactory.create(c, tester).print(c, tester);

                                // and it does not exceed the line length
                                if (tester.maxColumn < lineLength && tester.line==1)
                                {
                                    out.testers.release(tester);

                                    // prefer wrapping along the parameters
                                    return;
                                }

                                out.testers.release(tester);
                            }

                            int length =
                                getLengthOfChainedCall(out, node, parent, first, out.testers);

                            // if this chain element would exceed the maximal
                            // line length, perform wrapping
                            if (( length > lineLength || length<-1))
                            {
                                if (continuationIndent)
                                {
                                    out.continuation = true;
                                }

                                out.printNewline();

                                indent(align, scope, out);
                            }
                        }
                    }

                    break;
            }
        }
        else if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_BEFORE_OPERATOR,
                ConventionDefaults.LINE_WRAP_BEFORE_OPERATOR))
        {
            switch (((JavaNode) node).getParent().getType())
            {
                case JavaTokenTypes.DOT :
                    break;

                case JavaTokenTypes.METHOD_CALL : // last link of the chain (first in the tree)

                    int lineLength =
                        AbstractPrinter.settings.getInt(
                            ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);

                    if ((out.column + 1) > lineLength)
                    {
                        out.printNewline();

                        if (continuationIndent)
                        {
                            out.continuation = true;
                        }

                        printIndentation(out);
                    }
                    else
                    {
                        AST n = node.getFirstChild();

                        switch (n.getType())
                        {
                            case JavaTokenTypes.LPAREN :
SEEK_FORWARD: 
                                for (; n != null; n = n.getNextSibling())
                                {
                                    switch (n.getType())
                                    {
                                        case JavaTokenTypes.RPAREN :
                                            n = n.getNextSibling();

                                            break SEEK_FORWARD;
                                    }
                                }

                                break;

                            default :
                                n = n.getNextSibling();

                                break;
                        }

                        TestNodeWriter tester = out.testers.get();
                        tester.reset(out, false);
                        
                        PrinterFactory.create(n, tester).print(n, tester);
                        /*
                        TODO Figure out why this consumes so much memory
                        n = node.getNextSibling();
                        if (n!=null) {
                            PrinterFactory.create(n, tester).print(n, tester);
                        }
                        */

                        if (tester.maxColumn > lineLength || tester.line>1)
                        {
                            out.printNewline();
                            Marker current=out.state.markers.add(0,
                                out.line);
//                            ,
//                                0,true,out
//                                );

                            if (continuationIndent)
                            {
                                out.continuation = true;
                            }

                            printIndentation(out);
                        }

                        out.testers.release(tester);
                    }

                    break;
            }
        }
    }


    /**
     * Prints the indenation whitespace for wrapped lines.
     *
     * @param align if <code>true</code> enough whitespace will be printed to align under
     *        the '.' of the previous chain member.
     * @param scope curent scope info.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O occured.
     *
     * @since 1.0b9
     */
    private void indent(
        boolean          align,
        ParenthesesScope scope,
        NodeWriter       out)
      throws IOException
    {
        if (align)
        {
            int indentLength = out.getIndentLength();

            out.print(
                out.getString(
                    ((scope.chainOffset > indentLength)
                    ? (scope.chainOffset - indentLength)
                    : scope.chainOffset)), JavaTokenTypes.WS);
        }
        else if (
            out.continuation
            || (!out.continuation
            && AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_CONTINUATION_OPERATOR,
                ConventionDefaults.INDENT_CONTINUATION_OPERATOR)))
        {
            printIndentation(out);
        }
        else
        {
            printIndentation(out);
        }
    }
}
