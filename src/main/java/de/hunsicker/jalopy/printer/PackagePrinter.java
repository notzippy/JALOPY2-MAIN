/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for package declarations (<code>PACKAGE_DEF</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
final class PackagePrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new PackagePrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new PackagePrinter object.
     */
    protected PackagePrinter()
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
        logIssues(node, out);
        printCommentsBefore(node, NodeWriter.NEWLINE_NO, out);

        AST annotations = node.getFirstChild();
        PrinterFactory.create(annotations, out).print(annotations, out);
        
        out.print(PACKAGE_SPACE, JavaTokenTypes.LITERAL_package);

        AST identifier = annotations.getNextSibling();
        PrinterFactory.create(identifier, out).print(identifier, out);

        AST semi = identifier.getNextSibling();
        PrinterFactory.create(semi, out).print(semi, out);

        out.printBlankLines(
            AbstractPrinter.settings.getInt(
                ConventionKeys.BLANK_LINES_AFTER_PACKAGE,
                ConventionDefaults.BLANK_LINES_AFTER_PACKAGE));
        out.last = JavaTokenTypes.PACKAGE_DEF;
    }
}
