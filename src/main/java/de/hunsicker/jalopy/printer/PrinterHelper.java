/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;


/**
 * Some common helpers.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class PrinterHelper
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new PrinterHelper object.
     */
    private PrinterHelper()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Advance to the first non-LPAREN node for the rare case where both operands are
     * enclosed by several parenthesis groups, e.g. ((LA(4) >= '\u0003')), so we skip
     * unnecessary parenthesis
     *
     * @param lparen the first LPAREN child of an expression node.
     *
     * @return the first non parentheses ast or <code>null</code> if no such node exists.
     *
     * @throws IllegalArgumentException if <code>lparen.getType() !=
     *         JavaTokenTypes.LPAREN</code>
     */
    public static AST advanceToFirstNonParen(AST lparen)
    {
        if (lparen.getType() != JavaTokenTypes.LPAREN)
        {
            throw new IllegalArgumentException(lparen + " no LPAREN");
        }

LOOP: 
        for (AST next = lparen.getNextSibling(); next != null;
            next = next.getNextSibling())
        {
            switch (next.getType())
            {
                case JavaTokenTypes.LPAREN :

                    continue LOOP;

                default :
                    return next;
            }
        }

        return null;
    }


    /**
     * Removes the <code>abstract</code> modifier from the given modifiers list, if
     * found.
     *
     * @param node a MODIFIERS node.
     *
     * @throws IllegalArgumentException if <code>node.getType() !=
     *         JavaTokenTypes.MODIFIERS</code>.
     */
    public static void removeAbstractModifier(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.MODIFIERS :
                break;

            default :
                throw new IllegalArgumentException(
                    "MODIFIERS node expected, was " + node);
        }

        /*for (AST modifier = node.getFirstChild();
             modifier != null;
             modifier = modifier.getNextSibling())
        {
            String name = modifier.getText();

            if ("abstract".equals(name))
            {
                JavaNode current = (JavaNode)modifier;
                JavaNode parent = current.getParent();
                JavaNode previous = current.getPreviousSibling();
                JavaNode next = (JavaNode)current.getNextSibling();

                if (parent == previous)
                {
                    parent.setFirstChild(next);
                }
                else
                {
                    previous.setNextSibling(next);
                }

                if (next != null)
                {
                    next.setPreviousSibling(previous);
                }

                current.setParent(null);
                current.setPreviousSibling(null);
                current.setFirstChild(null);
                current.setNextSibling(null);

                break;
            }
        }*/
    }


    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @since 1.0b9
     */
    static boolean isMultipleExpression(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.EXPR :

                switch (node.getFirstChild().getType())
                {
                    case JavaTokenTypes.LAND :
                    case JavaTokenTypes.LOR :
                    case JavaTokenTypes.LT :
                    case JavaTokenTypes.GT :
                    case JavaTokenTypes.LE :
                    case JavaTokenTypes.GE :
                    case JavaTokenTypes.EQUAL :
                    case JavaTokenTypes.NOT_EQUAL :
                        return isMultipleExpression(node.getFirstChild().getFirstChild());
                }

                break;

            default :

                switch (node.getType())
                {
                    case JavaTokenTypes.LAND :
                    case JavaTokenTypes.LOR :
                    case JavaTokenTypes.LT :
                    case JavaTokenTypes.GT :
                    case JavaTokenTypes.LE :
                    case JavaTokenTypes.GE :
                    case JavaTokenTypes.EQUAL :
                    case JavaTokenTypes.NOT_EQUAL :

                        JavaNode parent = ((JavaNode) node).getParent();

                        switch (parent.getType())
                        {
                            case JavaTokenTypes.LAND :
                            case JavaTokenTypes.LOR :
                            case JavaTokenTypes.LT :
                            case JavaTokenTypes.GT :
                            case JavaTokenTypes.LE :
                            case JavaTokenTypes.GE :
                            case JavaTokenTypes.EQUAL :
                            case JavaTokenTypes.NOT_EQUAL :
                                return true;
                        }
                }

                break;
        }

        return false;
    }
}
