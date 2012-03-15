/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import de.hunsicker.jalopy.language.antlr.Node;

import antlr.ASTFactory;
import antlr.Token;
import antlr.collections.AST;


/**
 * Central facility to create extended nodes.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class NodeFactory
    extends ASTFactory
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The empty string constant. */
    protected static final String EMPTY_STRING = "" /* NOI18N */.intern();
    private final CompositeFactory compositeFactory;
    private class NodeImpl extends Node{}
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new NodeFactory object.
     */
    public NodeFactory(CompositeFactory compositeFactory)
    {
        this.compositeFactory = compositeFactory;
        this.theASTNodeType = "Node" /* NOI18N */;
        this.theASTNodeTypeClass = Node.class;
        
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Creates a new empty Node node.
     *
     * @return newly created Node.
     */

    /**
     * Creates a new empty JavaNode node.
     *
     * @return newly created Node.
     */
    public AST create()
    {
        Node node = (Node) compositeFactory.getCached(NodeFactory.class);
        
        if (node==null) {
            node = new NodeImpl();
            compositeFactory.addCached(NodeFactory.class,node);
        }
        
        return node;
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
     * Creates a new empty Node node.
     *
     * @param type information to setup the node with.
     *
     * @return newly created Node.
     */
    public AST create(int type)
    {
        AST t = create();
        t.initialize(type, EMPTY_STRING);

        return t;
    }


    /**
     * Creates a new empty Node node.
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
        AST t = create();
        t.initialize(type, text);

        return t;
    }


    /**
     * Creates a new empty Node node.
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

        AST t = create();
        t.initialize(node);

        return t;
    }


    /**
     * Creates a new empty Node node.
     *
     * @param tok token to setup the new node with.
     *
     * @return newly created Node.
     */
    public AST create(Token tok)
    {
        AST t = create();
        t.initialize(tok);

        return t;
    }
}
