/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;
import de.hunsicker.jalopy.language.JavaNodeHelper;
import de.hunsicker.jalopy.language.antlr.ExtendedToken;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.Environment;
import de.hunsicker.jalopy.storage.History;
import de.hunsicker.util.StringHelper;


/**
 * Printer for a Java AST.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.9 $
 */
final class JavaPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The empty string array. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /** Singleton. */
    private static final Printer INSTANCE = new JavaPrinter();
    private static final String DELIMETER = "|" /* NOI18N */;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new JavaPrinter object.
     */
    protected JavaPrinter()
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
        out.environment.set(
            Environment.Variable.CONVENTION.getName(),
            AbstractPrinter.settings.get(
                ConventionKeys.CONVENTION_NAME, ConventionDefaults.CONVENTION_NAME));

        try
        {
            History.Policy historyPolicy =
                History.Policy.valueOf(
                    AbstractPrinter.settings.get(
                        ConventionKeys.HISTORY_POLICY, ConventionDefaults.HISTORY_POLICY));
            boolean useCommentHistory = (historyPolicy == History.Policy.COMMENT);
            boolean useHeader = AbstractPrinter.settings.getBoolean(ConventionKeys.HEADER, false);
            boolean ignoreHeaderIfExists = AbstractPrinter.settings.getBoolean(ConventionKeys.HEADER_IGNORE_IF_EXISTS, true);

            if (((useHeader) && ( ! ignoreHeaderIfExists)) || useCommentHistory)
            {
                removeHeader(node, useCommentHistory);
            }

            if (useHeader)
            {
                if (( ! ignoreHeaderIfExists) || ( ! headerExists(node))) {
                    printHeader(out);
                }
            }

            boolean useFooter =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.FOOTER, ConventionDefaults.FOOTER);
            boolean ignoreFooterIfExists = AbstractPrinter.settings.getBoolean(ConventionKeys.FOOTER_IGNORE_IF_EXISTS, true);

            if (useFooter && ( ! ignoreFooterIfExists))
            {
                removeFooter(node);
            }

            for (
                AST child = node.getFirstChild(); child != null;
                child = child.getNextSibling())
            {
                PrinterFactory.create(child, out).print(child, out);
            }

            if (useFooter)
            {
                if (( ! ignoreFooterIfExists) || ( ! footerExists(node))) {
                    printFooter(out);
                }
            }
        }
        finally
        {
            out.environment.unset(Environment.Variable.CONVENTION.getName());
        }
    }


    /**
     * Returns the identify keys as stored in the code convention.
     *
     * @param key code convention key.
     *
     * @return identify keys. If no keys are stored, an empty array will be returned.
     */
    private String[] getConventionKeys(Convention.Key key)
    {
        List keys = new ArrayList();
        String str = AbstractPrinter.settings.get(key, EMPTY_STRING);

        for (
            StringTokenizer tokens = new StringTokenizer(str, DELIMETER);
            tokens.hasMoreElements();)
        {
            keys.add(tokens.nextElement());
        }

        return (String[]) keys.toArray(EMPTY_STRING_ARRAY);
    }


    /**
     * Returns the last node of the Java AST.
     *
     * @param root root node the AST
     *
     * @return the last node of the AST.
     *
     * @throws IllegalStateException DOCUMENT ME!
     *
     * @since 1.0b9
     */
    private JavaNode getLastElement(AST root)
    {
        for (
            AST declaration = root.getFirstChild(); declaration != null;
            declaration = declaration.getNextSibling())
        {
            // last top-level declaration
            if (declaration.getNextSibling() == null)
            {
                switch (declaration.getType())
                {
                    case JavaTokenTypes.CLASS_DEF :
                    case JavaTokenTypes.INTERFACE_DEF :

                        AST block =
                            JavaNodeHelper.getFirstChild(
                                declaration, JavaTokenTypes.OBJBLOCK);

                        for (
                            AST element = block.getFirstChild(); element != null;
                            element = element.getNextSibling())
                        {
                            // last RCURLY
                            if (element.getNextSibling() == null)
                            {
                                switch (element.getType())
                                {
                                    case JavaTokenTypes.RCURLY :
                                        return (JavaNode) element;
                                }
                            }
                        }

                        break;

                    case JavaTokenTypes.SEMI :
                    case JavaTokenTypes.EOF :
                        return (JavaNode) declaration;

                    case JavaTokenTypes.PACKAGE_DEF :
                    case JavaTokenTypes.IMPORT :

                        /**
                         * TODO implement ??
                         */
                        return (JavaNode) declaration;
                }
            }
        }

        throw new IllegalStateException("invalid AST -- " + root);
    }


    /**
     * Prints the footer.
     *
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printFooter(NodeWriter out)
      throws IOException
    {
        String text =
            out.environment.interpolate(
                AbstractPrinter.settings.get(ConventionKeys.FOOTER_TEXT, EMPTY_STRING));
        String[] footer = StringHelper.split(text, DELIMETER);

        if (footer.length > 0)
        {
            switch (out.last)
            {
                case JavaTokenTypes.RCURLY :
                case JavaTokenTypes.OBJBLOCK :
                case JavaTokenTypes.CLASS_DEF :
                case JavaTokenTypes.INTERFACE_DEF :
                    // print one extra after the last curly brace of a file
                    out.printNewline();

                    break;
            }

            out.printBlankLines(
                AbstractPrinter.settings.getInt(
                    ConventionKeys.BLANK_LINES_BEFORE_FOOTER,
                    ConventionDefaults.BLANK_LINES_BEFORE_FOOTER));

            for (int i = 0; i < footer.length; i++)
            {
                out.print(footer[i], JavaTokenTypes.ML_COMMENT);

                if (i < (footer.length - 1))
                {
                    out.printNewline();
                }
            }

            int blankLinesAfter =
                AbstractPrinter.settings.getInt(
                    ConventionKeys.BLANK_LINES_AFTER_FOOTER,
                    ConventionDefaults.BLANK_LINES_AFTER_FOOTER);

            // always print at least one empty line to be in sync with the
            // Java specs in case the footer consists of several single-line
            // comments
            if (blankLinesAfter == 0)
            {
                blankLinesAfter = 1;
            }

            out.printBlankLines(blankLinesAfter);
        }
    }


    /**
     * Prints the header.
     *
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printHeader(NodeWriter out)
      throws IOException
    {
        String text =
            out.environment.interpolate(
                AbstractPrinter.settings.get(ConventionKeys.HEADER_TEXT, EMPTY_STRING));
        String[] header = StringHelper.split(text, DELIMETER);

        if (header.length > 0)
        {
            out.printBlankLines(
                AbstractPrinter.settings.getInt(
                    ConventionKeys.BLANK_LINES_BEFORE_HEADER,
                    ConventionDefaults.BLANK_LINES_BEFORE_HEADER));

            for (int i = 0; i < header.length; i++)
            {
                out.print(header[i], JavaTokenTypes.ML_COMMENT);
                out.printNewline();
            }

            out.printBlankLines(
                AbstractPrinter.settings.getInt(
                    ConventionKeys.BLANK_LINES_AFTER_HEADER,
                    ConventionDefaults.BLANK_LINES_AFTER_HEADER));

            out.last = JavaTokenTypes.ML_COMMENT;
        }
    }


    /**
     * Removes the footer. The footer is actually the JavaTokenTyps.EOF node.
     * Comments are actually before this node not after it.
     *
     * @param root the root node of the Java AST.
     *
     * @since 1.0b8
     */
    private void removeFooter(AST root)
    {
        JavaNode eofNode = getLastElement(root);

        if (eofNode.hasCommentsBefore())
        {
            String[] keys = getConventionKeys(ConventionKeys.FOOTER_KEYS);
            int count = 0;
            int smartModeLines =
                AbstractPrinter.settings.getInt(ConventionKeys.FOOTER_SMART_MODE_LINES, 0);
            boolean smartMode = smartModeLines > 0;

            for (
                CommonHiddenStreamToken comment = eofNode.getHiddenBefore();
                comment != null; comment = comment.getHiddenBefore())
            {
                switch (comment.getType())
                {
                    case JavaTokenTypes.SL_COMMENT :
                    case JavaTokenTypes.SPECIAL_COMMENT :
                    case JavaTokenTypes.SEPARATOR_COMMENT :

                        if (smartMode && (count < smartModeLines))
                        {
                            removeFooterComment(comment, eofNode);
                        }

                        count++;

                        break;

                    default :

                        for (int j = 0; j < keys.length; j++)
                        {
                            if (comment.getText().indexOf(keys[j]) > -1)
                            {
                                removeFooterComment(comment, eofNode);
                            }
                        }

                        break;
                }
            }
        }
    }


    /**
     * Removes the given footer comment from the given node.
     *
     * @param comment a comment token.
     * @param node a tree node.
     */
    private void removeFooterComment(
        CommonHiddenStreamToken comment,
        JavaNode                node)
    {
        CommonHiddenStreamToken before = comment.getHiddenBefore();
        CommonHiddenStreamToken after = comment.getHiddenAfter();

        if (after != null)
        {
            ((ExtendedToken)after).setHiddenBefore(before);

            if (before != null)
            {
                ((ExtendedToken)before).setHiddenAfter(after);
            }
            else
            {
                // we've just removed the first comment after the RCURLY so add
                // the following as the new starting one
                node.setHiddenBefore(after);
            }
        }
        else if ((before != null) && (comment != node.getHiddenAfter()))
        {
            ((ExtendedToken)before).setHiddenAfter(after);

            if (after != null)
            {
                ((ExtendedToken)after).setHiddenBefore(before);
            }
        }
        else
        {
            // it was the first comment
            node.setHiddenBefore(null);
        }
        //((ExtendedToken)comment).setHiddenBefore(null);
    }
    
    /**
     * Checks if the footer exists.
     * @param root the root node of the tree.
     * @return true if the Footer comment was found, false if it was not
     */
    private boolean footerExists(AST root) 
    {
        //This could be enhanced with checking if "footer keys" are here.
        JavaNode eofNode = getLastElement(root);
        return eofNode.hasCommentsBefore();
    }
    
    /**
     * Checks if the header exists.
     * @param node the root node of the tree.
     * @return true if the Header comment was found, false if it was not
     */
    private boolean headerExists(AST node) 
    {
        //This could be enhanced with checking if "header keys" are here.
        JavaNode first = (JavaNode) node.getFirstChild();
        return first.hasCommentsBefore();       
    }


    /**
     * Removes the header. This method removes both our 'magic' header and the user
     * header, if any.
     *
     * @param node the root node of the tree.
     *
     * @return true if the Header comment was removed, false if it was not
     * @since 1.0b8
     */
    private void removeHeader(AST node, boolean useCommentHistory)
    {
        JavaNode first = (JavaNode) node.getFirstChild();
        String[] keys = getConventionKeys(ConventionKeys.HEADER_KEYS);
        int smartModeLines =
            AbstractPrinter.settings.getInt(
                ConventionKeys.HEADER_SMART_MODE_LINES,
                ConventionDefaults.HEADER_SMART_MODE_LINES);
        boolean smartMode = (smartModeLines > 0);

        int count = 0;

        for (
            CommonHiddenStreamToken token = first.getHiddenBefore(); token != null;
            token = token.getHiddenBefore())
        {
            if (token.getHiddenBefore() == null)
            {
                for (
                    CommonHiddenStreamToken comment = token;
                    (comment != null) && (count <= smartModeLines);
                    comment = comment.getHiddenAfter())
                {
                    switch (comment.getType())
                    {
                        case JavaTokenTypes.ML_COMMENT :
                        case JavaTokenTypes.JAVADOC_COMMENT :

                            for (int j = 0; j < keys.length; j++)
                            {
                                if (comment.getText().indexOf(keys[j]) > -1)
                                {
                                    removeHeaderComment(comment, first);
                                }
                            }

                            break;

                        case JavaTokenTypes.SL_COMMENT :

                            /**
                             * TODO this isn't really fool-proof?
                             */
                            if (comment.getText().indexOf('%') > -1)
                            {
                                removeHeaderComment(comment, first);
                            }
                            else if (smartMode && (count < smartModeLines))
                            {
                                removeHeaderComment(comment, first);
                            }

                            break;
                    }

                    count++;
                }

                break;
            }
        }
    }


    /**
     * Removes the given header comment from the given node.
     *
     * @param comment a comment token.
     * @param node a tree node.
     */
    private void removeHeaderComment(
        CommonHiddenStreamToken comment,
        JavaNode                node)
    {
        CommonHiddenStreamToken before = comment.getHiddenBefore();
        CommonHiddenStreamToken after = comment.getHiddenAfter();

        if (after != null)
        {
            ((ExtendedToken)after).setHiddenBefore(before);

            if (before != null)
            {
                ((ExtendedToken)before).setHiddenAfter(after);
            }
        }
        else if (before != null)
        {
            ((ExtendedToken)before).setHiddenAfter(after);

            if (after != null)
            {
                ((ExtendedToken)after).setHiddenBefore(before);
            }
        }
        else
        {
            // it was the first comment
            if (comment == node.getHiddenBefore())
            {
                node.setHiddenBefore(null);
            }
        }
    }
}
