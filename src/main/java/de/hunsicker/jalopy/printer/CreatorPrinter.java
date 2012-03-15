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


/**
 * Printer for creator constructs [<code>LITERAL_new</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class CreatorPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new CreatorPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new CreatorPrinter object.
     */
    private CreatorPrinter()
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

        out.print(NEW_SPACE, JavaTokenTypes.LITERAL_new);

        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.OBJBLOCK :
                    out.state.anonymousInnerClass = true;

                    JavaNode n = (JavaNode) node;

                    switch (n.getParent().getParent().getType())
                    {
                        // if the creator starts an anonymous inner class and
                        // is part of another creator or method call, we have
                        // to adjust the parentheses nesting level because the
                        // indentation printing depends on this setting and
                        // would indent one level to much if left alone
                        case JavaTokenTypes.ELIST :
                            out.indent();
                            out.state.paramLevel--;
                            PrinterFactory.create(child, out).print(child, out);
                            out.unindent();
                            out.state.paramLevel++;

                            break;

                        case JavaTokenTypes.ASSIGN :
                            PrinterFactory.create(child, out).print(child, out);

                            break;

                        default :
                            out.indent();
                            PrinterFactory.create(child, out).print(child, out);
                            out.unindent();

                            break;
                    }

                    out.state.anonymousInnerClass = false;

                    // hint for correct blank lines behaviour
                    out.last = JavaTokenTypes.CLASS_DEF;

                    break;

                default :
                    PrinterFactory.create(child, out).print(child, out);

                    break;
            }
        }
    }
}
