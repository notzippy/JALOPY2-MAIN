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
 * Printer for array types [<code>ARRAY_DECLARATOR</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.7 $
 */
final class ArrayTypePrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ArrayTypePrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ArrayTypePrinter object.
     */
    protected ArrayTypePrinter()
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
        AST child = node.getFirstChild();

        boolean bracketsAfterIdentifier =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.ARRAY_BRACKETS_AFTER_IDENT,
                ConventionDefaults.ARRAY_BRACKETS_AFTER_IDENT);

        if (child != null)
        {
            for (; child != null; child = child.getNextSibling())
            {
                switch (child.getType())
                {
                    case JavaTokenTypes.EXPR :

                        if (
                            AbstractPrinter.settings.getBoolean(
                                ConventionKeys.PADDING_BRACKETS,
                                ConventionDefaults.PADDING_BRACKETS))
                        {
                            out.print(BRACKET_LEFT_SPACE, JavaTokenTypes.LBRACK);
                            PrinterFactory.create(child, out).print(child, out);
                            out.print(SPACE_BRACKET_RIGHT, JavaTokenTypes.RBRACK);
                        }
                        else
                        {
                            out.print(BRACKET_LEFT, JavaTokenTypes.LBRACK);
                            PrinterFactory.create(child, out).print(child, out);
                            out.print(BRACKET_RIGHT, JavaTokenTypes.RBRACK);
                        }

                        break;

                    case JavaTokenTypes.ARRAY_DECLARATOR :
                        this.print(child, out);

                        if (child.getNextSibling() == null)
                        {
                            if (bracketsAfterIdentifier && canMoveBrackets(node))
                            {
                                out.state.arrayBrackets++;
                            }
                            else
                            {
                                out.print(BRACKETS, JavaTokenTypes.RBRACK);
                            }
                        }

                        break;

                    default :
                        PrinterFactory.create(child, out).print(child, out);

                        if (bracketsAfterIdentifier && canMoveBrackets(node))
                        {
                            out.state.arrayBrackets++;
                        }
                        else
                        {
                            if (
                                AbstractPrinter.settings.getBoolean(
                                    ConventionKeys.SPACE_BEFORE_BRACKETS_TYPES,
                                    ConventionDefaults.SPACE_BEFORE_BRACKETS_TYPES))
                            {
                                out.print(
                                    SPACE_BRACKETS, JavaTokenTypes.ARRAY_DECLARATOR);
                            }
                            else
                            {
                                out.print(BRACKETS, JavaTokenTypes.RBRACK);
                            }
                        }

                        break;
                }
            }
        }
        else // followed by an ARRAY_INIT
        {
            if (
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_BRACKETS_TYPES,
                    ConventionDefaults.SPACE_BEFORE_BRACKETS_TYPES))
            {
                out.print(SPACE_BRACKETS, JavaTokenTypes.ARRAY_DECLARATOR);
            }
            else
            {
                out.print(BRACKETS, JavaTokenTypes.RBRACK);
            }
        }

        out.last = JavaTokenTypes.ARRAY_DECLARATOR;
    }


    /**
     * Determines whether the brackets for the given array type can be moved behind the
     * identifier.
     *
     * @param node a ARRAY_DECLARATOR node.
     *
     * @return <code>true</code> if the brackets can be moved.
     *
     * @since 1.0b9
     */
    private boolean canMoveBrackets(AST node)
    {
        JavaNode parent = ((JavaNode) node).getParent();

        switch (parent.getType())
        {
            case JavaTokenTypes.TYPE :

                switch (parent.getParent().getType())
                {
                    case JavaTokenTypes.METHOD_DEF :
                    case JavaTokenTypes.TYPECAST :
                    case JavaTokenTypes.LITERAL_instanceof :
                        return false;
                }

                break;

            case JavaTokenTypes.TYPECAST :
            case JavaTokenTypes.LITERAL_new :
                return false;

            case JavaTokenTypes.ARRAY_DECLARATOR :
                return canMoveBrackets(parent);
        }

        AST next = node.getNextSibling();

        if (next != null)
        {
            switch (next.getType())
            {
                case JavaTokenTypes.LITERAL_class :
                    return false;
            }
        }

        return true;
    }
}
