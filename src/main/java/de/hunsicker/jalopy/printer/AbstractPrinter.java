/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Level;

import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;
import de.hunsicker.jalopy.language.JavaNodeHelper;
import de.hunsicker.jalopy.language.antlr.ExtendedToken;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.language.antlr.Node;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.util.StringHelper;


/**
 * Skeleton implementation of the printer interface. Provides comment printing and blank
 * lines support.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.17 $
 */
abstract class AbstractPrinter
    implements Printer
{
    //~ Static variables/initializers ----------------------------------------------------

//    static final Perl5Util REGEX_ENGINE = new Perl5Util();

    static final String[] EMPTY_STRING_ARRAY = new String[0];

    static final String AT = "@";
    static final String AT_INTERFACE_SPACE="@interface " /* NOI18N */;
    static final String ASSERT_SPACE = "assert " /* NOI18N */;
    static final String ASSIGN = "=" /* NOI18N */;
    static final String ASSIGN_PADDED = " = " /* NOI18N */;
    static final String SPACE_ASSIGN = " =" /* NOI18N */;
    static final String BRACES = "{}" /* NOI18N */;
    static final String BRACKETS = "[]" /* NOI18N */;
    static final String BRACKET_LEFT = "[" /* NOI18N */;
    static final String BRACKET_LEFT_SPACE = "[ " /* NOI18N */;
    static final String BRACKET_RIGHT = "]" /* NOI18N */;
    static final String CASE_SPACE = "case " /* NOI18N */;
    static final String CATCH = "catch" /* NOI18N */;
    static final String CLASS_SPACE = "class " /* NOI18N */;
    static final String COMMA = "," /* NOI18N */;
    static final String COMMA_SPACE = ", " /* NOI18N */;
    static final String COLON = ":" /* NOI18N */;
    static final String COLON_SPACE = ": " /* NOI18N */;
    static final String DEFAULT_COLON = "default:" /* NOI18N */;
    static final String DEFAULT_SPACE_COLON = "default :" /* NOI18N */;
    static final String DO = "do" /* NOI18N */;
    static final String DOT = "." /* NOI18N */;
    static final String ELSE = "else" /* NOI18N */;
    static final String ENUM_SPACE = "enum " /* NOI18N */;
    static final String EMPTY_STRING = "" /* NOI18N */.intern();
    static final String EXTENDS_SPACE = "extends " /* NOI18N */;
    static final String FINALLY = "finally" /* NOI18N */;
    static final String FOR = "for" /* NOI18N */;
    static final String FOR_SPACE = "for " /* NOI18N */;
    static final String IF = "if" /* NOI18N */;
    static final String IF_SPACE = "if " /* NOI18N */;
    static final String IMPLEMENTS_SPACE = "implements " /* NOI18N */;
    static final String IMPORT_SPACE = "import " /* NOI18N */;
    static final String INTERFACE_SPACE = "interface " /* NOI18N */;
    static final String L = "L" /* NOI18N */;
    static final String LCURLY = "{" /* NOI18N */;
    static final String LCURLY_SPACE = "{ " /* NOI18N */;
    static final String LPAREN = "(" /* NOI18N */;
    static final String LPAREN_SPACE = "( " /* NOI18N */;
    static final String NEW_SPACE = "new " /* NOI18N */;
    static final String PACKAGE_SPACE = "package " /* NOI18N */;
    static final String PARENTHESES = "()" /* NOI18N */;
    static final String QUESTION_SPACE = "? " /* NOI18N */;
    static final String RCURLY = "}" /* NOI18N */;
    static final String SPACE_RCURLY = " }" /* NOI18N */;
    static final String RETURN = "return" /* NOI18N */;
    static final String RPAREN = ")" /* NOI18N */;
    static final String SEMI = ";" /* NOI18N */;
    static final String SPACE = " " /* NOI18N */;
    static final String SPACE_BRACKET_RIGHT = " ]" /* NOI18N */;
    static final String SPACE_BRACKETS = " []" /* NOI18N */;
    static final String SPACE_COLON_SPACE = " : " /* NOI18N */;
    static final String SPACE_DEFAULT_SPACE = " default " /* NOI18N */;
    static final String SPACE_EXTENDS_SPACE = " extends " /* NOI18N */;
    static final String SPACE_LCURLY = " {" /* NOI18N */;
    static final String SPACE_IMPLEMENTS_SPACE = " implements " /* NOI18N */;
    static final String SPACE_QUESTION = " ?" /* NOI18N */;
    static final String SPACE_QUESTION_SPACE = " ? " /* NOI18N */;
    static final String SPACE_RPAREN = " )" /* NOI18N */;
    static final String SPACE_THROWS_SPACE = " throws " /* NOI18N */;
    static final String STATIC = "static" /* NOI18N */;
    static final String STATIC_SPACE = "static " /* NOI18N */;
    static final String STRING = "String" /* NOI18N */;
    static final String SUPER = "super" /* NOI18N */;
    static final String SWITCH = "switch" /* NOI18N */;
    static final String SUPER_SPACE = "super " /* NOI18N */;
    static final String SYNCHRONIZED = "synchronized" /* NOI18N */;
    static final String THIS = "this" /* NOI18N */;
    static final String THROW_SPACE = "throw " /* NOI18N */;
    static final String THROWS_SPACE = "throws " /* NOI18N */;
    static final String TRY = "try" /* NOI18N */;
    static final String VOID = "void" /* NOI18N */;
    static final String WHILE = "while" /* NOI18N */;
    static final String WHILE_SPACE = "while " /* NOI18N */;
    static final String QUESTION = "?" /* NOI18N */;

    /** The code convention settings. */
    protected static final Convention settings = Convention.getInstance();

    /** Indicates that <strong>no</strong> whitespace should be printed. */
    private static final boolean WHITESPACE_NO = false;

    /** Indicates that whitespace should be printed. */
    private static final boolean WHITESPACE_YES = true;

    //~ Instance variables ---------------------------------------------------------------

    /** Helper object that holds arguments for message formatting. */
    private final Object[] _args = new Object[2];

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new AbstractPrinter object.
     */
    protected AbstractPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Prints all the children of the passed node
     */
    public final void printChildren(AST node, NodeWriter out) throws IOException  {
        for(AST child = node.getFirstChild();child!=null;child=child.getNextSibling()) {
            PrinterFactory.create(child, out).print(child,out);
        }
    }
    /**
     * {@inheritDoc}
     */
    public abstract void print(
        AST        node,
        NodeWriter out)
      throws IOException;


    /**
     * Tracks the position of the given node by updating the position information
     *
     * @param node the node that needs its position tracked.
     * @param out stream to write to.
     *
     * @since 1.0b9
     */
    public void trackPosition(
        JavaNode   node,
        NodeWriter out)
    {
        trackPosition(node, out.line, out.column, out);
    }


    /**
     * Tracks the position of the given node by updating the position information.
     *
     * @param node the node that needs its position tracked.
     * @param line the new line position.
     * @param column the new column position.
     * @param out stream to write to.
     *
     * @since 1.0b9
     */
    public void trackPosition(
        JavaNode   node,
        int        line,
        int        column,
        NodeWriter out)
    {
        if (out.tracking && (out.mode == NodeWriter.MODE_DEFAULT))
        {
            node.updatePosition(line, column);
        }
    }


    /**
     * Outputs indentation whitespace according to the current marker offset or indentation
     * level (depends on the used indentation policy).
     *
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    protected void printIndentation(NodeWriter out)
      throws IOException
    {
        printIndentation(0, out);
    }


    /**
     * Outputs indentation whitespace.
     *
     * @param marker if not <code>null</code> this marker will be used to calculate the
     *        right amount of indentation whitespace. Otherwise indentation depends on
     *        the general indentation policy.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b9
     */
    protected void printIndentation(
        Marker     marker,
        NodeWriter out)
      throws IOException
    {
        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            if (marker != null) // custom indentation
            {
                int indentLength = out.getIndentLength();
                int length =
                    (marker.column >= indentLength) ? (marker.column - indentLength)
                                                    : marker.column;
                out.print(out.getString(length), JavaTokenTypes.WS);
            }
            else if (
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP)
                && out.state.markers.isMarked()) // deep indentation
            {
                int indentLength = out.getIndentLength();
                marker = out.state.markers.getLast();

                int length =
                    (marker.column > indentLength) ? (marker.column - indentLength)
                                                   : marker.column;

                out.print(out.getString(length), JavaTokenTypes.WS);
            }
            else // standard indentation
            {
                out.print(
                    out.getString((out.state.paramLevel * out.indentSize)),
                    JavaTokenTypes.WS);
            }
        }
    }


    /**
     * Outputs indentation whitespace according to the current marker offset. If no
     * marker is set, this method does nothing.
     *
     * @param diff The amount
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    protected void printIndentation(
        int        diff,
        NodeWriter out)
      throws IOException
    {
        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            if (
                !AbstractPrinter.settings.getBoolean(
                    ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP))
            {
                /**
                 * @todo implement custom indentation
                 */
                out.print(
                    out.getString((out.state.paramLevel * out.indentSize) + diff),
                    JavaTokenTypes.WS);
            }
            else if (out.state.markers.isMarked())
            {
                int indentLength = out.getIndentLength();
                Marker marker = out.state.markers.getLast();

                /*int offset = diff;

                if (out.state.paramLevel > 0)
                {
                    //offset += ((out.state.paramLevel - 1) * out.indentSize);
                    offset += out.indentSize;
                }*/

                //offset += out.state.paramLevel * out.indentSize;
                
                int length =
                    (marker.column > indentLength) ? (marker.column - indentLength)
                                                   : marker.column;

                if ((length + diff) >= 0)
                {
                    out.print(out.getString(length + diff), JavaTokenTypes.WS);
                }
                else if (length>0)
                {
                    out.print(out.getString(length), JavaTokenTypes.WS);
                }
                else {
                    System.out.println("Warning marker bad " + marker.column +"," + indentLength);
                }
            }
        }
    }


    /**
     * True if node is the child of parent
     *
     * @param node The parent
     * @param child The child
     *
     * @return True 
     *
     * @since 1.0b9
     */
    final boolean isChildOf(
        JavaNode node,
        JavaNode child)
    {
        JavaNode parent = child.getParent();

        if (parent != node)
        {
            switch (parent.getType())
            {
                case JavaTokenTypes.OBJBLOCK :
                case JavaTokenTypes.SLIST :
                case JavaTokenTypes.LCURLY :
                case JavaTokenTypes.LITERAL_if :
                case JavaTokenTypes.LITERAL_for :
                case JavaTokenTypes.LITERAL_while :
                case JavaTokenTypes.LITERAL_do :
                case JavaTokenTypes.SYNBLOCK :
                case JavaTokenTypes.LITERAL_try :
                case JavaTokenTypes.LITERAL_catch :
                case JavaTokenTypes.LITERAL_finally :
                case JavaTokenTypes.LITERAL_static :
                case JavaTokenTypes.PARAMETERS :
                case JavaTokenTypes.MODIFIERS :
                case JavaTokenTypes.TYPE :
                    break;

                default :
                    return isChildOf(node, parent);
            }
        }
        else
        {
            return true;
        }

        return false;
    }


    /**
     * Determines the number of blank lines before the given node as found in the parsed
     * source file.
     *
     * @param node a node.
     *
     * @return number of blank lines before the given node as found in the parsed source
     *         file.
     */
    int getOriginalBlankLines(JavaNode node)
    {
        int keepLinesUpTo =
            AbstractPrinter.settings.getInt(
                ConventionKeys.BLANK_LINES_KEEP_UP_TO,
                ConventionDefaults.BLANK_LINES_KEEP_UP_TO);

        if (keepLinesUpTo <= 0)
        {
            // forget about original blank lines
            return 0;
        }

        /**
         * @todo it is currently not possible to determine the original blank lines for
         *       the class declarations because of the sorting transformation; we need
         *       to implement the preserving logic via stream splitting and deal with
         *       hidden tokens
         */
        switch (node.getPreviousSibling().getType())
        {
            case JavaTokenTypes.METHOD_DEF :
            case JavaTokenTypes.CTOR_DEF :
            case JavaTokenTypes.CLASS_DEF :
            case JavaTokenTypes.INTERFACE_DEF :
            case JavaTokenTypes.STATIC_INIT :
            case JavaTokenTypes.INSTANCE_INIT :
            case JavaTokenTypes.IMPORT :
            case JavaTokenTypes.PACKAGE_DEF :
            case JavaTokenTypes.ROOT :
                return 0;

            case JavaTokenTypes.VARIABLE_DEF :

                if (!isLocalVariable(node))
                {
                    return 0;
                }
        }

        switch (node.getType())
        {
            case JavaTokenTypes.EXPR :

                for (
                    JavaNode child = (JavaNode) node.getFirstChild(); child != null;
                    child = (JavaNode) child.getFirstChild())
                {
                    if (child.hasCommentsBefore())
                    {
                        CommonHiddenStreamToken t = child.getCommentBefore();

                        if (t != null)
                        {
                            int l =
                                t.getLine() - node.getPreviousSibling().getEndLine() - 1;

                            if (l > keepLinesUpTo)
                            {
                                return keepLinesUpTo;
                            }

                            return Math.max(l, 0);
                        }
                    }
                }

                break;

            case JavaTokenTypes.VARIABLE_DEF :

                if (!isLocalVariable(node))
                {
                    return 0;
                }

                break;

            case JavaTokenTypes.RCURLY :

                int blankLinesBeforeRcurly =
                    AbstractPrinter.settings.getInt(
                        ConventionKeys.BLANK_LINES_BEFORE_BRACE_RIGHT,
                        ConventionDefaults.BLANK_LINES_BEFORE_BRACE_RIGHT);

                if (blankLinesBeforeRcurly != -1)
                {
                    return blankLinesBeforeRcurly;
                }
        }

        JavaNode prev = node.getPreviousSibling();
        int result = 0;

        /**
         * @todo currently import nodes have no prevSibling after transformation
         */
        if (prev != null)
        {
            if (!node.hasCommentsBefore())
            {
                if (node.getParent().getType() == JavaTokenTypes.EXPR)
                {
                    /*result = node.getStartLine() -
                            node.getParent().getPreviousSibling()
                                .getEndLine() - 1;*/
                }
                else
                {
                    result = node.getStartLine() - prev.getEndLine() - 1;
                }
            }
            else
            {
                if (node.getParent().getType() == JavaTokenTypes.EXPR)
                {
                    /*result = node.getHiddenBefore().getLine() -
                             node.getParent().getPreviousSibling()
                                 .getEndLine() - 1;*/
                }
                else
                {
                    result = node.getCommentBefore().getLine() - prev.getEndLine() - 1;
                    
                }
            }
        }

        if (result > keepLinesUpTo)
        {
            return keepLinesUpTo;
        }

        return result;
    }


    /**
     * Outputs all found issues for the given node.
     *
     * @param node a node.
     * @param out stream to write to.
     */
    void logIssues(
        AST        node,
        NodeWriter out)
    {
        if (out.mode == NodeWriter.MODE_DEFAULT)
        {
            if (out.issues.containsKey(node))
            {
                Object value = out.issues.get(node);
                _args[0] = out.filename;
                _args[1] = String.valueOf(out.line);

                StringBuffer buf = new StringBuffer(250);

                if (value instanceof List)
                {
                    List issues = (List) value;

                    for (int i = 0, size = issues.size(); i < size; i++)
                    {
                        //JavaNode n = (JavaNode) node;
                        buf.append(out.filename);
                        buf.append(':');
                        buf.append(out.line);
                        buf.append(':');
                        buf.append((String) issues.get(i));

                        Loggers.IO.warn(buf.toString());
                        buf.setLength(0);
                    }
                }
                else
                {
                    buf.append(out.filename);
                    buf.append(':');
                    buf.append(out.line);
                    buf.append(':');
                    buf.append((String) value);

                    Loggers.IO.warn(buf.toString());
                }
            }
        }
    }


    /**
     * Outputs a number of blank lines before the given node.
     *
     * @param node node to print blank lines for.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    void printBlankLinesBefore(
        JavaNode   node,
        NodeWriter out)
      throws IOException
    {
        int blankLines = getBlankLinesBefore(node, out);
        out.printBlankLines(blankLines);

        switch (node.getType())
        {
            case JavaTokenTypes.EXPR :
                // we need to store this information in order to be able to
                // print the right amout of blank lines before the node. This is
                // because we print out blank lines for the EXPR node
                // and afterwards may have to print blanks lines for a child
                // (because of an associated comment)
                out.blankLines = blankLines;
                out.expression = node;

                break;
        }
    }


    /**
     * Prints all comments after the given node. The handling of newlines before and
     * after depends on the type of the given node.
     *
     * @param node node that have its comments printed.
     * @param out stream to write to.
     *
     * @return <code>true</code> if comments were printed.
     *
     * @throws IOException if an I/O error occured.
     *
     * @see #printCommentsAfter(AST, boolean, boolean, NodeWriter)
     */
    boolean printCommentsAfter(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        if (out.mode != NodeWriter.MODE_DEFAULT)
        {
            return false;
        }

        JavaNode n = (JavaNode) node;

        if (n.getHiddenAfter() == null)
        {
            return false;
        }

        boolean result = false;

        switch (node.getType())
        {
            // these nodes can have endline comments (before their block),
            // therefore the comment printing depends on the current brace mode
            case JavaTokenTypes.LITERAL_if :
            case JavaTokenTypes.LITERAL_else :
            case JavaTokenTypes.LITERAL_for :
            case JavaTokenTypes.LITERAL_while :
            case JavaTokenTypes.LITERAL_try :
            case JavaTokenTypes.LITERAL_catch :
            case JavaTokenTypes.LITERAL_finally :
            case JavaTokenTypes.LITERAL_switch :
            case JavaTokenTypes.LITERAL_do :
            case JavaTokenTypes.LITERAL_synchronized :
            case JavaTokenTypes.METHOD_DEF :
            case JavaTokenTypes.CLASS_DEF :
            case JavaTokenTypes.INTERFACE_DEF :
            case JavaTokenTypes.CTOR_DEF :
            case JavaTokenTypes.INSTANCE_INIT :
            case JavaTokenTypes.STATIC_INIT :

                /**
                 * @todo does this stuff still work in 1.0b9?
                 */
                if (
                    AbstractPrinter.settings.getBoolean(
                        ConventionKeys.BRACE_NEWLINE_LEFT,
                        ConventionDefaults.BRACE_NEWLINE_LEFT))
                {
                    // print directly after, newlines are
                    // handled by BlockPrinter.java
                    result =
                        printCommentsAfter(
                            node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);
                }
                else
                {
                    /*List comments = ((JavaNode)node).getCommentsAfter();
                    JavaNode child = (JavaNode)JavaNodeHelper.getFirstChild(node,
                                                                        JavaTokenTypes.SLIST);

                    if (child != null)
                    {
                        if (child.hasCommentsAfter())
                        {
                            result = printCommentsAfter(node, out,
                                                        NodeWriter.NEWLINE_NO,
                                                        NodeWriter.NEWLINE_YES);
                        }
                        else
                        {
                            // we can't print the comment directly because than
                            // we would comment out the following open curly
                            // brace. Therefore we just store the comment;
                            // BlockPrinter.java will output it
                            AST comment = (AST)comments.remove(0);
                            out.setPendingComment(comment.getText());
                            result = false;
                        }
                    }*/
                }

                break;

            default :
                result =
                    printCommentsAfter(
                        node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);

                break;
        }

        return result;
    }


    /**
     * Prints all comments after the given node.
     *
     * @param node node that have its comments printed.
     * @param newlineBefore if <code>true</code>, a line break will be added before each
     *        comment.
     * @param newlineAfter if <code>true</code>, a line break will be added after each
     *        comment.
     * @param out stream to write to.
     *
     * @return <code>true</code> if comments were printed.
     *
     * @throws IOException if an I/O error occured.
     */
    boolean printCommentsAfter(
        AST        node,
        boolean    newlineBefore,
        boolean    newlineAfter,
        NodeWriter out)
      throws IOException
    {
        JavaNode n = (JavaNode) node;

        if (n.getHiddenAfter() == null || !n.hasCommentsAfter())
        {
            return false;
        }

        // if this is not the last right curly brace
        if ((node.getType() != JavaTokenTypes.RCURLY) || (out.getIndentLevel() != 0))
        {
            // store the position where the first comment starts
            int offset =
                out.column - 1
                + AbstractPrinter.settings.getInt(
                    ConventionKeys.INDENT_SIZE_COMMENT_ENDLINE,
                    ConventionDefaults.INDENT_SIZE_COMMENT_ENDLINE);

            CommonHiddenStreamToken firstComment =
                n.getHiddenAfter();
            // if we have more than one comment
            if (firstComment.getHiddenAfter() != null)
            {
                // print the first comment directly after the node
                printCommentAfter(
                    n, firstComment, newlineBefore, NodeWriter.NEWLINE_YES, WHITESPACE_YES,
                    true, out);

                if (out.mode != NodeWriter.MODE_DEFAULT)
                {
                    return false;
                }

                int indentLength = out.getIndentLength();

                // and align all other under the first
                for (
                    CommonHiddenStreamToken comment = firstComment.getHiddenAfter();
                    comment != null; comment = comment.getHiddenAfter())
                {
                    if (offset < indentLength)
                    {
                        int indentLevel = out.getIndentLevel();
                        out.setIndentLevel(0);
                        out.print(out.getString(offset), JavaTokenTypes.WS);
                        out.setIndentLevel(indentLevel);
                    }
                    else
                    {
                        out.print(
                            out.getString(offset - indentLength), JavaTokenTypes.WS);
                    }

                    printCommentAfter(
                        n, comment, newlineBefore, newlineAfter, WHITESPACE_NO, false, out);
                }
            }
            else
            {
                printCommentAfter(
                    n, firstComment, newlineBefore, newlineAfter, WHITESPACE_YES, true,
                    out);
            }
        }
        else
        {
            CommonHiddenStreamToken firstComment = n.getHiddenAfter();

            // is the first comment an endline comment?
            boolean endlineComment = firstComment.getLine() == n.getEndLine();

            if (!endlineComment)
            {
                out.printNewline();
                out.printNewline();
            }

            printCommentAfter(
                node, firstComment, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES,
                endlineComment ? WHITESPACE_YES
                               : WHITESPACE_NO, true, out);

            if (out.mode != NodeWriter.MODE_DEFAULT)
            {
                return false;
            }

            // the Java Language Specification requires every single-line
            // comment to be terminated by an end-of-line sequence, for the
            // sake of simplicity we print one for all comment types
            for (
                CommonHiddenStreamToken comment = firstComment.getHiddenAfter();
                comment != null; comment = comment.getHiddenAfter())
            {
                printCommentAfter(
                    n, comment, newlineBefore, NodeWriter.NEWLINE_YES, WHITESPACE_NO,
                    false, out);
            }
        }

        out.last = node.getType();

        return true;
    }


    /**
     * Prints all comments before the given node.
     *
     * @param node node that have its comments printed.
     * @param out stream to write to.
     *
     * @return <code>true</code> if comments were printed.
     *
     * @throws IOException if an I/O error occured.
     */
    boolean printCommentsBefore(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        return printCommentsBefore(node, true, out);
    }


    /**
     * Prints all comments before the given node.
     *
     * @param node node that have its comments printed.
     * @param newlineBefore if <code>true</code> a line break will be printed before the
     *        comment.
     * @param out stream to write to.
     *
     * @return <code>true</code> if comments were printed.
     *
     * @throws IOException if an I/O error occured.
     * @throws RuntimeException If an error occurs
     */
    boolean printCommentsBefore(
        AST        node,
        boolean    newlineBefore,
        NodeWriter out)
      throws IOException
    {
        if (out.mode == NodeWriter.MODE_TEST)
        {
            return false;
        }

        JavaNode n = (JavaNode) node;
        
        if (!newlineBefore) {
            switch (n.getParent().getType()) {
                case JavaTokenTypes.ENUM_CONSTANT_DEF:
                    newlineBefore = true;
                    break;
                default:
                    break;
            }
        }

        if (!n.hasCommentsBefore())
        {
            // no comments, only print blank lines
            printBlankLinesBefore(n, out);

            return false;
        }

        
        int linesToKeep =
            AbstractPrinter.settings.getInt(
                ConventionKeys.BLANK_LINES_KEEP_UP_TO,
                ConventionDefaults.BLANK_LINES_KEEP_UP_TO);
        boolean keepLines = linesToKeep > -1;


        CommonHiddenStreamToken previousComment = null;
        
        CommonHiddenStreamToken firstComment = n.getCommentBefore();
        

        for (
            CommonHiddenStreamToken comment = firstComment; comment != null;
            comment = comment.getHiddenAfter())
        {

            switch (comment.getType())
            {
	            case JavaTokenTypes.SEPARATOR_COMMENT :
                case JavaTokenTypes.JAVADOC_COMMENT :
	            	newlineBefore = newlineBefore | !out.newline;
	            	// fall through
                case JavaTokenTypes.SL_COMMENT :
                case JavaTokenTypes.ML_COMMENT :
                case JavaTokenTypes.SPECIAL_COMMENT :

                    if (n.getStartLine() != comment.getLine())
                    {
                        switch (out.last)
                        {
                            case JavaTokenTypes.SEPARATOR_COMMENT :
                                break;

                            default :

                                if (keepLines && (previousComment != null))
                                {
                                    // TODO Was commented out
                                    printBlankLinesBetweenComments(comment,
                                                                   previousComment,
                                                                   linesToKeep,
                                                                   out);
                                }
                                break;
                        }

                        printCommentBefore(
                            n, comment, comment == firstComment, (comment == firstComment ? newlineBefore:!out.newline),
                            NodeWriter.NEWLINE_YES, out);
                    }
                    else // print in same line
                    {
                        printCommentBefore(
                            n, comment, comment == firstComment, newlineBefore,
                            NodeWriter.NEWLINE_NO, out);
                        out.print(SPACE, out.last);

                        // change the column offset as we don't want line wrapping
                        // happen because of the comment
                        if (comment.getText()!=null)
                            out.column -= (comment.getText().length() - 1);
                    }

                    break;
                   
                case JavaTokenTypes.WS:
                    break;
                
                default :
                    throw new RuntimeException("Strange comment type ! " + comment);
            }

            previousComment = comment;
        }

        return true;
    }


    /**
     * Returns the number of blank lines that should be printed before the given node.
     *
     * @param node a node.
     * @param out stream to write to.
     *
     * @return the number of blank lines to be printed before the node.
     */
    private int getBlankLinesBefore(
        JavaNode   node,
        NodeWriter out)
    {
        int result = 0;

        if (canHaveBlankLinesBefore(node))
        {
            // first set the value as specified in the code convention
            switch (node.getType())
            {
                case JavaTokenTypes.LITERAL_throws:
                    result=1;
                    break;
                case JavaTokenTypes.ASSIGN :
                    break;

                case JavaTokenTypes.EXPR :

                    switch (node.getParent().getType())
                    {
                        case JavaTokenTypes.SLIST :
                        case JavaTokenTypes.CASESLIST :
                            break;

                        default :
                            return 0;
                    }

                    break;

                case JavaTokenTypes.VARIABLE_DEF :

                    switch (node.getParent().getType())
                    {
                        case JavaTokenTypes.FOR_INIT :
                            return 0;
                    }

                    switch (out.last)
                    {
                        case JavaTokenTypes.VARIABLE_DEF :
                            break;

                        case JavaTokenTypes.METHOD_DEF :
                        case JavaTokenTypes.CTOR_DEF :
                        case JavaTokenTypes.CLASS_DEF :
                        case JavaTokenTypes.INTERFACE_DEF :
                        case JavaTokenTypes.INSTANCE_INIT :
                        case JavaTokenTypes.STATIC_INIT :
                            result = 1;

                            break;

                        default :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_BEFORE_DECLARATION,
                                    ConventionDefaults.BLANK_LINES_BEFORE_DECLARATION);

                            break;
                    }

                    break;

                case JavaTokenTypes.RCURLY :

                    if (!node.hasCommentsBefore())
                    {
                        // TODO Add control over the number of lines to print before RBRACK on an ENUMBERATION
                        if (node.getParent().getParent().getType()==JavaTokenTypes.ENUM_DEF) {
                            if (!out.newline) {
                                return 1;
                            }
                        }
                        else switch (out.last)
                        {
                            case JavaTokenTypes.RCURLY :
                            case JavaTokenTypes.CLASS_DEF :
                            case JavaTokenTypes.INTERFACE_DEF :
                            case JavaTokenTypes.METHOD_DEF :
                            case JavaTokenTypes.CTOR_DEF :
                            case JavaTokenTypes.INSTANCE_INIT :
                            case JavaTokenTypes.STATIC_INIT :
                                return AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_BEFORE_BRACE_RIGHT,
                                    ConventionDefaults.BLANK_LINES_BEFORE_BRACE_RIGHT);

                            default :
                                result =
                                    AbstractPrinter.settings.getInt(
                                        ConventionKeys.BLANK_LINES_BEFORE_BRACE_RIGHT,
                                        ConventionDefaults.BLANK_LINES_BEFORE_BRACE_RIGHT);

                                break;
                        }
                    }

                    break;

                case JavaTokenTypes.LITERAL_if :

                    switch (out.last)
                    {
                        case JavaTokenTypes.LITERAL_else :
                        case JavaTokenTypes.LABELED_STAT :
                            break;

                        default :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_BEFORE_BLOCK,
                                    ConventionDefaults.BLANK_LINES_BEFORE_BLOCK);

                            break;
                    }

                    break;

                // blocks
                case JavaTokenTypes.LITERAL_while :

                    if (node.getParent().getType() == JavaTokenTypes.LITERAL_do)
                    {
                        // no blank lines before the while part of do-while statements
                        break;
                    }

                // fall-through
                case JavaTokenTypes.LITERAL_for :
                case JavaTokenTypes.LITERAL_try :
                case JavaTokenTypes.LITERAL_switch :
                case JavaTokenTypes.SYNBLOCK :
                case JavaTokenTypes.LITERAL_do :

                    switch (out.last)
                    {
                        case JavaTokenTypes.LABELED_STAT :
                            break;

                        default :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_BEFORE_BLOCK,
                                    ConventionDefaults.BLANK_LINES_BEFORE_BLOCK);

                            break;
                    }

                    break;

                case JavaTokenTypes.LITERAL_case :
                case JavaTokenTypes.LITERAL_default :
                    result =
                        AbstractPrinter.settings.getInt(
                            ConventionKeys.BLANK_LINES_BEFORE_CASE_BLOCK,
                            ConventionDefaults.BLANK_LINES_BEFORE_CASE_BLOCK);

                    break;

                case JavaTokenTypes.LITERAL_return :
                case JavaTokenTypes.LITERAL_break :
                case JavaTokenTypes.LITERAL_continue :
                    result =
                        AbstractPrinter.settings.getInt(
                            ConventionKeys.BLANK_LINES_BEFORE_CONTROL,
                            ConventionDefaults.BLANK_LINES_BEFORE_CONTROL);

                    break;

                case JavaTokenTypes.SLIST :

                    if (JavaNodeHelper.isFreestandingBlock(node))
                    {
                        result =
                            AbstractPrinter.settings.getInt(
                                ConventionKeys.BLANK_LINES_BEFORE_BLOCK,
                                ConventionDefaults.BLANK_LINES_BEFORE_BLOCK);
                    }

                    break;

                case JavaTokenTypes.LITERAL_else :
                case JavaTokenTypes.LITERAL_catch :
                case JavaTokenTypes.LITERAL_finally :

                    if (
                        !AbstractPrinter.settings.getBoolean(
                            ConventionKeys.BRACE_NEWLINE_RIGHT,
                            ConventionDefaults.BRACE_NEWLINE_RIGHT))
                    {
                        result--;
                    }

                    break;

                case JavaTokenTypes.SEMI :
                case JavaTokenTypes.IMPORT :
                case JavaTokenTypes.LABELED_STAT :
                case JavaTokenTypes.ARRAY_INIT :
                case JavaTokenTypes.CTOR_CALL :
                case JavaTokenTypes.SUPER_CTOR_CALL :
                case JavaTokenTypes.LITERAL_throw :
                case JavaTokenTypes.EMPTY_STAT :
                case JavaTokenTypes.PACKAGE_DEF :
                    break;

                case JavaTokenTypes.METHOD_DEF :
                {
                    JavaNode prev = node.getPreviousSibling();

                    switch (prev.getType())
                    {
                        case JavaTokenTypes.CTOR_DEF :
                        case JavaTokenTypes.CLASS_DEF :
                        case JavaTokenTypes.INTERFACE_DEF :
                        case JavaTokenTypes.VARIABLE_DEF :
                        case JavaTokenTypes.INSTANCE_INIT :
                        case JavaTokenTypes.STATIC_INIT :
                            result = 1;

                            break;

                        case JavaTokenTypes.METHOD_DEF :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_AFTER_METHOD,
                                    ConventionDefaults.BLANK_LINES_AFTER_METHOD);

                            break;

                        default :
                            break;
                    }

                    break;
                }

                case JavaTokenTypes.CTOR_DEF :
                {
                    JavaNode prev = node.getPreviousSibling();

                    switch (prev.getType())
                    {
                        case JavaTokenTypes.METHOD_DEF :
                        case JavaTokenTypes.CLASS_DEF :
                        case JavaTokenTypes.INTERFACE_DEF :
                        case JavaTokenTypes.INSTANCE_INIT :
                        case JavaTokenTypes.STATIC_INIT :
                        case JavaTokenTypes.VARIABLE_DEF :
                            result = 1;

                            break;

                        case JavaTokenTypes.CTOR_DEF :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_AFTER_METHOD,
                                    ConventionDefaults.BLANK_LINES_AFTER_METHOD);

                            break;

                        default :
                            break;
                    }

                    break;
                }

                case JavaTokenTypes.CLASS_DEF :
                {
                    JavaNode prev = node.getPreviousSibling();
///CLOVER:OFF
OUTER:
                    switch (prev.getType())
                    {
                        case JavaTokenTypes.METHOD_DEF :
                        case JavaTokenTypes.CTOR_DEF :
                        case JavaTokenTypes.INSTANCE_INIT :
                        case JavaTokenTypes.STATIC_INIT :
                        case JavaTokenTypes.VARIABLE_DEF :
                            result = 1;

                            break;

                        case JavaTokenTypes.INTERFACE_DEF :

                            if (out.indentLevel > 0)
                            {
                                result = 1;
                            }
                            else
                            {
                                result = 2;
                            }

                            break;

                        case JavaTokenTypes.CLASS_DEF :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_AFTER_CLASS,
                                    ConventionDefaults.BLANK_LINES_AFTER_CLASS);

                            /**
                             * @todo see NodeWriter.printRightBrace(int, boolean)
                             */
                            if (out.indentLevel == 0)
                            {
                                result++;
                            }

                            break;

                        case JavaTokenTypes.SEMI :

                            // we ignore additional semis in import statements, so if we
                            // find one here, we know we are behind the last import
                            // statement
                            switch (node.getParent().getType())
                            {
                                case JavaTokenTypes.ROOT :
                                case JavaTokenTypes.PACKAGE_DEF :
                                case JavaTokenTypes.IMPORT :
                                    break;

                                default :
                                    break OUTER;
                            }

                        // fall-through
                        case JavaTokenTypes.IMPORT :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_AFTER_IMPORT,
                                    ConventionDefaults.BLANK_LINES_AFTER_IMPORT);

                            break;

                        default :
                            break;
                    }

                    break;
                }
///CLOVER:ON

                case JavaTokenTypes.ENUM_DEF :
                case JavaTokenTypes.INTERFACE_DEF :
                {
                    JavaNode prev = node.getPreviousSibling();

                    switch (prev.getType())
                    {
                        case JavaTokenTypes.METHOD_DEF :
                        case JavaTokenTypes.CTOR_DEF :
                        case JavaTokenTypes.INSTANCE_INIT :
                        case JavaTokenTypes.STATIC_INIT :
                        case JavaTokenTypes.VARIABLE_DEF :
                            result = 1;

                            break;

                        case JavaTokenTypes.CLASS_DEF :

                            if (out.indentLevel > 0)
                            {
                                result = 1;
                            }
                            else
                            {
                                result = 2;
                            }

                            break;

                        case JavaTokenTypes.INTERFACE_DEF :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_AFTER_INTERFACE,
                                    ConventionDefaults.BLANK_LINES_AFTER_INTERFACE);

                            /**
                             * @todo see NodeWriter.printRightBrace(int, boolean)
                             */
                            if (out.indentLevel == 0)
                            {
                                result++;
                            }

                            break;

                        case JavaTokenTypes.IMPORT :
                            result =
                                AbstractPrinter.settings.getInt(
                                    ConventionKeys.BLANK_LINES_AFTER_IMPORT,
                                    ConventionDefaults.BLANK_LINES_AFTER_IMPORT);

                            break;

                        default :
                            break;
                    }

                    break;
                }

                case JavaTokenTypes.INSTANCE_INIT :
                    result = 1;

                    break;

                case JavaTokenTypes.STATIC_INIT :
                    result = 1;

                    break;

                default :

                    /**
                     * @todo do we need to determine whether this node is part of an
                     *       imaginary node and thus the blank line setting was already
                     *       determined?
                     */
                    return 0;
            }
        }
        else
        {
            return 0;
        }

        // override if necessary (according to the last node printed)
        switch (out.last)
        {
            case JavaTokenTypes.INSTANCE_INIT :
            case JavaTokenTypes.STATIC_INIT :
            case JavaTokenTypes.RCURLY :

                switch (node.getType())
                {
                    case JavaTokenTypes.RCURLY :
                    case JavaTokenTypes.LITERAL_else :
                    case JavaTokenTypes.LITERAL_catch :
                    case JavaTokenTypes.LITERAL_finally :
                    case JavaTokenTypes.LITERAL_case :
                    case JavaTokenTypes.LITERAL_default :
                        break;

                    case JavaTokenTypes.LITERAL_while :

                        switch (node.getParent().getType())
                        {
                            case JavaTokenTypes.LITERAL_do :
                                result = 0;

                                break;
                        }

                        break;

                    default :

                        int blankLinesAfterBlock =
                            AbstractPrinter.settings.getInt(
                                ConventionKeys.BLANK_LINES_AFTER_BLOCK,
                                ConventionDefaults.BLANK_LINES_AFTER_BLOCK);

                        if (blankLinesAfterBlock > result)
                        {
                            result = blankLinesAfterBlock;
                        }

                        /*
                           if (!AbstractPrinter.settings.getBoolean(ConventionKeys.BRACE_NEWLINE_RIGHT,
                                                      ConventionDefaults.BRACE_NEWLINE_RIGHT))
                           {
                               switch (node.getPreviousSibling().getType())
                               {
                                   case JavaTokenTypes.CLASS_DEF:
                                   case JavaTokenTypes.INTERFACE_DEF:
                                   break;
                                   default:
                                   result++;
                                   break;
                               }
                           }
                         */
                        break;
                }

                break;

            case JavaTokenTypes.LITERAL_case :
            case JavaTokenTypes.LITERAL_default :

                switch (node.getType())
                {
                    case JavaTokenTypes.LITERAL_case :
                    case JavaTokenTypes.LITERAL_default :
                    case JavaTokenTypes.LITERAL_return :
                    case JavaTokenTypes.LITERAL_break :
                        result = 0;

                        break;
                }

                break;

            case JavaTokenTypes.VARIABLE_DEF :

                switch (node.getType())
                {
                    case JavaTokenTypes.VARIABLE_DEF :
                        break;

                    case JavaTokenTypes.METHOD_DEF :
                    case JavaTokenTypes.CTOR_DEF :
                    case JavaTokenTypes.INSTANCE_INIT :
                    case JavaTokenTypes.STATIC_INIT :
                    case JavaTokenTypes.CLASS_DEF :
                    case JavaTokenTypes.INTERFACE_DEF :
                    case JavaTokenTypes.RCURLY :
                        break;

                    default :

                        int blankLinesAfterDeclaration =
                            AbstractPrinter.settings.getInt(
                                ConventionKeys.BLANK_LINES_AFTER_DECLARATION,
                                ConventionDefaults.BLANK_LINES_AFTER_DECLARATION);

                        if (blankLinesAfterDeclaration > result)
                        {
                            result = blankLinesAfterDeclaration;
                        }

                        break;
                }

                break;

            case JavaTokenTypes.LITERAL_while :
            case JavaTokenTypes.LITERAL_if :
            case JavaTokenTypes.LITERAL_for :
            case JavaTokenTypes.LITERAL_do :
            case JavaTokenTypes.LCURLY :

                int blankLinesAfterOpenCurly =
                    AbstractPrinter.settings.getInt(
                        ConventionKeys.BLANK_LINES_AFTER_BRACE_LEFT,
                        ConventionDefaults.BLANK_LINES_AFTER_BRACE_LEFT);

                if (blankLinesAfterOpenCurly > -1)
                {
                    // force number of blank lines after open curly braces
                    // (and block statements without braces printed)
                    result = blankLinesAfterOpenCurly;
                }

                break;

            case JavaTokenTypes.BOF :
                break;
        }

        // and keep the original blank lines
        switch (node.getType())
        {
            case JavaTokenTypes.CLASS_DEF :
            case JavaTokenTypes.INTERFACE_DEF :
            case JavaTokenTypes.METHOD_DEF :
            case JavaTokenTypes.CTOR_DEF :
            case JavaTokenTypes.INSTANCE_INIT :
            case JavaTokenTypes.STATIC_INIT :
            case JavaTokenTypes.PACKAGE_DEF :
                break;

            default :

                int original = getOriginalBlankLines(node);

                if (original > result)
                {
                    result = original;
                }

                break;
        }

        return result;
    }


    /**
     * Returns the number of blank lines that should be printed before a node because of
     * the given comment (the first comment of the given node).
     *
     * @param comment the first comment of a node.
     * @param previous The previous node
     * @param out stream to write to.
     *
     * @return the number of blank lines to be printed before the given comment.
     */
    private int getCommentBlankLines(
        CommonHiddenStreamToken comment,
        JavaNode                previous,
        NodeWriter              out)
    {
        switch (out.last)
        {
            case JavaTokenTypes.BOF :
                return 0;

            case JavaTokenTypes.LCURLY :

                int blankLinesAfterOpenCurly =
                    AbstractPrinter.settings.getInt(
                        ConventionKeys.BLANK_LINES_AFTER_BRACE_LEFT,
                        ConventionDefaults.BLANK_LINES_AFTER_BRACE_LEFT);

                if (blankLinesAfterOpenCurly > -1)
                {
                    // force number of blank lines after open curly braces
                    return blankLinesAfterOpenCurly;
                }

                break;
        }

        int result = 0;

        switch (comment.getType())
        {
            case JavaTokenTypes.SL_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.SL_COMMENT :
                        result = 0;

                        break;

                    default :
                        result =
                            AbstractPrinter.settings.getInt(
                                ConventionKeys.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE,
                                ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE);

                        break;
                }

                break;

            case JavaTokenTypes.SPECIAL_COMMENT :
                result =
                    AbstractPrinter.settings.getInt(
                        ConventionKeys.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE,
                        ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE);

                break;

            case JavaTokenTypes.ML_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.ML_COMMENT :
                        result = 0;

                        break;

                    default :
                        result =
                            AbstractPrinter.settings.getInt(
                                ConventionKeys.BLANK_LINES_BEFORE_COMMENT_MULTI_LINE,
                                ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_MULTI_LINE);

                        break;
                }

                break;

            case JavaTokenTypes.JAVADOC_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.JAVADOC_COMMENT :
                        result = 0;

                        break;

                    default :
                        result =
                            AbstractPrinter.settings.getInt(
                                ConventionKeys.BLANK_LINES_BEFORE_COMMENT_JAVADOC,
                                ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_JAVADOC);

                        break;
                }

                break;

            case JavaTokenTypes.SEPARATOR_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.RCURLY :
                        result = 2;

                        break;

                    default :
                        result = 1;

                        break;
                }

                break;
        }

        switch (out.last)
        {
            case JavaTokenTypes.PACKAGE_DEF :

                int linesAfterPackage =
                    AbstractPrinter.settings.getInt(
                        ConventionKeys.BLANK_LINES_AFTER_PACKAGE,
                        ConventionDefaults.BLANK_LINES_AFTER_PACKAGE);

                if (result > linesAfterPackage)
                {
                    result -= linesAfterPackage;
                }
                else
                {
                    result = 0;
                }

                break;
        }

        int keepLinesUpTo =
            AbstractPrinter.settings.getInt(
                ConventionKeys.BLANK_LINES_KEEP_UP_TO,
                ConventionDefaults.BLANK_LINES_KEEP_UP_TO);

        int l = comment.getLine() - previous.getEndLine() - 1;

        if (l > keepLinesUpTo)
        {
            return keepLinesUpTo;
        }

        return Math.max(result, l);
    }


    /**
     * Determines whether the given VARIABLE_DEF node denotes a local variable (those
     * immediately contained by a method, constructor or initializer block).
     *
     * @param node VARIABLE_DEF node.
     *
     * @return <code>true</code> if the given node denotes a local variable.
     */
    private boolean isLocalVariable(JavaNode node)
    {
        switch (node.getParent().getType())
        {
            case JavaTokenTypes.OBJBLOCK :
                return false;

            default :
                return true;
        }
    }


    /**
     * Determines whether the given node can have blank lines before.
     *
     * @param node node to check.
     *
     * @return <code>true</code> if the given node can have blank lines before.
     */
    private boolean canHaveBlankLinesBefore(JavaNode node)
    {
        /*
        switch (node.getType())
        {
            case JavaTokenTypes.EXPR:
            case JavaTokenTypes.LITERAL_if:
            case JavaTokenTypes.LITERAL_for:
            case JavaTokenTypes.LITERAL_while:
            case JavaTokenTypes.LITERAL_do:
            case JavaTokenTypes.LITERAL_try:
            case JavaTokenTypes.LITERAL_catch:
            case JavaTokenTypes.LITERAL_finally:
                return true;
        }
        */
        JavaNode parent = node.getParent();

        switch (parent.getType())
        {
            case JavaTokenTypes.SLIST :
            case JavaTokenTypes.OBJBLOCK :
            case JavaTokenTypes.LCURLY :
            case JavaTokenTypes.ROOT :
                return true;

            case JavaTokenTypes.MODIFIERS :
                return parent.getFirstChild() == node;

            case JavaTokenTypes.EXPR :
            case JavaTokenTypes.METHOD_CALL :
            case JavaTokenTypes.IMPORT :
            case JavaTokenTypes.CLASS_DEF :
            case JavaTokenTypes.INTERFACE_DEF :
            case JavaTokenTypes.PACKAGE_DEF :
                return false;

            default :
                return canHaveBlankLinesBefore(parent);
        }
    }


    /**
     * Outputs a number of blank lines before the given comment (according to the code
     * convention).
     *
     * @param node node to print blank lines for.
     * @param comment the first comment of the given node.
     * @param first <code>true</code> indicates that <em>comment</em> is the first
     *        comment (in case of multiple comments in a row)
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printBlankLinesBefore(
        JavaNode                node,
        CommonHiddenStreamToken comment,
        boolean                 first,
        NodeWriter              out)
      throws IOException
    {
        int blankLines = 0;
        int blankLinesForComment = 0;

        switch (comment.getType())
        {
            case JavaTokenTypes.SL_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.SL_COMMENT :
                    case JavaTokenTypes.BOF :
                        break;

                    case JavaTokenTypes.JAVADOC_COMMENT :
                    case JavaTokenTypes.ML_COMMENT :
                    case JavaTokenTypes.SEPARATOR_COMMENT :
                    case JavaTokenTypes.SPECIAL_COMMENT :

                    //case JavaTokenTypes.SEMI:
                    case JavaTokenTypes.RCURLY :
                    case JavaTokenTypes.CLASS_DEF :
                    case JavaTokenTypes.INTERFACE_DEF :
                    case JavaTokenTypes.VARIABLE_DEF :

                        if (first)
                        {
                            blankLines = getBlankLinesBefore(node, out);
                        }

                        blankLinesForComment = 1;

                        break;

                    default :

                        if (first)
                        {
                            blankLines = getBlankLinesBefore(node, out);
                        }

                        blankLinesForComment =
                            getCommentBlankLines(comment, node.getPreviousSibling(), out);

                        break;
                }

                break;

            case JavaTokenTypes.JAVADOC_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.JAVADOC_COMMENT :
                    case JavaTokenTypes.BOF :
                        break;

                    case JavaTokenTypes.SL_COMMENT :
                    case JavaTokenTypes.ML_COMMENT :
                    case JavaTokenTypes.SEPARATOR_COMMENT :
                    case JavaTokenTypes.SPECIAL_COMMENT :

                    //case JavaTokenTypes.SEMI:
                    case JavaTokenTypes.RCURLY :
                    case JavaTokenTypes.CLASS_DEF :
                    case JavaTokenTypes.INTERFACE_DEF :
                    case JavaTokenTypes.VARIABLE_DEF :

                        if (first)
                        {
                            blankLines = Math.max(getBlankLinesBefore(node, out),out.newline ? 0:1);
                        }

                        //blankLinesForComment = 1;
                        blankLinesForComment =
                            getCommentBlankLines(comment, node.getPreviousSibling(), out);

                        break;

                    default :

                        if (first)
                        {
                            blankLines = Math.max(getBlankLinesBefore(node, out),out.newline ? 0:1);
                        }

                        blankLinesForComment =
                            getCommentBlankLines(comment, node.getPreviousSibling(), out);

                        break;
                }

                break;

            case JavaTokenTypes.ML_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.ML_COMMENT :
                    case JavaTokenTypes.BOF :
                        break;

                    case JavaTokenTypes.SL_COMMENT :
                    case JavaTokenTypes.JAVADOC_COMMENT :
                    case JavaTokenTypes.SEPARATOR_COMMENT :
                    case JavaTokenTypes.SPECIAL_COMMENT :

                        if (first)
                        {
                            blankLines = getBlankLinesBefore(node, out);
                        }

                        blankLinesForComment = 1;

                        break;

                    default :

                        if (first)
                        {
                            blankLines = getBlankLinesBefore(node, out);
                        }

                        blankLinesForComment =
                            getCommentBlankLines(comment, node.getPreviousSibling(), out);

                        break;
                }

                break;

            case JavaTokenTypes.SEPARATOR_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.ML_COMMENT :
                    case JavaTokenTypes.SL_COMMENT :
                    case JavaTokenTypes.JAVADOC_COMMENT :
                    case JavaTokenTypes.SEPARATOR_COMMENT :
                    case JavaTokenTypes.SPECIAL_COMMENT :

                        if (first)
                        {
                            blankLines = getBlankLinesBefore(node, out);
                        }

                        blankLinesForComment = 1;

                        break;

                    case JavaTokenTypes.BOF :
                        break;

                    default :

                        if (first)
                        {
                            blankLines = getBlankLinesBefore(node, out);
                        }

                        blankLinesForComment =
                            getCommentBlankLines(comment, node.getPreviousSibling(), out);

                        break;
                }

                break;

            case JavaTokenTypes.SPECIAL_COMMENT :

                switch (out.last)
                {
                    case JavaTokenTypes.ML_COMMENT :
                    case JavaTokenTypes.SL_COMMENT :
                    case JavaTokenTypes.JAVADOC_COMMENT :
                    case JavaTokenTypes.SEPARATOR_COMMENT :
                    case JavaTokenTypes.SPECIAL_COMMENT :

                        if (first)
                        {
                            blankLines = getBlankLinesBefore(node, out);
                        }

                        blankLinesForComment = 1;

                        break;

                    case JavaTokenTypes.BOF :
                        break;

                    default :

                        if (first)
                        {
                            blankLines = getBlankLinesBefore(node, out);
                        }

                        blankLinesForComment =
                            getCommentBlankLines(comment, node.getPreviousSibling(), out);

                        break;
                }

                break;
        }

        if (canHaveBlankLinesBefore(node))
        {
            out.printBlankLines(Math.max(blankLines, blankLinesForComment));
        }
        else
        {
            if (out.expression != null)
            {
                if (isChildOf(out.expression, node))
                {
                    out.printBlankLines(blankLinesForComment - out.blankLines);
                }
            }
            else
            {
                out.printBlankLines(Math.max(blankLines, blankLinesForComment));
            }
        }
    }


    /**
     * Prints the original amout of blank lines between the two given comments.
     *
     * @param comment the comment.
     * @param previousComment the previous comment.
     * @param keepLines the number of blank lines to keep.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    
    private void printBlankLinesBetweenComments(
        CommonHiddenStreamToken comment,
        CommonHiddenStreamToken previousComment,
        int                     keepLines,
        NodeWriter              out)
      throws IOException
    {
        int lines = 0;

        switch (previousComment.getType())
        {
            case JavaTokenTypes.SPECIAL_COMMENT :
                break;

            default :
                lines = comment.getLine() - previousComment.getLine() - 1;

                break;
        }

        if (lines > keepLines)
        {
            out.printBlankLines(keepLines);
        }
        else if (lines > 0)
        {
            out.printBlankLines(lines);
        }
    }


    /**
     * Prints the given comment after the specified node.
     *
     * @param node the node the comment is attached to.
     * @param comment the comment to print.
     * @param newlineBefore if <code>true</code> a newline will be printed before the
     *        comment.
     * @param newlineAfter if <code>true</code> a newline will be printed after the
     *        comment.
     * @param whitespaceBefore if <code>true</code> the comment will be indented.
     * @param first <code>true</code> indicates that this comment is the first in case
     *        multiple comments are found in a row.
     * @param out the stream to write to.
     *
     * @throws IOException if an I/O error occured.
     * @throws IllegalArgumentException If an error occurs
     */
    private void printCommentAfter(
        AST                     node,
        CommonHiddenStreamToken comment,
        boolean                 newlineBefore,
        boolean                 newlineAfter,
        boolean                 whitespaceBefore,
        boolean                 first,
        NodeWriter              out)
      throws IOException
    {
        int last = out.last;
        boolean restore = false;
        int indent = 0;

        if (
            (comment.getColumn() == 1)
            && (!AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_FIRST_COLUMN_COMMENT,
                ConventionDefaults.INDENT_FIRST_COLUMN_COMMENT)))
        {
            // we should retain first column comments, so we have to
            // change the indentation level
            restore = true;
            indent = out.getIndentLevel();
            out.setIndentLevel(0);
        }

        if (whitespaceBefore)
        {
            out.print(
                out.getString(
                    AbstractPrinter.settings.getInt(
                        ConventionKeys.INDENT_SIZE_COMMENT_ENDLINE,
                        ConventionDefaults.INDENT_SIZE_COMMENT_ENDLINE)),
                JavaTokenTypes.WS);
        }

        switch (comment.getType())
        {
            case JavaTokenTypes.JAVADOC_COMMENT :
                // Javadoc endline comment may indicate badly
                // formatted input source, issue an error here

                /**
                 * @todo allow Javadoc endline comment if it is the first comment, and
                 *       only throw error otherwise
                 */
                throw new IllegalArgumentException(
                    "no valid endline comment -- " + comment + "(attached to " + node
                    + ")");

            case JavaTokenTypes.SL_COMMENT :
                out.print(comment.getText(), comment.getType());

                break;

            case JavaTokenTypes.ML_COMMENT :
                printMultiLineComment(comment, JavaTokenTypes.ML_COMMENT, out);

                break;

            case JavaTokenTypes.SPECIAL_COMMENT :
                printSpecialComment(comment, out);

                break;

            case JavaTokenTypes.SEPARATOR_COMMENT :
                out.print(comment.getText(), comment.getType());

                break;
                
            default :
                throw new IllegalArgumentException("invalid type -- " + comment);
        }

        if (restore)
        {
            // restore current indentation level
            out.setIndentLevel(indent);
        }

        if (newlineAfter || shouldForceNewline(node, comment))
        {
            out.printNewline();
        }

        out.last = last;
    }


    /**
     * Prints the given comment before the specified node.
     *
     * @param node node the comment belongs to.
     * @param comment the comment to print.
     * @param first true indicates that comment is the first comment (in case of multiple comments in a row)
     * @param newlineBefore if <code>true</code>, a line break will be added before each
     *        comment, if appropriate.
     * @param newlineAfter if <code>true</code>, a line break will be added after each
     *        comment, if appropriate.
     * @param out the stream to print to.
     *
     * @throws IOException if an I/O error occured.
     * @throws IllegalArgumentException If an error occurs
     */
    private void printCommentBefore(
        JavaNode                node,
        CommonHiddenStreamToken comment,
        boolean                 first,
        boolean                 newlineBefore,
        boolean                 newlineAfter,
        NodeWriter              out)
      throws IOException
    {
        if (newlineBefore)
            printBlankLinesBefore(node, comment, first, out);

        boolean retain = false;
        int indent = 0;

        if (
            (comment.getType() == JavaTokenTypes.SPECIAL_COMMENT)
            || ((comment.getColumn() == 1)
            && (!AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_FIRST_COLUMN_COMMENT,
                ConventionDefaults.INDENT_FIRST_COLUMN_COMMENT))))
        {
            // we should retain indentation, so we have to change the
            // indentation level
            indent = out.getIndentLevel();
            out.setIndentLevel(0);
            retain = true;
        }

        int type = comment.getType();

        switch (type)
        {
            case JavaTokenTypes.SL_COMMENT :
                out.print(comment.getText() , type);

                break;

            case JavaTokenTypes.JAVADOC_COMMENT :

                ExtendedToken t = (ExtendedToken) comment;
                Node c = t.getComment();

                if (c != null)
                {
                    ((JavadocPrinter) PrinterFactory.create(c, out)).print(node, c, out);
                }
                else
                {
                    printMultiLineComment(comment, JavaTokenTypes.JAVADOC_COMMENT, out);
                }

                break;

            case JavaTokenTypes.ML_COMMENT :
                printMultiLineComment(comment, JavaTokenTypes.ML_COMMENT, out);

                break;

            case JavaTokenTypes.SPECIAL_COMMENT :
                printSpecialComment(comment, out);

                break;

            case JavaTokenTypes.SEPARATOR_COMMENT :
                out.print(comment.getText(), comment.getType());

                // if the node the separator comment belongs to, contains
                // no other comments we want a newline between the two
// TODO Allow this to be configurable 
//                if (comment.getHiddenAfter() == null)
//                {
                    out.printNewline();
//                }

                break;

            default :
                throw new IllegalArgumentException("invalid type -- " + comment);
        }

        if (retain)
        {
            // restore the indentation level
            out.setIndentLevel(indent);
        }

        if (newlineAfter)
        {
            out.printNewline();
        }
    }


    /**
     * Prints the given multi line comment.
     *
     * @param comment the comment token.
     * @param type the type of the comment.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    private void printMultiLineComment(
        CommonHiddenStreamToken comment,
        int                     type,
        NodeWriter              out)
      throws IOException
    {
        // we always split the comment into several lines in order to indent
        // the text correctly (and the proper eol characters are used)
        String[] lines = null;

        boolean format =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.COMMENT_FORMAT_MULTI_LINE,
                ConventionDefaults.COMMENT_FORMAT_MULTI_LINE);

        if (format)
        {
            lines =
                StringHelper.wrapStringToArray(comment.getText(), Integer.MAX_VALUE, out.lineSeparator, false,
                    StringHelper.TRIM_LEADING);
        }
        else
        {
			lines = comment.getText().split( "/\r\n|\n|\r/" /* NOI18N */);
        }

        int lastLine = lines.length - 1;

        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].length() > 0)
            {
                out.print(StringHelper.trimTrailing(lines[i]), type);
            }

            if (i < lastLine)
            {
                out.printNewline();
            }
        }
    }


    /**
     * Prints the given multi line comment.
     *
     * @param comment the comment token.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    private void printSpecialComment(
        CommonHiddenStreamToken comment,
        NodeWriter              out)
      throws IOException
    {
        String[] lines = StringHelper.split(comment.getText(), out.originalLineSeparator);

        int lastLine = lines.length - 1;

        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].length() > 0)
            {
                out.print(
                    StringHelper.trimTrailing(lines[i]), JavaTokenTypes.SPECIAL_COMMENT);
            }

            if (i < lastLine)
            {
                out.printNewline();
            }
        }
    }


    /**
     * Determines wether the given comment makes a line break necessary after the given
     * node.
     *
     * @param node a node.
     * @param comment a comment.
     *
     * @return <code>true</code> if the given comment makes a line break necessary after
     *         the given node.
     */
    private boolean shouldForceNewline(
        AST                     node,
        CommonHiddenStreamToken comment)
    {
        switch (comment.getType())
        {
            case JavaTokenTypes.SL_COMMENT :
            case JavaTokenTypes.SPECIAL_COMMENT :
            case JavaTokenTypes.SEPARATOR_COMMENT :

                if (
                    AbstractPrinter.settings.getBoolean(
                        ConventionKeys.BRACE_NEWLINE_LEFT,
                        ConventionDefaults.BRACE_NEWLINE_LEFT))
                {
                    return (!JavaNodeHelper.isBlockNext(node));
                }

                return true;

            default :
                return false;
        }
    }

    protected void prepareComment(JavaNode lcurly,JavaNode rcurly, NodeWriter out){
        if (out.mode == NodeWriter.MODE_TEST) {
            return;
        }
        String currentFile = "<unknown>";
        if (!out.getFilename().equals("<unknown>"))
            currentFile = out.getFilename();
        else {
            AST tag = lcurly;
            System.out.println(out.mode +"," + NodeWriter.MODE_DEFAULT +"," +NodeWriter.MODE_TEST);
            out.state.args[0] = currentFile;
            out.state.args[1] = new Integer(out.line);
            out.state.args[2] = new Integer(out.column);
            out.state.args[3] = "File name is unknown! Line-";
            out.state.args[4] = new Integer(((Node) tag).getStartLine());
            out.state.args[5] = tag;
                
            Loggers.PRINTER_JAVADOC.l7dlog(
                Level.INFO, "TAG_ADD_MISSING", out.state.args, null);
            
        }
        String commentForNode = getNodeComment(lcurly,out,currentFile);
        
        CommonHiddenStreamToken firstComment =
            rcurly.getHiddenAfter();
        if (firstComment==null){
            // Append comment
            firstComment = new CommonHiddenStreamToken(JavaTokenTypes.SL_COMMENT,commentForNode);
            firstComment.setLine(rcurly.getEndLine()); 
            rcurly.setHiddenAfter(firstComment);
            AST tag = rcurly;
            out.state.args[0] = currentFile;
            out.state.args[1] = new Integer(out.line);
            out.state.args[2] = new Integer(out.column);
            out.state.args[3] = commentForNode;
            out.state.args[4] = " (Line "+((Node) tag).getStartLine()+")";
            out.state.args[5] = tag;
                
            Loggers.PRINTER_JAVADOC.l7dlog(
                Level.WARN, "TAG_ADD_MISSING", out.state.args, null);
        }
        else {
            if (!firstComment.getText().equals(commentForNode)){
                AST tag = rcurly;
                out.state.args[0] = currentFile;
                out.state.args[1] = new Integer(out.line);
                out.state.args[2] = new Integer(out.column);
                out.state.args[3] = firstComment.getText(); 
                out.state.args[4] = commentForNode;
                out.state.args[5] = tag;
                Loggers.PRINTER_JAVADOC.l7dlog(
                    Level.WARN, "TAG_MISSPELLED_NAME", out.state.args, null);
                firstComment.setText(commentForNode);   
            }
            
        }
        
    }
    private String getNodeComment(JavaNode node, NodeWriter out, String currentFile) {
       JavaNode parent = node.getParent();
       String comment = "// mt comment";
       JavaNode anext = null;
       if (parent!=null){
            switch(parent.getType()){
                
                case JavaTokenTypes.LITERAL_if:
                if (node.getPreviousSibling().getType() == JavaTokenTypes.LITERAL_else){
                    comment="// end else";
                }
                else {
                    if (parent.getPreviousSibling().getType() == JavaTokenTypes.LITERAL_else){
                        comment = "// end else if";
                    }
                    else
                        comment="// end if";
                }
                //comment = comment +", p"+parent+",c"+node;
                break;
                case JavaTokenTypes.LITERAL_switch:
                comment="// end switch";
                break;
                case JavaTokenTypes.LITERAL_else:
                comment="// end if else";
                break;
                case JavaTokenTypes.LITERAL_catch:
                comment = "// end catch";
                break;
                case JavaTokenTypes.LITERAL_try:
                comment = "// end try";
                break;
                case JavaTokenTypes.LITERAL_while:
                comment = "// end while";
                break;
                case JavaTokenTypes.LITERAL_do:
                comment = "// end do";
                break;
                case JavaTokenTypes.LITERAL_for:
                comment = "// end for";
                break;
                case JavaTokenTypes.ENUM_DEF:
                comment = "// end enum";
                break;
                case JavaTokenTypes.LITERAL_finally:
                comment = "// end finally";
                break;
                case JavaTokenTypes.LITERAL_new:
                comment = "// end new";
                break;
                case JavaTokenTypes.CASESLIST:
                comment = "// end case";
                break;
                case JavaTokenTypes.SYNBLOCK:
                comment = "// end synchronized";
                break;
                case JavaTokenTypes.INTERFACE_DEF:
                anext = (JavaNode)parent.getFirstChild();
                while((anext=(JavaNode)anext.getNextSibling())!=null){
                    if (anext.getType()==JavaTokenTypes.IDENT){
                        break;
                    }
                }
                if (anext !=null){
                    comment="// end "+anext.getText();                    
                } 
                else 
                    comment="// TODO not found ";
                break;
                
                case JavaTokenTypes.LITERAL_static:
                    comment = "// end static";
                    break;
                case JavaTokenTypes.METHOD_DEF:
                anext = (JavaNode)parent.getFirstChild();
                while((anext=(JavaNode)anext.getNextSibling())!=null){
                    if (anext.getType()==JavaTokenTypes.IDENT){
                        break;
                    }
                }
                if (anext !=null){
                    comment="// end "+anext.getText()+"()";                    
                } 
                else 
                    comment="// TODO not found ";
                break;
                
                case JavaTokenTypes.CTOR_DEF:
                // If the parent is a constructor this is an end of method
                // Back traverse the current node till we hit the method_def
                JavaNode prev = node;
                while((prev=prev.getPreviousSibling())!=null) {
                    if (prev.getType()==JavaTokenTypes.IDENT)
                    break;                    
                }
                
                if (prev!=null){
                    comment="// end "+prev.getText()+"()";                    
                } 
                else 
                    comment="// TODO not found ";
                break;
                
                
                case JavaTokenTypes.CLASS_DEF:
                anext = (JavaNode)parent.getFirstChild();
                while((anext=(JavaNode)anext.getNextSibling())!=null){
                    if (anext.getType()==JavaTokenTypes.IDENT){
                        break;
                    }
                }
                if (anext !=null){
                    comment="// end "+anext.getText();                    
                } 
                else 
                    comment="// TODO not found ";
                break;
                
                default:
                comment = ""; //"// TODO "+parent+" "+out.getLast()+" "+node.getType()+" TODO unknown type "+parent.getType();
                AST tag = node;
                out.state.args[0] = currentFile;
                out.state.args[1] = new Integer(out.line);
                out.state.args[2] = new Integer(out.column);
                out.state.args[3] = "Unknown code "+new StringBuffer().append(parent); 
                out.state.args[4] = new StringBuffer().append(parent);
                out.state.args[5] = tag;
                Loggers.PRINTER_JAVADOC.l7dlog(
                    Level.WARN, "GENERATE_COMMENT", out.state.args, null);
                    
            }
       }
       return comment;
    }
    /**
     * Counts the number of blank lines following a node
     * 
     * @param n The node
     * @return The number of lines
     */
    protected int countChildrenLines(JavaNode n, int current) {
        for(;n!=null;n=(JavaNode) n.getNextSibling()) {
            current += n.nlAfter;   
        }
        return current;
    }
}
