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
 * Printer for constructor declarations (<code>CTOR_DEF</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
final class ConstructorDeclarationPrinter
    extends BasicDeclarationPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ConstructorDeclarationPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ConstructorDeclarationPrinter object.
     */
    protected ConstructorDeclarationPrinter()
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
        JavaNode n = (JavaNode) node;

        addCommentIfNeeded(n, out);

        // trigger printing of blank lines/comments
        super.print(node, out);

        AST modifiers = node.getFirstChild();
        PrinterFactory.create(modifiers, out).print(modifiers, out);

        AST identifier = modifiers.getNextSibling();
        PrinterFactory.create(identifier, out).print(identifier, out);

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_BEFORE_METHOD_DEF_PAREN,
                ConventionDefaults.SPACE_BEFORE_METHOD_DEF_PAREN))
        {
            out.print(SPACE, JavaTokenTypes.WS);
        }

        // set the marker needed by ThrowsPrinter.java
        Marker marker = out.state.markers.add(out.line, out.column);

        AST lparen = identifier.getNextSibling();
        PrinterFactory.create(lparen, out).print(lparen, out);

        AST parameters = lparen.getNextSibling();
        PrinterFactory.create(parameters, out).print(parameters, out);

        AST rparen = parameters.getNextSibling();
        PrinterFactory.create(rparen, out).print(rparen, out);

        for (
            AST child = rparen.getNextSibling(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.SLIST :
                    out.state.markers.remove(marker);

                    break;
            }

            PrinterFactory.create(child, out).print(child, out);
        }

        out.state.newlineBeforeLeftBrace = false;
        out.state.parametersWrapped = false;

        out.last = JavaTokenTypes.CTOR_DEF;
    }
}
