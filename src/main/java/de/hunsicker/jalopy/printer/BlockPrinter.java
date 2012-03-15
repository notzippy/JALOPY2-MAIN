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
 * Printer for statement or object blocks [<code>SLIST</code> and <code>OBJBLOCK</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.12 $
 */
class BlockPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new BlockPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new BlockPrinter object.
     */
    protected BlockPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return the sole instance of this class.
     */
    public static Printer getInstance()
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
        JavaNode lcurly = (JavaNode) node;

        // always print a newline before the left curly brace
        boolean forceNewlineBefore = out.state.newlineBeforeLeftBrace;
        boolean newLineAfter = false;

        boolean treatDifferent =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_TREAT_DIFFERENT,
                ConventionDefaults.BRACE_TREAT_DIFFERENT);

        if (treatDifferent)
        {
            switch (lcurly.getParent().getType())
            {
                case JavaTokenTypes.METHOD_DEF :
                case JavaTokenTypes.CTOR_DEF :
                case JavaTokenTypes.CLASS_DEF :
                case JavaTokenTypes.INTERFACE_DEF :
                    forceNewlineBefore = true;

                    break;

                case JavaTokenTypes.CASESLIST :
                    forceNewlineBefore = false;
                    treatDifferent = false;

                    break;
                case JavaTokenTypes.ENUM_DEF:
                case JavaTokenTypes.ANNOTATION_DEF:
                    forceNewlineBefore = false;
                	break;

                default :
                    treatDifferent = false;

                    break;
            }
        }

        boolean freestanding = JavaNodeHelper.isFreestandingBlock(lcurly);
        boolean cuddleEmpty =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_EMPTY_CUDDLE, ConventionDefaults.BRACE_EMPTY_CUDDLE);
        boolean insertEmptyStatement =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_EMPTY_INSERT_STATEMENT,
                ConventionDefaults.BRACE_EMPTY_INSERT_STATEMENT);
        boolean leftBraceNewline =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_NEWLINE_LEFT, ConventionDefaults.BRACE_NEWLINE_LEFT);

        // do we print a SLIST or an OBJBLOCK?
        int closeBraceType =
            ((node.getType() == JavaTokenTypes.SLIST) ? JavaTokenTypes.RCURLY
                                                      : JavaTokenTypes.OBJBLOCK);

        if (JavaNodeHelper.isEmptyBlock(node))
        {
            // only insert empty statement for SLIST, not for OBJBLOCK
            if (insertEmptyStatement)
            {
                switch (((JavaNode) node).getParent().getType())
                {
                    case JavaTokenTypes.METHOD_DEF :
                    case JavaTokenTypes.CTOR_DEF :
                    case JavaTokenTypes.CLASS_DEF :
                    case JavaTokenTypes.INTERFACE_DEF :
                    case JavaTokenTypes.ENUM_DEF:
                    case JavaTokenTypes.ANNOTATION_DEF:
                        // it does not make sense to add the empty statement
                        // for class, ctor or method bodies
                        break;

                    default :
                        printBracesEmptyStatement(
                            lcurly, leftBraceNewline, forceNewlineBefore, freestanding,
                            closeBraceType, out);

                        return;
                }
            }
            else if (cuddleEmpty && !lcurly.getParent().hasCommentsAfter())
            {
                printBracesCuddled(closeBraceType, lcurly, NodeWriter.NEWLINE_YES, out);

                return;
            }
        }

        // check if we have empty braces
        switch (out.last)
        {
            case JavaTokenTypes.LITERAL_default :
            case JavaTokenTypes.LITERAL_case :
                // not needed for a switch statement
                break;

            default :

                if (node.getFirstChild() == null)
                {
                    if (insertEmptyStatement)
                    {
                        printBracesEmptyStatement(
                            lcurly, leftBraceNewline, forceNewlineBefore, freestanding,
                            closeBraceType, out);
                    }
                    else if (cuddleEmpty && !lcurly.getParent().hasCommentsAfter())
                    {
                        printBracesCuddled(
                            closeBraceType, lcurly, NodeWriter.NEWLINE_NO, out);
                    }
                    else
                    {
                        /**
                         * @todo handle class/ifc/method/ctor different
                         */
                        int offset = out.printLeftBrace();

                        trackPosition(lcurly, out.line - 1, offset, out);

                        if (out.state.newlineBeforeLeftBrace)
                        {
                            out.state.newlineBeforeLeftBrace = false;
                        }

                        out.printRightBrace(
                            JavaTokenTypes.RCURLY, !treatDifferent, NodeWriter.NEWLINE_YES);

                        JavaNode rcurly = (JavaNode) lcurly.getFirstChild();

                        trackPosition(rcurly, out.line - 1, offset + 1, out);

                        out.last = closeBraceType;
                    }

                    // empty braces printed, we're finished
                    return;
                }
        }

        boolean brace = true;
        boolean indent = false;
        boolean forceNewLineAfter =true;
	    int wrapLineCount = Integer.MAX_VALUE;
        boolean removeBlockBraces =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_REMOVE_BLOCK, ConventionDefaults.BRACE_REMOVE_BLOCK);

        if (freestanding)
        {
            if (removeBlockBraces && canRemoveBraces(lcurly))
            {
                /**
                 * @todo implement for HiddenStreamToken
                 */

                /*
                if (lcurly.hasCommentsBefore())
                {
                    JavaNode statement = (JavaNode)lcurly.getFirstChild();

                    if (statement.hasCommentsBefore())
                    {

                        List braceComments = lcurly.getCommentsBefore();
                        List statementComments = statement.getCommentsBefore();

                        for (int i = 0, size = braceComments.size();
                             i < size;
                             i++)
                        {
                            statementComments.add(i, braceComments.get(i));
                        }

                        lcurly.setCommentsBefore(null);
                    }
                }
                */
                brace = false;
            }
            switch(lcurly.getParent().getType()) {
                case JavaTokenTypes.ANNOTATION:
                    forceNewLineAfter = !AbstractPrinter.settings.getBoolean(ConventionKeys.ANON_LCURLY_NO_NEW_LINE,
                                                                            ConventionDefaults.ANON_LCURLY_NO_NEW_LINE);
                    wrapLineCount = AbstractPrinter.settings.getInt(
                                                                        ConventionKeys.ANON_ALIGN_VALUES_WHEN_EXCEEDS,
                                                                        ConventionDefaults.ANON_ALIGN_VALUES_WHEN_EXCEEDS);
                    newLineAfter = false;
                break;
            }

            indent = false;
        }
        else
        {
            // remove braces if not necessary and possible
            switch (out.last)
            {
                // TRUE for both if and else (see IfElsePrinter.java)
                case JavaTokenTypes.LITERAL_if :

                    if (
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.BRACE_REMOVE_IF_ELSE,
                            ConventionDefaults.BRACE_REMOVE_IF_ELSE))
                    {
                        if (!isBraceNecessary(node))
                        {
                            brace = false;
                            indent = true;
                            out.printNewline();
                        }
                    }

                    break;

                case JavaTokenTypes.LITERAL_for :

                    if (
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.BRACE_REMOVE_FOR,
                            ConventionDefaults.BRACE_REMOVE_FOR))
                    {
                        if (!isBraceNecessary(node))
                        {
                            brace = false;
                            indent = true;
                            out.printNewline();
                        }
                    }

                    break;

                case JavaTokenTypes.LITERAL_while :

                    if (
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.BRACE_REMOVE_WHILE,
                            ConventionDefaults.BRACE_REMOVE_WHILE))
                    {
                        if (!isBraceNecessary(node))
                        {
                            brace = false;
                            indent = true;
                            out.printNewline();
                        }
                    }

                    break;

                case JavaTokenTypes.LITERAL_do :

                    if (
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.BRACE_REMOVE_DO_WHILE,
                            ConventionDefaults.BRACE_REMOVE_DO_WHILE))
                    {
                        if (!isBraceNecessary(node))
                        {
                            brace = false;
                            indent = true;
                            out.printNewline();
                        }
                    }

                    break;
                case JavaTokenTypes.LITERAL_enum:
                    newLineAfter = false;
                	forceNewLineAfter = !AbstractPrinter.settings.getBoolean(ConventionKeys.ENUM_LCURLY_NO_NEW_LINE,
                                                                        ConventionDefaults.ENUM_LCURLY_NO_NEW_LINE);
                    wrapLineCount = AbstractPrinter.settings.getInt(
                                                                    ConventionKeys.ENUM_ALIGN_VALUES_WHEN_EXCEEDS,
                                                                    ConventionDefaults.ENUM_ALIGN_VALUES_WHEN_EXCEEDS);
                	indent=true;
            	break;
            	
                case JavaTokenTypes.AT:
                	forceNewLineAfter = !AbstractPrinter.settings.getBoolean(ConventionKeys.ANON_DEF_LCURLY_NO_NEW_LINE,
                                                                             ConventionDefaults.ANON_DEF_LCURLY_NO_NEW_LINE);
                    wrapLineCount = AbstractPrinter.settings.getInt(
                                                                    ConventionKeys.ANON_DEF_ALIGN_VALUES_WHEN_EXCEEDS,
                                                                    ConventionDefaults.ANON_DEF_ALIGN_VALUES_WHEN_EXCEEDS);
	            	newLineAfter = false;
	            	indent=true;
                	break;

                case JavaTokenTypes.LITERAL_case :
                case JavaTokenTypes.LITERAL_default :

                    if (!isBraceNecessary(node))
                    {
                        brace = false;
                        indent = true;
                    }

                    leftBraceNewline = false;

                    break;
                    default :
                        switch(lcurly.getParent().getType()) {
                            case JavaTokenTypes.ANNOTATION:
                            	forceNewLineAfter = !AbstractPrinter.settings.getBoolean(ConventionKeys.ANON_LCURLY_NO_NEW_LINE,
                                                                                        ConventionDefaults.ANON_LCURLY_NO_NEW_LINE);
                                wrapLineCount = AbstractPrinter.settings.getInt(
                                                                                ConventionKeys.ANON_ALIGN_VALUES_WHEN_EXCEEDS,
                                                                                ConventionDefaults.ANON_ALIGN_VALUES_WHEN_EXCEEDS);
           	            	newLineAfter = false;
           	            	indent=true;
                           	break;
                        }
            }
        }

        if (brace)
        {
            boolean indentBrace = !freestanding && !forceNewlineBefore;

            printLeftBrace(
                lcurly, leftBraceNewline, forceNewlineBefore,forceNewLineAfter, freestanding, indentBrace, out);
        }
        
        else if (indent)
        {
            out.indent();

            // we suppress the printing of braces, but for the sake of
            // simplicity we let our ancestors think we doesn't
            out.last = JavaTokenTypes.LCURLY;
        }

        JavaNode rcurly = null;
        int enumCounter = 1;
        int currentLine = out.line;

LOOP:

        // print everything despite the closing curly brace
        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.RCURLY :
                    rcurly = (JavaNode) child;

                    break LOOP;
               case JavaTokenTypes.COMMA :
                   out.nextNewline = newLineAfter;
                       PrinterFactory.create(child, out).print(child, out);
	               if (newLineAfter) {
	                   out.printNewline();
                       out.nextNewline = false;
	               }
               break;
                   

                default :
                    if (wrapLineCount!=Integer.MAX_VALUE) {
                        if (++enumCounter>wrapLineCount) {
                            enumCounter = 1;
                            newLineAfter = true;
                        }
                        else {
                            newLineAfter = false;
                        }
                    }
                    currentLine = out.line;
                    out.nextNewline = newLineAfter;
                    PrinterFactory.create(child, out).print(child, out);
                    if (currentLine == out.line) {
                        AST nextNode = child.getNextSibling();
                        if (newLineAfter && nextNode!=null && nextNode.getType()!=JavaTokenTypes.COMMA) {
                            newLineAfter = false;
                            out.printNewline();
                        }
                    }
                    else {
                        // If already on a new line reset the counter and the newLineAfter flag 
                        newLineAfter = false;
                        enumCounter = 0;
                    }
                    out.nextNewline = false;
                    

                    break;
            }
        }

        if (brace)
        {
            
            printCommentsBefore(rcurly, out);

            boolean rightBraceNewline =
                isCloseBraceNewline(lcurly, closeBraceType, freestanding);
            
            boolean addCustomComment = AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_ADD_COMMENT, ConventionDefaults.BRACE_ADD_COMMENT);
            
            int offset =
                out.printRightBrace(
                    closeBraceType, !treatDifferent && !freestanding, rightBraceNewline && !rcurly.hasCommentsAfter() && !addCustomComment);

            trackPosition(
                rcurly, rightBraceNewline ? (out.line - 1)
                                          : out.line, offset, out);
            
            if (addCustomComment) {
                prepareComment(lcurly,rcurly,out);
            }

            printCommentsAfter(rcurly, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_NO, out);
            //if (!freestanding && !forceNewlineBefore) {
            //    out.unindent();
           // }
        }
        else if (indent)
        {
            out.unindent();
        }
    }


    /**
     * Determines whether braces are necessary for the given block.
     *
     * @param node first node of the block to check. Either OBJBLOCK, SLIST,
     *        INSTANCE_INIT or LCURLY.
     *
     * @return <code>true</code> if braces must be printed for the given block.
     */
    private boolean isBraceNecessary(AST node)
    {
        switch (node.getType())
        {
            // always print braces for OBJBLOCKs
            case JavaTokenTypes.OBJBLOCK :
                return true;
        }

        int count = 0;

        // count the number of braces/statements in the block
        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (count++)
            {
                case 0 :

                    switch (child.getType())
                    {
                        case JavaTokenTypes.VARIABLE_DEF :
                            return true;

                        case JavaTokenTypes.EXPR :
                        case JavaTokenTypes.LITERAL_return :
                        case JavaTokenTypes.LITERAL_throw :
                        case JavaTokenTypes.LITERAL_break :
                        case JavaTokenTypes.LITERAL_continue :
                        case JavaTokenTypes.EMPTY_STAT :
                            // we have to look further, continue search
                            break;

                        case JavaTokenTypes.LCURLY :

                            JavaNode lcurly = (JavaNode) child;

                            // never remove braces if we find associated
                            // comments
                            if (lcurly.hasCommentsBefore() || lcurly.hasCommentsAfter())
                            {
                                return true;
                            }

                            break;

                        // we assume we need braces
                        default :
                            return true;
                    }

                    break;

                case 1 :

                    switch (child.getType())
                    {
                        case JavaTokenTypes.RCURLY :

                            JavaNode rcurly = (JavaNode) child;

                            // never remove braces if we find associated
                            // comments
                            if (rcurly.hasCommentsBefore() || rcurly.hasCommentsAfter())
                            {
                                return true;
                            }

                            return false;
                    }

                    break;

                // 3 or more childs means we always need braces
                case 2 :
                    return true;
            }
        }

        return false;
    }


    /**
     * Determines whether the closing brace of a block should have printed a following
     * line break.
     *
     * @param node the <strong>opening</strong> brace of the block.
     * @param type the block type. Either OBJBLOCK or SLIST.
     * @param freestanding <code>true</code> indicates that the block is a freestanding
     *        block.
     *
     * @return <code>true</code> if a line break should be printed after the closing
     *         brace.
     *
     * @since 1.0b8
     */
    private boolean isCloseBraceNewline(
        JavaNode node,
        int      type,
        boolean  freestanding)
    {
        boolean rightBraceNewline = true;

        if (type == JavaTokenTypes.OBJBLOCK)
        {
            switch (node.getParent().getType()) {
             case JavaTokenTypes.LITERAL_new : return false;
             case JavaTokenTypes.ANNOTATION : return false;
             default : return true;
            }
        }
        else if (!freestanding)
        {
            JavaNode next = (JavaNode) node.getNextSibling();

            if (next != null)
            {
                switch (next.getType())
                {
                    case JavaTokenTypes.LITERAL_else : // else part of if-else
                    case JavaTokenTypes.LITERAL_catch : // catch block
                    case JavaTokenTypes.LITERAL_finally : // finally block
                        rightBraceNewline =
                            AbstractPrinter.settings.getBoolean(
                                ConventionKeys.BRACE_NEWLINE_RIGHT,
                                ConventionDefaults.BRACE_NEWLINE_RIGHT);

                        break;

                    case JavaTokenTypes.LITERAL_while :

                        switch (next.getParent().getType())
                        {
                            case JavaTokenTypes.LITERAL_do : // do-while block
                                rightBraceNewline =
                                    AbstractPrinter.settings.getBoolean(
                                        ConventionKeys.BRACE_NEWLINE_RIGHT,
                                        ConventionDefaults.BRACE_NEWLINE_RIGHT);

                                break;
                        }

                        break;
                }
            }
            else
            {
                JavaNode parent = node.getParent();

                switch (parent.getType())
                {
                    case JavaTokenTypes.LITERAL_catch :

                        AST n = parent.getNextSibling();

                        if (n != null)
                        {
                            switch (n.getType())
                            {
                                case JavaTokenTypes.LITERAL_catch :
                                case JavaTokenTypes.LITERAL_finally :
                                    rightBraceNewline =
                                        AbstractPrinter.settings.getBoolean(
                                            ConventionKeys.BRACE_NEWLINE_RIGHT,
                                            ConventionDefaults.BRACE_NEWLINE_RIGHT);

                                    break;
                            }
                        }

                        break;
                }
            }
        }

        return rightBraceNewline;
    }


    /**
     * Determines whether the braces of the given node may be savely removed.
     *
     * <p>
     * If we want to omit braces we have to check whether there is a VARIABLE_DEF in the
     * given scope, in which case we have to print braces as we don't know whether the
     * exact same definition occurs in other scopes which would lead to compilation
     * errors (we change scope if we remove braces).
     * </p>
     *
     * @param lcurly open curly brace of the node.
     *
     * @return <code>true</code> if the braces of the given node can be removed.
     */
    private boolean canRemoveBraces(JavaNode lcurly)
    {
        switch (lcurly.getParent().getType())
        {
            case JavaTokenTypes.INSTANCE_INIT :
            case JavaTokenTypes.ANNOTATION :
                return false;
        }

        if (lcurly.hasCommentsBefore())
        {
            return false;
        }

        for (
            AST child = lcurly.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.VARIABLE_DEF :
                    return false;
            }
        }

        return true;
    }


    /**
     * Prints an empty pair of braces cuddled (on one line).
     *
     * @param type The type
     * @param lcurly The left curly
     * @param newlineAfter if <code>true</code> a newline should be printed after the
     *        brace.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printBracesCuddled(
        int        type,
        JavaNode   lcurly,
        boolean    newlineAfter,
        NodeWriter out)
      throws IOException
    {
        out.print(
            out.getString(
                AbstractPrinter.settings.getInt(
                    ConventionKeys.INDENT_SIZE_BRACE_CUDDLED,
                    ConventionDefaults.INDENT_SIZE_BRACE_CUDDLED)), JavaTokenTypes.WS);
        out.print(BRACES, type);

        if (out.state.newlineBeforeLeftBrace)
        {
            out.state.newlineBeforeLeftBrace = false;
        }

        JavaNode rcurly = (JavaNode) lcurly.getFirstChild();

        if (
            !printCommentsAfter(rcurly, NodeWriter.NEWLINE_NO, newlineAfter, out)
            && newlineAfter)
        {
            out.printNewline();
        }
    }


    /**
     * Prints a pair of braces with a single empty statement.
     *
     * @param lcurly The left curly
     * @param leftBraceNewline Flag
     * @param forceNewlineBefore Flag
     * @param freestanding Flag
     * @param type The node type
     * @param out The output
     *
     * @throws IOException If an IO error occurs
     */
    private void printBracesEmptyStatement(
        JavaNode   lcurly,
        boolean    leftBraceNewline,
        boolean    forceNewlineBefore,
        boolean    freestanding,
        int        type,
        NodeWriter out)
      throws IOException
    {
        JavaNode rcurly = (JavaNode) lcurly.getFirstChild();

        printLeftBrace(lcurly, leftBraceNewline, forceNewlineBefore,false, freestanding, true, out);

        if (out.state.newlineBeforeLeftBrace)
        {
            out.state.newlineBeforeLeftBrace = false;
        }

        out.print(SEMI, out.last);
        out.printNewline();

        boolean newLineAfterBrace = isCloseBraceNewline(lcurly, type, freestanding);

        int offset = out.printRightBrace(type, NodeWriter.NEWLINE_NO);

        trackPosition(rcurly, out.line, offset, out);

        if (
            !printCommentsAfter(rcurly, NodeWriter.NEWLINE_NO, newLineAfterBrace, out)
            && newLineAfterBrace)
        {
            out.printNewline();
        }

        out.last = type;
    }
/*
    private void printLeftBrace(
                                JavaNode   lcurly,
                                boolean    leftBraceNewline,
                                boolean    forceNewlineBefore,
                                boolean    freestanding,
                                boolean    indent,
                                NodeWriter out)
                              throws IOException
                            {
        printLeftBrace(lcurly,leftBraceNewline, forceNewlineBefore, true,freestanding,indent,out);
                            }
    */
    /**
     * Prints the opening curly brace.
     *
     * @param lcurly the LCURLY node.
     * @param leftBraceNewline issue a line break before the brace.
     * @param forceNewlineBefore force a line break.
     * @param freestanding <code>true</code> indicates a freestanding block. For
     *        freestanding blocks, we never want to ouput additional leading
     *        indentation.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printLeftBrace(
        JavaNode   lcurly,
        boolean    leftBraceNewline,
        boolean    forceNewlineBefore,
        boolean    forceNewLineAfter,
        boolean    freestanding,
        boolean    indent,
        NodeWriter out)
      throws IOException
    {
        if (lcurly.hasCommentsBefore() || freestanding)
        {
            printCommentsBefore(lcurly, NodeWriter.NEWLINE_NO, out);
        }

        boolean commentsAfter = lcurly.hasCommentsAfter();
        forceNewLineAfter = forceNewLineAfter && !commentsAfter;

        if (freestanding)
        {
            int offset =
                out.printLeftBrace(
                    NodeWriter.NEWLINE_NO, forceNewLineAfter, NodeWriter.INDENT_NO);

            trackPosition(lcurly, commentsAfter ? out.line
                                                : (out.line - 1), offset, out);
        }
        else
        {
            if (forceNewlineBefore)
            {
                int offset =
                    out.printLeftBrace(
                        NodeWriter.NEWLINE_YES, forceNewLineAfter, NodeWriter.INDENT_NO);

                trackPosition(
                    lcurly, commentsAfter ? out.line
                                          : (out.line - 1), offset, out);
            }
            else
            {
                if (out.newline)
                {
                    int offset =
                        out.printLeftBrace(
                            NodeWriter.NEWLINE_NO, forceNewLineAfter, indent && NodeWriter.INDENT_YES);

                    trackPosition(
                        lcurly, commentsAfter ? out.line
                                              : (out.line - 1), offset, out);
                }
                else
                {
                    int offset =
                        out.printLeftBrace(
                            leftBraceNewline, forceNewLineAfter, indent && NodeWriter.INDENT_YES);

                    trackPosition(
                        lcurly, commentsAfter ? out.line
                                              : (out.line - 1), offset, out);
                }
            }
        }

        // another micro optimization: if we encounter an endline
        // comment after the lcurly but we print newlines after
        // braces, it looks bad if we print the comment as an endline
        // comment if the braces are empty, so we issue a line break
        //
        //      { // endline comment
        //      }
        //
        //          vs.
        //      {
        //          // endline comment
        //      }
        if (leftBraceNewline)
        {
            if (commentsAfter)
            {
                AST next = lcurly.getFirstChild();

                if ((next != null) && (next.getType() == JavaTokenTypes.RCURLY))
                {
                    out.printNewline();
                }
            }
        }

        if (out.state.newlineBeforeLeftBrace)
        {
            out.state.newlineBeforeLeftBrace = false;
        }

        if (commentsAfter)
        {
            printCommentsAfter(
                lcurly, NodeWriter.NEWLINE_YES, NodeWriter.NEWLINE_YES, out);
        }
    }
}
