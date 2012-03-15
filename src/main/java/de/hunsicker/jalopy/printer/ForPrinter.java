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
 * Printer for <code>for</code> loops [<code>LITERAL_for</code>].
 * <pre class="snippet">
 * <strong>for </strong>(<em>initialization</em>; <em>Boolean-expression</em>; <em>step</em>;)
 * {
 *    <em>statement</em>
 * }
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.9 $
 */
final class ForPrinter
    extends BlockStatementPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ForPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ForPrinter object.
     */
    protected ForPrinter()
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

        int offset = 1;

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_BEFORE_STATEMENT_PAREN,
                ConventionDefaults.SPACE_BEFORE_STATEMENT_PAREN))
        {
            offset = out.print(FOR_SPACE, JavaTokenTypes.LITERAL_for);
        }
        else
        {
            offset = out.print(FOR, JavaTokenTypes.LITERAL_for);
        }

        trackPosition((JavaNode) node, out.line, offset, out);

        AST lparen = node.getFirstChild();
        PrinterFactory.create(lparen, out).print(lparen, out);

        Marker marker = out.state.markers.add();

        AST forNode = lparen.getNextSibling();
        AST rparen = forNode.getNextSibling();
        switch (forNode.getType()) {
            case JavaTokenTypes.FOR_INIT:
                rparen = for_init(forNode,out,marker);
            	break;
            case JavaTokenTypes.FOR_EACH_CLAUSE:
                rparen = for_each(forNode,out,marker);
                break;
            default :
                throw new IllegalArgumentException("no viable printer for -- " + forNode);
                
        }

        PrinterFactory.create(rparen, out).print(rparen, out);

        out.state.markers.remove(marker);

        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            out.state.paramLevel--;
            out.state.parenScope.removeFirst();
        }

        out.last = JavaTokenTypes.LITERAL_for;

        AST body = rparen.getNextSibling();

        switch (body.getType())
        {
            case JavaTokenTypes.SLIST :
                PrinterFactory.create(body, out).print(body, out);

                break;

            default :

                // insert braces manually
                if (
                    AbstractPrinter.settings.getBoolean(
                        ConventionKeys.BRACE_INSERT_FOR,
                        ConventionDefaults.BRACE_INSERT_FOR))
                {
                    out.printLeftBrace(
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.BRACE_NEWLINE_LEFT,
                            ConventionDefaults.BRACE_NEWLINE_LEFT), NodeWriter.NEWLINE_YES);
                    PrinterFactory.create(body, out).print(body, out);
                    out.printRightBrace();
                }
                else
                {
                    out.printNewline();
                    out.indent();
                    PrinterFactory.create(body, out).print(body, out);
                    out.unindent();
                }
        }

        // to simplify line wrapping we always indicate braces
        out.last = JavaTokenTypes.RCURLY;        
    }
    /**
     * Creates the for each node
     * 
     * @param forNode
     * @param out
     * @param marker
     */
    private AST for_each(AST forNode, NodeWriter out, Marker marker) throws IOException{
                if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            out.state.paramLevel++;
            out.state.parenScope.addFirst(new ParenthesesScope(out.state.paramLevel));
        }
        AST forEachClause = forNode.getFirstChild();
        AST expresion = forEachClause.getNextSibling();
        PrinterFactory.create(forEachClause, out).print(forEachClause,out);
        out.print(SPACE_COLON_SPACE,forEachClause.getType());
        printChildren(expresion,out);
        return forNode.getNextSibling();
    }

        private AST for_init(AST forInit, NodeWriter out, Marker marker) throws IOException {
        AST firstSemi = forInit.getNextSibling();
        AST forCond = firstSemi.getNextSibling();
        AST secondSemi = forCond.getNextSibling();
        AST forIter = secondSemi.getNextSibling();

        int lineLength =
            AbstractPrinter.settings.getInt(
                ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);
        boolean indentDeep =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP);
        boolean firstWrap = false;

        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            out.state.paramLevel++;
            out.state.parenScope.addFirst(new ParenthesesScope(out.state.paramLevel));

            if (
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_LEFT_PAREN,
                    ConventionDefaults.LINE_WRAP_AFTER_LEFT_PAREN))
            {
                TestNodeWriter tester = out.testers.get();

                AST child = forInit.getFirstChild();

                for (AST c = child; c != null; c = c.getNextSibling())
                {
                    PrinterFactory.create(c, out).print(c, tester);
                }

                child = forCond.getFirstChild();

                if (child != null)
                {
                    PrinterFactory.create(child, out).print(child, tester);
                }

                child = forIter.getFirstChild();

                if (child != null)
                {
                    PrinterFactory.create(child, out).print(child, tester);
                }

                if ((out.column + tester.length) > lineLength)
                {
                    firstWrap = true;
                }

                out.testers.release(tester);
            }
        }

        printForInit(forInit, firstWrap, out);

        boolean wrapAll =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_PARAMS_EXCEED,
                ConventionDefaults.LINE_WRAP_PARAMS_EXCEED);
        boolean spaceAfterSemi =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_AFTER_SEMICOLON,
                ConventionDefaults.SPACE_AFTER_SEMICOLON);

        out.continuation =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_CONTINUATION_BLOCK,
                ConventionDefaults.INDENT_CONTINUATION_BLOCK);

        boolean secondWrap = false;

        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            if (firstWrap && wrapAll)
            {
                secondWrap = true;
            }
            else
            {
                TestNodeWriter tester = out.testers.get();

                AST child = forCond.getFirstChild();

                if (child != null)
                {
                    PrinterFactory.create(child, out).print(child, tester);

                    // add enough space for semis before and after
                    tester.length += (spaceAfterSemi ? 3
                                                     : 2);
                }

                if ((out.column + tester.length) > lineLength)
                {
                    secondWrap = true;
                }

                out.testers.release(tester);
            }
        }

        out.print(SEMI, JavaTokenTypes.FOR_INIT);
        printCommentsAfter(firstSemi, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

        printForCond(forCond, secondWrap, out);

        boolean thirdWrap = false;

        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            if (firstWrap && wrapAll)
            {
                thirdWrap = true;
            }
            else
            {
                TestNodeWriter tester = out.testers.get();

                AST child = forIter.getFirstChild();

                if (child != null)
                {
                    PrinterFactory.create(child, out).print(child, tester);

                    // add enough space for semis before and parenthesis after
                    tester.length += (spaceAfterSemi ? 5
                                                     : 3);
                }

                if ((out.column + tester.length) > lineLength)
                {
                    thirdWrap = true;
                }

                out.testers.release(tester);
            }
        }

        out.print(SEMI, JavaTokenTypes.FOR_INIT);
        printCommentsAfter(secondSemi, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

        printForIter(forIter, thirdWrap, out);

        out.continuation = false;

        if (
            (firstWrap || secondWrap || thirdWrap)
            && AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_BEFORE_RIGHT_PAREN,
                ConventionDefaults.LINE_WRAP_BEFORE_RIGHT_PAREN))
        {
            if (!out.newline)
            {
                out.printNewline();

                if (indentDeep)
                {
                    out.print(
                        out.getString(marker.column - 1 - out.getIndentLength()),
                        JavaTokenTypes.WS);
                }
            }

            out.print(EMPTY_STRING, JavaTokenTypes.WS);
        }
        
        return forIter.getNextSibling();

    }


    /**
     * Prints the condition part of the for loop.
     *
     * @param node the condition part node of the loop.
     * @param wrap if <code>true</code> print a newline before the part.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printForCond(
        AST        node,
        boolean    wrap,
        NodeWriter out)
      throws IOException
    {
        if (node.getFirstChild() == null)
        {
            return;
        }

        if (wrap)
        {
            out.printNewline();
            printIndentation(out);
        }
        else if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_AFTER_SEMICOLON,
                ConventionDefaults.SPACE_AFTER_SEMICOLON))
        {
            out.print(SPACE, JavaTokenTypes.FOR_INIT);
        }

        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            PrinterFactory.create(child, out).print(child, out);
        }
    }


    /**
     * Prints the initialization part of the for loop.
     *
     * @param node the initialization part node of the loop.
     * @param wrap True if should wrap
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occurred.
     */
    private void printForInit(
        AST        node,
        boolean    wrap,
        NodeWriter out)
      throws IOException
    {
        if (wrap)
        {
            out.printNewline();
            printIndentation(out);
        }

        AST child = node.getFirstChild();

        if (child != null)
        {
            printVariableDefs(node, out);
        }
    }


    /**
     * Prints the iteration part of the for loop.
     *
     * @param node the iteration part node of the loop.
     * @param wrap if <code>true</code> print a newline before the part.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occurred.
     */
    private void printForIter(
        AST        node,
        boolean    wrap,
        NodeWriter out)
      throws IOException
    {
        AST elist = node.getFirstChild();

        if (elist == null)
        {
            return;
        }

        if (wrap)
        {
            out.printNewline();
            printIndentation(out);
        }
        else if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_AFTER_SEMICOLON,
                ConventionDefaults.SPACE_AFTER_SEMICOLON))
        {
            out.print(SPACE, JavaTokenTypes.FOR_INIT);
        }

        boolean spaceAfterComma =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_SEMICOLON);
        String comma = spaceAfterComma ? COMMA_SPACE
                                       : COMMA;

        // we don't use ParametersPrinter.java because of the
        // automated added parentheses, which would lead to compile
        // errors here
        for (
            AST element = elist.getFirstChild(); element != null;
            element = element.getNextSibling())
        {
            switch (element.getType())
            {
                case JavaTokenTypes.COMMA :
                    out.print(comma, JavaTokenTypes.COMMA);

                    break;

                default :
                    PrinterFactory.create(element, out).print(element, out);

                    break;
            }
        }
    }


    /**
     * Outputs a single variable definition of the initialization part of the for loop.
     *
     * @param node a node.
     * @param out the output stream to write to.
     * @param printType
     *
     * @throws IOException if an I/O error occured.
     */
    private void printVariableDef(
        AST        node,
        NodeWriter out,
        boolean    printType)
      throws IOException
    {
        // the initialization part of the loop can either consist of variable
        // definitions or assignment expressions
        // for (int i = 0;;){...}  vs.  for (i = 0;;){...}
        switch (node.getType())
        {
            // one or more assignment expressions
            case JavaTokenTypes.ELIST :

                boolean spaceAfterComma =
                    AbstractPrinter.settings.getBoolean(
                        ConventionKeys.SPACE_AFTER_COMMA,
                        ConventionDefaults.SPACE_AFTER_COMMA);
                String comma = spaceAfterComma ? COMMA_SPACE
                                               : COMMA;

                /**
                 * TODO is this still valid?
                 */

                // don't use ParametersPrinter.java because of the added
                // parenthesis
                for (
                    AST param = node.getFirstChild(); param != null;
                    param = param.getNextSibling())
                {
                    PrinterFactory.create(param, out).print(param, out);

                    /**
                     * @todo space after comma
                     */
                    if (param.getNextSibling() != null)
                    {
                        out.print(comma, JavaTokenTypes.COMMA);
                    }
                }

                return;
        }

        AST modifier = node.getFirstChild();
        AST type = modifier.getNextSibling();

        if (printType)
        {
            PrinterFactory.create(modifier, out).print(modifier, out);
            PrinterFactory.create(type, out).print(type, out);
        }

        AST identifier = type.getNextSibling();

        // only insert whitespace if we haven't multiple variable defs in the
        // initialization part
        if (out.last != JavaTokenTypes.COMMA)
        {
            out.print(SPACE, out.last);
        }

        PrinterFactory.create(identifier, out).print(identifier, out);

        AST assign = identifier.getNextSibling();

        if (assign != null)
        {
            // if we're printing multiple variable defs but the last one
            // has no assignment then a semi appears which is bad as we always
            // output one in printForCond(), so ignore it
            switch (assign.getType())
            {
                case JavaTokenTypes.SEMI :
                    break;

                default :
                    PrinterFactory.create(assign, out).print(assign, out);

                    break;
            }
        }
    }


    /**
     * Outputs all variable definitions/initializations of the initialization part of the
     * for loop.
     *
     * @param node a VARIABLE_DEF node.
     * @param out the output stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printVariableDefs(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        AST child = node.getFirstChild();
        boolean spaceAfterComma =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_COMMA);
        String comma = spaceAfterComma ? COMMA_SPACE
                                       : COMMA;

        switch (child.getType())
        {
            case JavaTokenTypes.VARIABLE_DEF :
                printVariableDef(child, out, true);

                for (
                    child = child.getNextSibling(); child != null;
                    child = child.getNextSibling())
                {
                    out.print(comma, JavaTokenTypes.COMMA);
                    printVariableDef(child, out, false);
                }

                break;

            case JavaTokenTypes.ELIST :

                for (AST var = child.getFirstChild(); var != null;
                    var = var.getNextSibling())
                {
                    switch (child.getType())
                    {
                        case JavaTokenTypes.COMMA :
                            out.print(comma, JavaTokenTypes.COMMA);

                            break;

                        default :
                            PrinterFactory.create(var, out).print(var, out);

                            break;
                    }
                }

                break;
        }
    }
}
