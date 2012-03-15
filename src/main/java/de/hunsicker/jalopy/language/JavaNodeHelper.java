/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.*;

/**
 * Some common helpers for dealing with the nodes of a Java AST.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public final class JavaNodeHelper
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new JavaNodeHelper object.
     */
    private JavaNodeHelper()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Determines whether the given node represents an abstract method.
     *
     * @param node a METHOD_DEF node.
     *
     * @return <code>true</code> if the given node represents an abstract method.
     *
     * @throws IllegalArgumentException if <code><em>node</em>.getType() !=
     *         METHOD_DEF</code>
     */
    public static boolean isAbstractMethod(AST node)
    {
        boolean result = false;

        switch (node.getType())
        {
            case JavaTokenTypes.METHOD_DEF :

                for (
                    AST child = node.getFirstChild(); child != null;
                    child = child.getNextSibling())
                {
                    switch (child.getType())
                    {
                        case JavaTokenTypes.SEMI :
                            result = true;

                            break;
                    }
                }

                break;

            default :
                throw new IllegalArgumentException("no METHOD_DEF -- " + node);
        }

        return result;
    }


    /**
     * Determines wether the given node represents an anonymous inner class or interface.
     *
     * @param node a CLASS_DEF or INTERFACE_DEF node.
     *
     * @return <code>true</code> if the given node represents an anonymous inner class.
     *
     * @throws IllegalArgumentException if <code><em>node</em>.getType() != CLASS_DEF ||
     *         INTERFACE_DEF</code>
     *
     * @since 1.0b8
     */
    public static boolean isAnonymousInnerClass(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.CLASS_DEF :
            case JavaTokenTypes.INTERFACE_DEF :
                return (((JavaNode) node).getParent().getParent().getType() == JavaTokenTypes.LITERAL_new);

            default :
                throw new IllegalArgumentException("invalid node -- " + node);
        }
    }


    /**
     * Determines whether the given node will be followed by a block.
     *
     * @param node a node.
     *
     * @return <code>true</code> if the following node marks the start of a block. Note
     *         that if <code>node == null</code> this method always returns
     *         <code>false</code>.
     *
     * @since 1.0b8
     */
    public static boolean isBlockNext(AST node)
    {
        if (node == null)
        {
            return false;
        }

        AST next = node.getNextSibling();

        if (next == null)
        {
            JavaNode parent = ((JavaNode) node).getParent();

            switch (parent.getType())
            {
                case JavaTokenTypes.MODIFIERS :
                case JavaTokenTypes.TYPE :
                    return false;

                case JavaTokenTypes.EXTENDS_CLAUSE :

                    AST implementsClause = parent.getNextSibling();

                    if (implementsClause.getFirstChild() == null)
                    {
                        return isBlockNext(implementsClause);
                    }
                        return false;

                case JavaTokenTypes.IMPLEMENTS_CLAUSE :
                    return isBlockNext(parent);

                /*case JavaTokenTypes.SLIST:
                switch (parent.getParent().getType())
                {
                case JavaTokenTypes.LITERAL_try:
                case JavaTokenTypes.LITERAL_catch:
                return parent.getParent().getNextSibling() != null;
                }
                // fall through*/
                default :
                    return false;
            }
        }

        switch (next.getType())
        {
            case JavaTokenTypes.SLIST :
            case JavaTokenTypes.CASESLIST :
            case JavaTokenTypes.OBJBLOCK :
            case JavaTokenTypes.LCURLY :
            case JavaTokenTypes.ASSIGN :
                return true;
        }

        return false;
    }


    /**
     * Determines whether the given node is part of a chain.
     *
     * @param node the first child of a chain.
     *
     * @return <code>true</code> if the given node is part of a chain.
     *
     * @since 1.0b8
     */
    public static boolean isChained(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.DOT :

                AST next = node.getFirstChild();

                switch (next.getType())
                {
                    case JavaTokenTypes.METHOD_CALL :
                        //case JavaTokenTypes.INDEX_OP :
                        return true;
                }

                break;
        }

        return false;
    }


    /**
     * Builds the dotted name string representation of the object contained within the
     * JavaNode.
     *
     * @param tree the JavaNode containing the entire hierarchy of the object.
     *
     * @return string.
     */
    public static String getDottedName(AST tree)
    {
        String result = null;

        switch (tree.getType())
        {
            case JavaTokenTypes.DOT :

                AST left = tree.getFirstChild();
                AST right = left.getNextSibling();
                result = getDottedName(left) + '.' + getDottedName(right);

                break;

            case JavaTokenTypes.ARRAY_DECLARATOR :

                StringBuffer buf = new StringBuffer(30);
                left = tree.getFirstChild();
                right = left.getNextSibling();
                buf.append(getDottedName(left));

                if (right != null)
                {
                    buf.append('.');
                    buf.append(getDottedName(right));
                }

                buf.append(" []");
                result = buf.toString();

                break;

            case JavaTokenTypes.METHOD_CALL :
                result = getDottedName(tree.getFirstChild()) + "()";

                break;

            default :
                result = tree.getText();

                break;
        }

        return result;
    }


    /**
     * Determines whether the given node, which represents a block, is empty.
     *
     * @param node SLIST or OBJBLOCK.
     *
     * @return <code>true</code> if the given node is empty, i.e. contains no siblings.
     *
     * @throws IllegalArgumentException if the given node is not of type SLIST, CASESLIST
     *         or OBJBLOCK.
     */
    public static boolean isEmptyBlock(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.SLIST :
            case JavaTokenTypes.CASESLIST :
            case JavaTokenTypes.OBJBLOCK :
            case JavaTokenTypes.ANNOTATION_ARRAY_INIT :
            case JavaTokenTypes.ANNOTATION :
                break;

            default :
                throw new IllegalArgumentException("invalid type -- " + node);
        }

        boolean result = false;
WALK: 
        for (
            JavaNode child = (JavaNode) node.getFirstChild(); child != null;
            child = (JavaNode) child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.SLIST :
                case JavaTokenTypes.CASESLIST :
                    result = isEmptyBlock(child);

                    break WALK;

                case JavaTokenTypes.LCURLY :

                    if (child.hasCommentsBefore() || child.hasCommentsAfter())
                    {
                        return false;
                    }
                        // continue with the next sibling
                        continue WALK;

                case JavaTokenTypes.RCURLY :
                    return !child.hasCommentsBefore();

                default :

                    return child == null;
            }
        }

        return result;
    }


    /**
     * Determines the first chain link in the given chain. The first chain node is
     * actually the deepest child node in the AST portion!
     *
     * @param node the last chain link in the node chain.
     *
     * @return the first node in the chain.
     *
     * @see #isChained
     * @since 1.0b8
     */
    public static AST getFirstChainLink(AST node)
    {
        AST dot = node.getFirstChild();

        if (isChained(dot))
        {
            return getFirstChainLink(dot.getFirstChild());
        }

        return node;
    }


    /**
     * Return the first occurrence of the child node with the given type, if any.
     *
     * @param node node to search.
     * @param type type to search for.
     *
     * @return first child node with the given type. Returns <code>null</code> if no
     *         child node with the given type exists.
     */
    public static AST getFirstChild(
        AST node,
        int type)
    {
        AST result = null;

        for (AST child = node.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            if (child.getType() == type)
            {
                result = child;

                break;
            }
        }

        return result;
    }


    /**
     * Determines whether the given node represents a freestanding block. A freestanding
     * block is a block without associated block statement.
     *
     * @param node a node representing a block.
     *
     * @return <code>true</code> if <em>node</em> represents a freestanding block.
     *
     * @since 1.0b10
     */
    public static boolean isFreestandingBlock(JavaNode node)
    {
        boolean result = false;

        switch (node.getParent().getType())
        {
            case JavaTokenTypes.SLIST :
            case JavaTokenTypes.INSTANCE_INIT :
            case JavaTokenTypes.ANNOTATION:
                result = true;

                break;
        }

        return result;
    }


    /**
     * Determines whether the given node represents an inner class or interface.
     *
     * @param node a CLASS_DEF or INTERFACE_DEF node.
     *
     * @return <code>true</code> if the node represents an inner class or interface.
     *
     * @throws IllegalArgumentException if <code><em>node</em>.getType() != CLASS_DEF ||
     *         INTERFACE_DEF</code>
     *
     * @since 1.0b8
     */
    public static boolean isInnerClass(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.CLASS_DEF :
            case JavaTokenTypes.INTERFACE_DEF :
                return ((JavaNode) node).getParent().getType() == JavaTokenTypes.OBJBLOCK;

            default :
                throw new IllegalArgumentException("invalid node -- " + node);
        }
    }


    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @since 1.0b8
     */
    public static JavaNode getLastChild(AST node)
    {
        if (node == null)
        {
            return null;
        }

        switch (node.getType())
        {
            case JavaTokenTypes.DOT :
            {
                AST last = node.getFirstChild();
SEARCH: 
                for (AST child = last; child != null; child = child.getFirstChild())
                {
                    switch (child.getType())
                    {
                        case JavaTokenTypes.DOT :
                            last = child;

                        default :
                            break SEARCH;
                    }
                }

                return getLastChild(last.getNextSibling());
            }

            default :

                AST first = node.getFirstChild();

                if (first == null)
                {
                    return (JavaNode) node;
                }
                    AST second = first.getNextSibling();

                    if (second == null)
                    {
                        return (JavaNode) first;
                    }
                        AST last = second;

                        for (
                            AST child = second; child != null;
                            child = child.getNextSibling())
                        {
                            last = child;
                        }

                        return getLastChild(last);
        }
    }


    /**
     * Determines whether the given node represents a local variable.
     *
     * @param node a VARIABLE_DEF node.
     *
     * @return <code>true</code> if the given node represents a local variable.
     *
     * @throws IllegalArgumentException if <code><em>node</em>.getType() !=
     *         JavaTokenTypes.VARIABLE_DEF</code>.
     */
    public static boolean isLocalVariable(AST node)
    {
        if (node.getType() != JavaTokenTypes.VARIABLE_DEF)
        {
            throw new IllegalArgumentException("no VARIABLE_DEF node -- " + node);
        }

        return ((JavaNode) node).getParent().getType() != JavaTokenTypes.OBJBLOCK;
    }


    /**
     * Advance to the first non-LPAREN node for the rare case where both operands are
     * enclosed by several parenthesis groups, e.g. ((LA(4) >= '\u0003')), so we
     * actually skip unnecessary parentheses.
     *
     * @param lparen the first LPAREN child of an expression node.
     *
     * @return the first NON parentheses node or <code>null</code> if no such node
     *         exists.
     *
     * @throws NullPointerException if <code><em>lparen</em> == null</code>
     * @throws IllegalArgumentException if <code><em>lparen</em>.getType() !=
     *         JavaTokenTypes.LPAREN</code>
     */
    public static AST advanceToFirstNonParen(AST lparen)
    {
        if (lparen == null)
        {
            throw new NullPointerException();
        }

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
}
