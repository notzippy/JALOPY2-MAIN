/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;
import de.hunsicker.jalopy.language.JavaNodeHelper;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.util.StringHelper;


/**
 * Printer for import declarations (<code>IMPORT</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.8 $
 */
final class ImportPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ImportPrinter();
    private static final String DELIMETER = "|" /* NOI18N */;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ImportPrinter object.
     */
    protected ImportPrinter()
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
        printCommentsBefore(node, out);
        trackPosition((JavaNode) node, out);

        out.print(IMPORT_SPACE, JavaTokenTypes.LITERAL_import);
        if (node.getType() == JavaTokenTypes.STATIC_IMPORT) {
            out.print(STATIC_SPACE, JavaTokenTypes.LITERAL_import);
        }

        AST identifier = node.getFirstChild();
        String name = JavaNodeHelper.getDottedName(identifier);
        out.print(name, JavaTokenTypes.LITERAL_import);

        AST semi = identifier.getNextSibling();
        PrinterFactory.create(semi, out).print(semi, out);

        AST next = node.getNextSibling();

        if (next != null)
        {
            switch (next.getType())
            {
                case JavaTokenTypes.IMPORT :

                    // grouping of the declarations only makes sense if
                    // sorting is enabled
                    if (
                        AbstractPrinter.settings.getBoolean(
                            ConventionKeys.IMPORT_SORT, ConventionDefaults.IMPORT_SORT))
                    {
                        String nextName =
                            JavaNodeHelper.getDottedName(next.getFirstChild());
                        int depth = getImportDepth(name);
                        int offset = StringHelper.indexOf('.', name, depth);

                        // the declaration has a package name equal/greater
                        // than the given package depth
                        if (offset > -1)
                        {
                            String nextPart = name.substring(0, offset);

                            // so only issue extra newline if next declaration
                            // starts with a different one
                            if (!nextName.startsWith(nextPart + '.'))
                            {
                                printNewline(next, out);
                            }
                        }

                        // no package name found; if the next declaration
                        // contains one, we have to issue an extra newline
                        else if (depth > 0)
                        {
                            int dots = StringHelper.occurs('.', name);

                            if (dots > 0)
                            {
                                String nextPart =
                                    name.substring(0, name.lastIndexOf('.'));

                                if (
                                    !nextName.startsWith(nextPart + '.')
                                    || (StringHelper.occurs('.', nextName) != dots))
                                {
                                    printNewline(next, out);
                                }
                            }
                            else if (nextName.indexOf('.') > -1)
                            {
                                printNewline(next, out);
                            }
                        }
                    }

                    break;
            }
        }

        out.last = JavaTokenTypes.IMPORT;
    }


    /**
     * Returns the import depth for the given import declaration. This integer indicates
     * the number of packages/subpackages that are to group together.
     *
     * @param declaration declaration to check.
     *
     * @return the import depth for the given import declaration.
     */
    private int getImportDepth(String declaration)
    {
        int defaultGroupingDepth =
            AbstractPrinter.settings.getInt(
                ConventionKeys.IMPORT_GROUPING_DEPTH,
                ConventionDefaults.IMPORT_GROUPING_DEPTH);

        // '0' means no grouping at all
        if (defaultGroupingDepth > 0)
        {
            String info =
                AbstractPrinter.settings.get(
                    ConventionKeys.IMPORT_GROUPING, ConventionDefaults.IMPORT_GROUPING);

            if (info.length() > 0)
            {
                Map values = decodeGroupingInfo(info);

                for (Iterator i = values.entrySet().iterator(); i.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) i.next();

                    if (declaration.startsWith((String) entry.getKey()))
                    {
                        return new Integer((String) entry.getValue()).intValue();
                    }
                }
            }
        }

        return defaultGroupingDepth;
    }


    private Map decodeGroupingInfo(String info)
    {
        Map result = new HashMap(15);

        for (
            StringTokenizer tokens = new StringTokenizer(info, DELIMETER);
            tokens.hasMoreElements();)
        {
            String pair = tokens.nextToken();
            int delimOffset = pair.indexOf(':');
            String name = pair.substring(0, delimOffset);
            String depth = pair.substring(delimOffset + 1);
            result.put(name, depth);
        }

        return result;
    }


    private void printNewline(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        JavaNode n = (JavaNode) node;
        CommonHiddenStreamToken comment = n.getHiddenBefore();

        if ((comment == null) || (node.getType() != JavaTokenTypes.IMPORT))
        {
            out.printNewline();
        }
        else
        {
            switch (comment.getType())
            {
                case JavaTokenTypes.SL_COMMENT :

                    if (
                        AbstractPrinter.settings.getInt(
                            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE,
                            ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE) <= 0)
                    {
                        out.printNewline();
                    }

                    break;

                case JavaTokenTypes.JAVADOC_COMMENT :

                    if (
                        AbstractPrinter.settings.getInt(
                            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_JAVADOC,
                            ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_JAVADOC) <= 0)
                    {
                        out.printNewline();
                    }

                    break;

                case JavaTokenTypes.ML_COMMENT :

                    if (
                        AbstractPrinter.settings.getInt(
                            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_MULTI_LINE,
                            ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_MULTI_LINE) <= 0)
                    {
                        out.printNewline();
                    }

                    break;

                // we always print a newline for all other comments so nothing
                // to do here
                default :
                    break;
            }
        }
    }
}
