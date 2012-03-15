/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import antlr.collections.AST;


/**
 * Helper class to make implementing tree walkers easy.
 * 
 * <p>
 * To implement a class that walks over all nodes simply dereive from TreeWalker and
 * implement the callback method {@link #visit}.
 * </p>
 * <pre class="snippet">
 * class MyWalker
 *     extends TreeWalker
 * {
 *     // called for every node
 *     public void visit(AST node)
 *     {
 *         System.out.println("node visited: " &#043; node);
 *     }
 * }
 * </pre>
 * 
 * <p>
 * If you want to control which nodes will be actually visited, overwrite {@link
 * #walkNode} too.
 * </p>
 * <pre class="snippet">
 * // visits only IMPORT nodes and quits after the last IMPORT node found
 * protected void walkNode(AST node)
 * {
 *     switch (node.getType())
 *     {
 *         case JavaTokenTypes.ROOT:
 *             // skip to next child
 *             walkNode(node.getFirstChild());
 *             break;
 * 
 *         case JavaTokenTypes.PACKAGE_DEF:
 *             // skip to next child
 *             walkNode(node.getNextSibling());
 *             break;
 * 
 *        case JavaTokenTypes.IMPORT:
 *             // only visit root node, DON'T walk over it's children
 *             visit(node);
 * 
 *             // continue with the next node
 *             walkNode(node.getNextSibling());
 *             break;
 * 
 *         case JavaTokenTypes.CLASS_DEF:
 *             // quit after the first non-IMPORT node
 *             return;
 *     }
 * }
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public abstract class TreeWalker
{
    //~ Instance variables ---------------------------------------------------------------

    /** Indicates a stop. */
    protected boolean stop;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new TreeWalker object.
     */
    protected TreeWalker()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Resets the walker.
     */
    public void reset()
    {
        this.stop = false;
    }


    /**
     * Callback method that can be called for a node found. Overwrite to perform whatever
     * action you want take place for a node.
     * 
     * <p>
     * In the default implementation, this method will be called for every node of the
     * tree.
     * </p>
     *
     * @param node a node of the tree.
     */
    public abstract void visit(AST node);


    /**
     * Starts the walking with the root node of the tree or node portion.
     *
     * @param tree the tree to walk over.
     */
    public void walk(AST tree)
    {
        walkNode(tree);
    }


    /**
     * Stops the walking. You have to reset the walker prior reusing it.
     *
     * @see #reset
     */
    protected void stop()
    {
        this.stop = true;
    }


    /**
     * Iterates over all children of the given node.
     *
     * @param node node to iterate over.
     */
    protected void walkChildren(AST node)
    {
        if (!this.stop)
        {
            for (
                AST child = node.getFirstChild(); child != null;
                child = child.getNextSibling())
            {
                walkNode(child);
            }
        }
    }


    /**
     * Walks over the given node. Links in children. Called for every single node of the
     * tree. The default implemention simply calls
     * <pre class="snippet">
     * visit(node);
     * walkChildren();
     * </pre>
     * to visit the current node and continue walking over all children of the node.
     *
     * @param node a node of the tree.
     */
    protected void walkNode(AST node)
    {
        if (!this.stop)
        {
            visit(node);
            walkChildren(node);
        }
    }
}
