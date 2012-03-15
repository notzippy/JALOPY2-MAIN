/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language.antlr;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.Token;
import antlr.collections.AST;
import de.hunsicker.jalopy.language.CompositeFactory;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.Loggers;


/**
 * Central facility to create the nodes for Java parse trees.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public class JavaNodeFactory
    extends ASTFactory
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String EMPTY_STRING = "" /* NOI18N */.intern();
    private final CompositeFactory _compositeFactory;
    
    private class JavaNodeImpl extends JavaNode {

        public JavaNodeImpl() {
            super();
        }
        
    }

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new JavaNodeFactory object.
     */
    public JavaNodeFactory(CompositeFactory compositeFactory)
    {
        this._compositeFactory = compositeFactory;
        this.theASTNodeType = "JavaNode" /* NOI18N */;
        this.theASTNodeTypeClass = JavaNode.class;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Add the given node as a child to the given root.
     *
     * @param currentAST root pair.
     * @param child new child to add.
     * TODO Examine what this does ?
     */
    
    public void addASTChild(
        ASTPair currentAST,
        AST     child)
    {
        if (child != null)
        {
            JavaNode newChild = (JavaNode) child;

            if (currentAST.root == null)
            {
                // make new child the current root
                currentAST.root = newChild;
            }
            else
            {
                JavaNode root = (JavaNode) currentAST.root;

                if (
                    (root.getType() == JavaTokenTypes.EXPR)
                    && (newChild.getType() == JavaTokenTypes.EXPR))
                {
                    ;
                }
                else if (newChild.isPositionKnown())
                {
                    root.endLine = newChild.endLine;
                    root.endColumn = newChild.endColumn;
                }

                // add new child to current root
                if (currentAST.child == null)
                {
                    currentAST.root.setFirstChild(newChild);
                    newChild.parent = root;
                    newChild.prevSibling = root;
                }
                else
                {
                    currentAST.child.setNextSibling(newChild);
                    newChild.parent = root;
                    newChild.prevSibling = (JavaNode) currentAST.child;
                }

                // update the parent link for all siblings
                for (
                    JavaNode sibling = (JavaNode) newChild.getNextSibling();
                    sibling != null; sibling = (JavaNode) sibling.getNextSibling())
                {
                    sibling.parent = root;
                }
            }

            currentAST.child = child;
            currentAST.advanceChildToEnd();
        }
    }
    /**
     * Creates a new JavaNode node.
     *
     * @param type information to setup the node with.
     *
     * @return newly created Node.
     */
    public AST create(int type)
    {
        JavaNode node = (JavaNode) _compositeFactory.getCached(JavaNodeFactory.class);
        if (node==null) {
            node = new JavaNodeImpl();
            _compositeFactory.addCached(JavaNodeFactory.class,node);
        }
        node.initialize(type, EMPTY_STRING);

        return node;
    }


    /**
     * Creates a new JavaNode node.
     *
     * @param node node to setup the new node with.
     *
     * @return newly created Node.
     */
    public AST create(AST node)
    {
        if (node == null)
        {
            return null;
        }

        JavaNode result = (JavaNode) create();
        result.initialize(node);

        return result;
    }


    /**
     * Creates a new JavaNode node.
     *
     * @param token token to setup the new node with.
     *
     * @return newly created Node.
     */
    public AST create(Token token)
    {
        if (token == null)
        {
            return null;
        }

        JavaNode result = (JavaNode) create();
        result.initialize(token);

        return result;
    }


    /**
     * Creates a new JavaNode node.
     *
     * @param type type information to setup the node with.
     * @param text text to setup the node with.
     *
     * @return newly created Node.
     */
    public AST create(
        int    type,
        String text)
    {
        JavaNode result = (JavaNode) create();
        result.initialize(type, text);

        return result;
    }


    /**
     * Duplicate the given tree (including all siblings of root).
     *
     * @param t the root node of the tree (or tree portion)
     *
     * @return the copy of the tree.
     */
    public AST dupList(AST t)
    {
        JavaNode result = (JavaNode) dupTree(t); // if t == null, then result==null

        if (result != null)
        {
            JavaNode node = (JavaNode) t;
            result.parent = node.parent;
            result.prevSibling = node.parent;
        }

        JavaNode nt = result;

        while (t != null) // for each sibling of the root
        {
            t = t.getNextSibling();

            JavaNode next = (JavaNode) dupTree(t);
            nt.setNextSibling(next); // dup each subtree, building new tree

            if (next != null)
            {
                next.prevSibling = nt;
                next.parent = result.parent;
            }

            nt = (JavaNode) nt.getNextSibling();
        }

        return result;
    }
    
    /** Copy a single node with same Java AST objec type.
     *  Ignore the tokenType->Class mapping since you know
     *  the type of the node, t.getClass(), and doing a dup.
     *
     *  clone() is not used because we want all AST creation
     *  to go thru the factory so creation can be
     *  tracked.  Returns null if t is null.
     */
    public AST dup(AST t) {
        if ( t==null ) {
            return null;
        }
        AST dup_t = create();
        dup_t.initialize(t);
        return dup_t;
    }


    /**
     * Duplicate a tree, assuming this is a root node of a tree -- duplicates that node
     * and what's below; ignore siblings of root node.
     *
     * @param t the root node of the tree (or tree portion)
     *
     * @return the copy of the tree.
     */
    public AST dupTree(AST t)
    {
        
        JavaNode result = (JavaNode) dup(t); // make copy of root

        // copy all children of root.
        if (t != null)
        {
            JavaNode child = (JavaNode) t.getFirstChild();

            if (child != null)
            {
                child.parent = result;
                child.prevSibling = result;
                result.setFirstChild(dupList(child));
            }
        }

        return result;
    }


    /**
     * Logs the given error message.
     *
     * @param message an error message.
     */
    public void error(String message)
    {
        Loggers.PARSER.error(message);
    }


    /**
     * Makes a tree from a list of nodes. The first element in the array is the root. If
     * the root is <code>null</code>, then the tree is actually a simple list not a
     * tree. Handles <code>null</code> children nodes correctly. For example,
     * <code>build(a, b,  null, c)</code> yields <code>tree (a b c)</code>.
     * <code>build(null,a,b)</code> yields tree <code>(nil a b)</code>.
     * 
     * <p>
     * Sets also the line/column info of the root node.
     * </p>
     *
     * @param nodes the nodes to create the tree with.
     * TODO Examine what this does
     * @return the generated tree.
     */
    
    public AST make(AST[] nodes)
    {
        if ((nodes == null) || (nodes.length == 0))
        {
            return null;
        }

        JavaNode root = (JavaNode) nodes[0];

        if (root != null)
        {
            // don't leave any old pointers set
            root.setFirstChild(null);
        }

        // was the position info set
        boolean set = root.isPositionKnown();
        JavaNode tail = null;

        // link in children
        for (int i = 1; i < nodes.length; i++)
        {
            if (nodes[i] == null)
            {
                continue;
            }

            JavaNode first = (JavaNode) nodes[i];

            if (root == null)
            {
                // set the root and set it up for a flat list
                root = tail = (JavaNode) nodes[i];
            }
            else if (tail == null)
            {
                root.setFirstChild(nodes[i]);
                tail = first;
                tail.parent = root;
                tail.prevSibling = root;
            }
            else
            {
                tail.setNextSibling(nodes[i]);

                JavaNode tmp = tail;
                tail = first;
                tail.parent = root;
                tail.prevSibling = tmp;
            }

            // set the root position from the first child were the position
            // is known
            if (!set && first.isPositionKnown())
            {
                // make sure the first node holds the first token
                first = getFirstNode(first);
                root.startLine = first.startLine;
                root.startColumn = first.startColumn;
                root.endLine = first.endLine;
                root.endColumn = first.endColumn;
                set = true;
            }

            // chase tail to last sibling
            while (tail.getNextSibling() != null)
            {
                JavaNode prevSibling = tail;
                tail = (JavaNode) tail.getNextSibling();
                tail.parent = root;
                tail.prevSibling = prevSibling;
            }
        }

        if (root.isPositionKnown() && tail!=null)
        {
            if (tail.isPositionKnown())
            {
                root.endLine = tail.endLine;
                root.endColumn = tail.endColumn;
            }
            else
            {
                tail.startLine = root.startLine;
                tail.startColumn = root.startColumn + 1;
                tail.endLine = root.endLine;
                tail.endColumn = root.endColumn - 1;
            }
        }

        return root;
    }


    /**
     * DOCUMENT ME!
     *
     * @param currentAST DOCUMENT ME!
     * @param root DOCUMENT ME!
     */
    
    public void makeASTRoot(
        ASTPair currentAST,
        AST     root)
    {
        if (root != null)
        {
            // Add the current root as a child of new root
            root.addChild(currentAST.root);

            // The new current child is the last sibling of the old root
            currentAST.child = currentAST.root;
            currentAST.advanceChildToEnd();

            // update the parent link for all siblings
            for (
                JavaNode sibling = (JavaNode) currentAST.root; sibling != null;
                sibling = (JavaNode) sibling.getNextSibling())
            {
                sibling.parent = (JavaNode) root;
            }

            // Set the new root
            currentAST.root = root;
        }
    }


    /**
     * Returns the node which holds the actual first token (as found in the input
     * source).  For dotted AST portions that means we have to link into the children to
     * find the correct one (which is the last in the AST portion).
     *
     * @param node node to search for the actual first node.
     *
     * @return the actual first node (normally <em>node</em> is returned).
     */
    private JavaNode getFirstNode(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.DOT :
            case JavaTokenTypes.METHOD_CALL :
                return getFirstNode(node.getFirstChild());
        }

        return (JavaNode) node;
    }

    public JavaNode create(
        int startLine,
        int startColumn,
        int endLine,
        int endColumn) {
        JavaNode node = (JavaNode)create();
        node.startLine=startLine;
        node.startColumn=startColumn;
        node.endLine=endLine;
        node.endColumn=endColumn;        
        return node;
    }
    
}
