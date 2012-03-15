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
 * Printer for assignments [<code>ASSIGN</code>]. These represent either assignment
 * statements or variable declaration assignments.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.11 $
 */
final class AssignmentPrinter
    extends OperatorPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    static final int OFFSET_NONE = -1;

    /** Singleton. */
    private static final AssignmentPrinter INSTANCE = new AssignmentPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new AssignmentPrinter object.
     */
    private AssignmentPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return the sole instance of this class.
     */
    public static AssignmentPrinter getInstance()
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
        print(node, false, out);
    }


    /**
     * Prints the given node.
     *
     * @param node node to print.
     * @param wrapAfterAssign if <code>true</code> the value won't be aligned after the
     *        '=', but rather the indentation level will be increased and the value will
     *        be printed like a block.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    public void print(
        AST        node,
        boolean    wrapAfterAssign,
        NodeWriter out)
      throws IOException
    {
        logIssues(node, out);
        printCommentsBefore(node, out);

        boolean wrapLines =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP)
            && (out.mode == NodeWriter.MODE_DEFAULT);
        boolean preferWrapAfterLeftParen =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_AFTER_LEFT_PAREN,
                ConventionDefaults.LINE_WRAP_AFTER_LEFT_PAREN);
        boolean preferWrapAfterAssign =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_AFTER_ASSIGN,
                ConventionDefaults.LINE_WRAP_AFTER_ASSIGN);
        boolean padding =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.PADDING_ASSIGNMENT_OPERATORS,
                ConventionDefaults.PADDING_ASSIGNMENT_OPERATORS);
        int lineLength =
            AbstractPrinter.settings.getInt(
                ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);
        boolean indentStandard =
            !AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP);
        
        boolean alignVarAssigns = AbstractPrinter.settings.getBoolean(
                            ConventionKeys.ALIGN_VAR_DECL_ASSIGNS,
                            AbstractPrinter.settings.getBoolean(
                            ConventionKeys.ALIGN_VAR_ASSIGNS,
                            ConventionDefaults.ALIGN_VAR_ASSIGNS));

        AST expr = node.getFirstChild();

        if (isPartOfDeclaration(node))
        {
            if (wrapLines)
            {
                JavaNode parent = ((JavaNode) node).getParent();

                if (
                    !wrapAfterAssign
                    && alignVarAssigns && 
                        !((JavaNode)node).getParent().hasJavadocComment(AbstractPrinter.settings.getBoolean(
                ConventionKeys.DONT_COMMENT_JAVADOC_WHEN_ML,
                ConventionDefaults.DONT_COMMENT_JAVADOC_WHEN_ML)))
                {
                    if (isNewChunk(parent, JavaTokenTypes.VARIABLE_DEF))
                    {
                        out.state.assignOffset = OFFSET_NONE;
                    }

                    alignAssignment(node, true, out);
                }

                boolean indent =
                    (indentStandard || wrapAfterAssign || preferWrapAfterAssign
                    || preferWrapAfterLeftParen);

                if (indent)
                {
                    out.indent();
                }

                Marker marker = null;

                if (wrapAfterAssign) // force wrap after assign
                {
                    if (padding)
                    {
                        out.print(SPACE_ASSIGN, JavaTokenTypes.ASSIGN);
                    }
                    else
                    {
                        out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                    }

                    if (!printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out))
                        out.printNewline();

                    printIndentation(out);

                    marker = out.state.markers.add();

                    PrinterFactory.create(expr, out).print(expr, out);
                }
                else if (wrapLines)
                {
                    TestNodeWriter tester = out.testers.get();
                    PrinterFactory.create(expr, out).print(expr, tester);

                    if (
                        (preferWrapAfterAssign)
                        && ((tester.length + out.column + (padding ? 3
                                                                   : 1)) > lineLength))
                    {
                        if (padding)
                        {
                            out.print(SPACE_ASSIGN, JavaTokenTypes.ASSIGN);
                        }
                        else
                        {
                            out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                        }

                        if (!printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out))
                            out.printNewline();

                        printIndentation(out);
                    }
                    else if (indentStandard)
                    {
                        if (
                            out.column > lineLength /* ||(out.column + tester.length + (padding ? 3 : 1) > lineLength)*/    )
                        {
                            if (padding)
                            {
                                out.print(SPACE_ASSIGN, JavaTokenTypes.ASSIGN);
                            }
                            else
                            {
                                out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                            }

                            //out.state.markers.add();
                            if (!printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out))
                                out.printNewline();

                            printIndentation(out);
                        }
                        else
                        {
                            if (padding)
                            {
                                /*marker = out.state.markers.add(out.line,
                                                               out.column + 2);*/
                                out.print(ASSIGN_PADDED, JavaTokenTypes.ASSIGN);
                            }
                            else
                            {
                                out.print(ASSIGN, JavaTokenTypes.ASSIGN);

                                //out.state.markers.add();
                            }

                            printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

                            if (out.newline)
                                printIndentation(out);

                        }
                    }
                    else if (padding)
                    {
                        marker = out.state.markers.add(out.line, out.column + 2);
                        out.print(ASSIGN_PADDED, JavaTokenTypes.ASSIGN);

                        printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

                        if (out.newline)
                            printIndentation(out);
                    }
                    else
                    {
                        out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                        out.state.markers.add();

                        printCommentsAfter(node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

                        if (out.newline)
                            printIndentation(out);
                    }

                    PrinterFactory.create(expr, out).print(expr, out);

                    out.testers.release(tester);

                    if (marker != null)
                    {
                        out.state.markers.remove(marker);
                    }
                }
                else
                {
                    if (padding)
                    {
                        out.print(ASSIGN_PADDED, JavaTokenTypes.ASSIGN);
                    }
                    else
                    {
                        out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                    }

                    PrinterFactory.create(expr, out).print(expr, out);
                }

                if (indent)
                {
                    out.unindent();
                }
            }
            else // never perform wrapping
            {
                if (padding)
                {
                    out.print(ASSIGN_PADDED, JavaTokenTypes.ASSIGN);
                }
                else
                {
                    out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                }

                PrinterFactory.create(expr, out).print(expr, out);
            }
        }
        else // assignment expression
        {
            alignVarAssigns = AbstractPrinter.settings.getBoolean(
                            ConventionKeys.ALIGN_VAR_ASSIGNS,
                            ConventionDefaults.ALIGN_VAR_ASSIGNS);
            AST rhs = printLeftHandSide(node, out);

            if (out.mode == NodeWriter.MODE_DEFAULT)
            {
                TestNodeWriter tester = out.testers.get();
                tester.reset(out,false);
                PrinterFactory.create(rhs, out).print(rhs, tester);

                boolean indent =
                    (indentStandard || wrapAfterAssign || preferWrapAfterAssign
                    || preferWrapAfterLeftParen);

                if (indent)
                {
                    out.indent();
                }

                Marker marker = null;

                if (
                    preferWrapAfterAssign && wrapLines
                    && (out.getIndentLength() < out.column)
                    && tester.line>1
//                    && ((out.column + (padding ? 3
//                                               : 1) + tester.length) > lineLength))
                    )
                {
                    if (padding)
                    {
                        out.print(SPACE_ASSIGN, JavaTokenTypes.ASSIGN);
                    }
                    else
                    {
                        out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                    }

                    out.printNewline();
                    printIndentation(out);
                    marker = out.state.markers.add();
                }
                else
                {
                    if (alignVarAssigns)
                    {
                        JavaNode parent = ((JavaNode) node).getParent();

                        if (isNewChunk(parent, JavaTokenTypes.ASSIGN) || out.state.anonymousInnerClass)
                        {
                            out.state.assignOffset = OFFSET_NONE;
                        }

                        if (canAlign((JavaNode) node))
                        {
                            alignAssignment(node, false, out);
                        }
                    }

                    if (padding)
                    {
                        marker = out.state.markers.add(out.line, out.column + 2);
                        out.print(ASSIGN_PADDED, JavaTokenTypes.ASSIGN);
                    }
                    else
                    {
                        out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                        marker = out.state.markers.add();
                    }

                    out.testers.release(tester);
                }

                printRightHandSide(rhs, out);

                if (indent)
                {
                    out.unindent();
                }

                if (marker != null)
                {
                    out.state.markers.remove(marker);
                }
            }
            else
            {
                if (padding)
                {
                    out.print(ASSIGN_PADDED, JavaTokenTypes.ASSIGN);
                }
                else
                {
                    out.print(ASSIGN, JavaTokenTypes.ASSIGN);
                }

                printRightHandSide(rhs, out);
            }
        }
    }


    void align(
        int        amount,
        NodeWriter out)
      throws IOException
    {
        out.print(out.getString(amount), JavaTokenTypes.WS);
    }


    /**
     * Checks whether the given node denotes an assignment.
     *
     * @param node EXPR node to check.
     *
     * @return <code>true</code> if the given node denotes an assignment.
     */
    private boolean isAssignment(AST node)
    {
        AST child = node.getFirstChild();

        switch (child.getType())
        {
            case JavaTokenTypes.ASSIGN :

                switch (child.getFirstChild().getType())
                {
                    case JavaTokenTypes.QUESTION :
                        return false;

                    default :
                        return true;
                }

            default :
                return false;
        }
    }


    /**
     * Determines whether the given node marks the start of a new chunk.
     *
     * @param node a VARIABLE_DEF or EXPR node.
     * @param type the node type for which the chunk state should be determined, either
     *        VARIABLE_DEF or ASSIGN.
     *
     * @return <code>true</code> if the node marks a new chunk.
     */
    private boolean isNewChunk(
        AST node,
        int type)
    {
        JavaNode n = (JavaNode) node;

        // special handling of 'for' statements
        switch (n.getParent().getType())
        {
            case JavaTokenTypes.FOR_INIT :
                return true;
        }

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.CHUNKS_BY_COMMENTS, ConventionDefaults.CHUNKS_BY_COMMENTS))
        {
            if (n.hasCommentsBefore())
            {
                return true;
            }
        }

        int maxLinesBetween =
            AbstractPrinter.settings.getInt(
                ConventionKeys.BLANK_LINES_KEEP_UP_TO,
                ConventionDefaults.BLANK_LINES_KEEP_UP_TO);

        // it does not make sense to mark chunks by blank lines if no blank
        // lines will be kept
        if (maxLinesBetween > 0)
        {
            if (
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.CHUNKS_BY_BLANK_LINES,
                    ConventionDefaults.CHUNKS_BY_BLANK_LINES))
            {
                JavaNode prev = n.getPreviousSibling();

                switch (type)
                {
                    // align assignment part of VARIABLE_DEF
                    case JavaTokenTypes.VARIABLE_DEF :

                        switch (prev.getType())
                        {
                            case JavaTokenTypes.VARIABLE_DEF :

                                if (maxLinesBetween > 0)
                                {
                                    // Count hidden blank lines by adding up the newlines between the 2 nodes
                                    
                                    int totalLines = countChildrenLines((JavaNode)n.getPreviousSibling().getFirstChild(),0);
                                    if (totalLines-1 > maxLinesBetween) {
                                        return true;
                                    }
                                }

                                return false;
                        }

                        break;

                    case JavaTokenTypes.ASSIGN :

                        switch (prev.getType())
                        {
                            case JavaTokenTypes.EXPR :

                                if (isAssignment(prev))
                                {
                                    if (maxLinesBetween > 0)
                                    {
                                        // Count hidden blank lines by adding up the newlines between the 2 nodes
                                        
                                        int totalLines = countChildrenLines((JavaNode)n.getPreviousSibling().getFirstChild(),0);
                                        if (totalLines-1 > maxLinesBetween) {
                                            return true;
                                        }
                                    }
                                }

                                return false;

                            case JavaTokenTypes.VARIABLE_DEF :

                                if (maxLinesBetween > 0)
                                {
                                    if (
                                        (n.getStartLine()
                                        - n.getPreviousSibling().getStartLine() - 1) > maxLinesBetween)
                                    {
                                        return true;
                                    }
                                }

                                return false;
                        }

                        break;
                }
            }
        }

        return false;
    }


    /**
     * Determines the next sibling of the given ASSIGN node.
     *
     * @param node the ASSIGN node.
     * @param parent the parent node of the ASSIGN node.
     *
     * @return the next sibling, returns <code>null</code> if no sibling could be found.
     *
     * @throws IllegalArgumentException if an unexpected <em>parent</em> type was given.
     */
    private AST getNextSibling(
        AST      node,
        JavaNode parent)
    {
        /**
         * @todo are really all cases handled?
         */
        switch (parent.getType())
        {
            case JavaTokenTypes.EXPR :
            case JavaTokenTypes.VARIABLE_DEF :
                return parent.getNextSibling();

            case JavaTokenTypes.ASSIGN :
            case JavaTokenTypes.QUESTION :
            case JavaTokenTypes.EQUAL :
            case JavaTokenTypes.NOT_EQUAL :
            case JavaTokenTypes.LT :
            case JavaTokenTypes.GT :
                return getNextSibling(parent, parent.getParent());

            /*case JavaTokenTypes.LITERAL_if:
            case JavaTokenTypes.LITERAL_else:
            case JavaTokenTypes.LITERAL_for:
            case JavaTokenTypes.LITERAL_while:
            case JavaTokenTypes.LITERAL_do:
            case JavaTokenTypes.LITERAL_try:
            case JavaTokenTypes.LITERAL_catch:
            case JavaTokenTypes.LITERAL_finally:
            case JavaTokenTypes.LITERAL_synchronized:
            case JavaTokenTypes.LCURLY:
            case JavaTokenTypes.SLIST:
            case JavaTokenTypes.OBJBLOCK:
                return null;*/
            default :
                throw new IllegalArgumentException("unexpected parent node --" + parent);
        }
    }


    /**
     * Determines whether the given assignment is part of a variable declaration.
     *
     * @param node an ASSIGN node.
     *
     * @return <code>true</code> if the node is part of a declaration.
     *
     * @since 1.0b9
     */
    private boolean isPartOfDeclaration(AST node)
    {
        return ((JavaNode) node).getParent().getType() == JavaTokenTypes.VARIABLE_DEF;
    }


    /**
     * Outputs whitespace to align the assignment of the given node under prior
     * assignments or variable definitions.
     *
     * @param node the current ASSIGN node to print.
     * @param variableAssign <code>true</code> indicates that this assignment is part of
     *        a variable declaration.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void alignAssignment(
        AST        node,
        boolean    variableAssign,
        NodeWriter out)
      throws IOException
    {
        JavaNode parent = ((JavaNode) node).getParent();
        AST next = getNextSibling(node, parent);

        // offset already defined, succesive assignment
        if (out.state.assignOffset != OFFSET_NONE)
        {
            // align if necessary
            if (out.column < out.state.assignOffset)
            {
                align(out.state.assignOffset - out.column, out);
            }

            // delete the offset if we're not followed by a VARIABLE_DEF or
            // ASSIGN node
            if (next != null)
            {
                switch (next.getType())
                {
                    case JavaTokenTypes.VARIABLE_DEF :

                        if (isNewChunk(next, JavaTokenTypes.VARIABLE_DEF))
                        {
                            out.state.assignOffset = OFFSET_NONE;
                        }

                        break;

                    case JavaTokenTypes.EXPR :

                        if (
                            !isAssignment(next)
                            || isNewChunk(next, JavaTokenTypes.ASSIGN))
                        {
                            out.state.assignOffset = OFFSET_NONE;
                        }

                        break;

                    default :
                        out.state.assignOffset = OFFSET_NONE;

                        break;
                }
            }
        }
        else // no offset defined, this is the first VARIABLE_DEF/ASSIGN
        {
            if (next != null)
            {
                switch (next.getType())
                {
                    case JavaTokenTypes.EXPR :
                    {
                        if (variableAssign || !isAssignment(next))
                        {
                            out.state.assignOffset = OFFSET_NONE;

                            break;
                        }

                        //boolean lastAssign = isNewChunk(next, JavaTokenTypes.ASSIGN); // last chunk
                        int length = 0;
                        TestNodeWriter tester = out.testers.get();
SEARCH:
                        for (AST def = parent; def != null; def = def.getNextSibling())
                        {
                            switch (def.getType())
                            {
                                case JavaTokenTypes.EXPR :

                                    // if the next sibling is an assignment
                                    if (isAssignment(def))
                                    {
                                        tester.reset();

                                        AST rhs = def.getFirstChild().getFirstChild();
                                        PrinterFactory.create(rhs, out).print(rhs, tester);

                                        if (tester.length > length)
                                        {
                                            length = tester.length;
                                        }

                                        AST t = def.getNextSibling();

                                        if (t != null)
                                        {
                                            if (isNewChunk(t, JavaTokenTypes.ASSIGN))
                                            {
                                                break SEARCH;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        // no assignment found, quit
                                        break SEARCH;
                                    }

                                    break;
                            }
                        }

                        out.state.assignOffset = length + out.getIndentLength() + 1;

                        // align if necessary
                        if (out.column < out.state.assignOffset)
                        {
                            align(out.state.assignOffset - out.column, out);
                        }

                        /*if (lastAssign)
                        {
                            out.state.assignOffset = OFFSET_NONE;
                        }*/
                        out.testers.release(tester);

                        break;
                    }

                    case JavaTokenTypes.VARIABLE_DEF :
                    {
                        if (!variableAssign)
                        {
                            out.state.assignOffset = OFFSET_NONE;

                            break;
                        }

                        int length = OFFSET_NONE;

                        //boolean lastAssign = false;
                        TestNodeWriter tester = out.testers.get();
                        boolean alignVariables =
                            AbstractPrinter.settings.getBoolean(
                                ConventionKeys.ALIGN_VAR_IDENTS,
                                ConventionDefaults.ALIGN_VAR_IDENTS);
SEARCH:

                        // determine the longest VARIABLE_DEF or ASSIGN
                        for (AST def = parent; def != null; def = def.getNextSibling())
                        {
                            switch (def.getType())
                            {
                                case JavaTokenTypes.EXPR :

                                    // if the next sibling is an assignment
                                    if (isAssignment(def))
                                    {
                                        tester.reset();

                                        AST rhs = def.getFirstChild().getFirstChild();
                                        PrinterFactory.create(rhs, out).print(rhs, tester);

                                        if (tester.length > length)
                                        {
                                            length = tester.length;
                                        }

                                        if (isNewChunk(def, JavaTokenTypes.ASSIGN))
                                        {
                                            break SEARCH;
                                        }
                                    }
                                    else
                                    {
                                        // no assignment found, quit
                                        break SEARCH;
                                    }

                                    break;

                                case JavaTokenTypes.VARIABLE_DEF :
                                    tester.reset();

                                    AST defModifier = def.getFirstChild();
                                    PrinterFactory.create(defModifier, out).print(
                                        defModifier, tester);

                                    AST defType = defModifier.getNextSibling();
                                    PrinterFactory.create(defType, out).print(defType, tester);

                                    // we have to adjust the length in case
                                    // variable alignment is performed
                                    if (
                                        alignVariables
                                        && (out.state.variableOffset != VariableDeclarationPrinter.OFFSET_NONE))
                                    {
                                        if (out.state.variableOffset > tester.length)
                                        {
                                            tester.length =
                                                out.state.variableOffset
                                                - out.getIndentLength() - 1;
                                        }
                                    }

                                    AST defIdent = defType.getNextSibling();
                                    PrinterFactory.create(defIdent, out).print(
                                        defIdent, tester);
                                    tester.length++; // space before identifier

                                    if (tester.length > length)
                                    {
                                        length = tester.length;
                                    }

                                    AST n = def.getNextSibling();

                                    if (n != null)
                                    {
                                        if (isNewChunk(n, JavaTokenTypes.VARIABLE_DEF))
                                        {
                                            break SEARCH;
                                        }
                                    }

                                    break;

                                default :
                                    break SEARCH;
                            }
                        }

                        out.testers.release(tester);

                        out.state.assignOffset = length + out.getIndentLength() + 1;

                        // align if necessary
                        if (out.column < out.state.assignOffset)
                        {
                            align(out.state.assignOffset - out.column, out);
                        }

                        //if (lastAssign)
                        //out.state.assignOffset = OFFSET_NONE;
                        break;
                    }

                    default :
                        out.state.assignOffset = OFFSET_NONE;

                        break;
                }
            }
        }
    }


    /**
     * Determines whether the given assignment expression can be aligned.
     *
     * @param node ASSIGN node.
     *
     * @return <code>true</code> if the node can be aligned. ASSIGN nodes can't be
     *         aligned if they are part of a block statement expression list.
     *
     * @since 1.0b9
     */
    private boolean canAlign(JavaNode node)
    {
        JavaNode parent = node.getParent();

        /**
         * @todo parent should never be null!
         */
        if (parent != null)
        {
            switch (parent.getType())
            {
                case JavaTokenTypes.LITERAL_if :
                case JavaTokenTypes.LITERAL_else :
                case JavaTokenTypes.LITERAL_do :
                case JavaTokenTypes.LITERAL_for :
                case JavaTokenTypes.LITERAL_while :
                case JavaTokenTypes.LITERAL_switch :
                    return false;

                case JavaTokenTypes.SLIST :
                case JavaTokenTypes.OBJBLOCK :
                    return true;

                default :
                    return canAlign(parent);
            }
        }
        return false;
    }
}
