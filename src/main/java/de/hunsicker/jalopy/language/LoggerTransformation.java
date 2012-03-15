/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.util.ArrayList;
import java.util.List;

import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaNodeFactory;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;

import antlr.ASTFactory;
import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;


/**
 * Transformation which checks debug logging statements and adds an enclosing boolean
 * expression if not yet present.
 * 
 * <p>
 * The current implementation only works for the Jakarta Log4J logging toolkit and is
 * somewhat weak in that every method call with a name of &quot;debug&quot; will be
 * recognized as a logging call.
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class LoggerTransformation
    extends TreeWalker
    implements Transformation
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String DEBUG = "debug";
    private static final String LEVEL_DEBUG = "Level.DEBUG";
    private static final String LOCALIZED_LOG = "l7dlog";
    private static final String PRIORITY_DEBUG = "Priority.DEBUG";

    //~ Instance variables ---------------------------------------------------------------

    private List _calls = new ArrayList(50); // List of <JavaNode>
    private JavaNodeFactory _factory;

    public LoggerTransformation(JavaNodeFactory factory) {
        _factory = factory;
    }
    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void apply(AST tree)
      throws TransformationException
    {
        try
        {
            walk(tree);

            for (int i = 0, size = _calls.size(); i < size; i++)
            {
                AST node = (AST) _calls.get(i);
                AST name = node.getFirstChild();

                switch (name.getType())
                {
                    case JavaTokenTypes.DOT :

                        AST firstPart = name.getFirstChild();
                        AST lastPart = firstPart.getNextSibling();
                        String methodName = lastPart.getText();

                        if (isDebugCall(node, methodName))
                        {
                            JavaNode expr = ((JavaNode) node).getParent();

                            if (!isEnclosed(expr,firstPart))
                            {
                                addConditional(expr, firstPart);
                            }
                        }

                        break;
                }
            }
        }
        finally
        {
            _calls.clear();
        }
    }


    /**
     * {@inheritDoc}
     */
    public void visit(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.METHOD_CALL :
                // we can't apply the transformation during the tree
                // walking, because we alter the nodes in a way that
                // affects the walk so we simply store all method call
                // nodes to defer the actual work after the walk has
                // finished
                _calls.add(node);

                break;
        }
    }


    /**
     * Determines whether the given node represents a debug call.
     *
     * @param node METHOD_CALL node to check.
     * @param name name of the method call.
     *
     * @return <code>true</code> if the given node represents a method call.
     */
    private boolean isDebugCall(
        AST    node,
        String name)
    {
        /**
         * @todo we need a Symbol Table to query the type of the ref var
         */
        if (DEBUG.equals(name))
        {
            JavaNode n = (JavaNode) node;

            switch (n.getType())
            {
                case JavaTokenTypes.ASSIGN :
                case JavaTokenTypes.EXPR :
                    return false;
            }

            AST params = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.ELIST);
            AST expr = params.getFirstChild();

            // we need at least one parameter
            if (expr == null)
            {
                return false;
            }

            return true;
        }
        else if (LOCALIZED_LOG.equals(name))
        {
            AST params = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.ELIST);
            AST expr = params.getFirstChild();

            if (expr != null)
            {
                AST param = expr.getFirstChild();

                switch (param.getType())
                {
                    case JavaTokenTypes.DOT :

                        String n = JavaNodeHelper.getDottedName(param);

                        if (LEVEL_DEBUG.equals(n) || PRIORITY_DEBUG.equals(n))
                        {
                            return true;
                        }

                        break;
                }
            }
        }

        return false;
    }


    /**
     * Determines whether the given expression (denoting a method call) is enclosed with
     * a conditional expression.
     *
     * @param expr EXPR node to check.
     *
     * @return <code>true</code> if an enclosing conditional expression could be found.
     */
    private boolean isEnclosed(JavaNode expr, AST loggerName)
    {
        JavaNode parent = expr.getParent();

        switch (parent.getType())
        {
            case JavaTokenTypes.LITERAL_if :
                // Pull out expression list and check to see if logger variable
                // exists in expression list
                return hasLogger(parent,loggerName);
                

            case JavaTokenTypes.SLIST :
                return isEnclosed(parent, loggerName);

            default :
                return false;
        }
    }
    private boolean hasLogger(AST ifExpression, AST logger) {
        boolean result = false;
        boolean qe = false;
        AST child = ifExpression.getFirstChild();
        if (child!=null)
        for(AST next = child;next!=null && !result && !qe;) {
            switch (next.getType()) {
                case JavaTokenTypes.IDENT :
                    result = next.getText().equals(logger.getText());
                    break;
                case JavaTokenTypes.RPAREN:
                    qe = true;
                    break;
                default :
                    result = hasLogger(next,logger);
                    break;
                
                
            }
            
            next = next.getNextSibling();
        }
        return result;
    }


    /**
     * Adds a conditional expression for the given debug method call.
     *
     * @param expr our method call node to add conditional for.
     * @param name name of the debug method call.
     */
    private void addConditional(
        JavaNode expr,
        AST      name)
    {
        JavaNode cond = createConditional(name);
        JavaNode parent = expr.getParent();
        JavaNode prev = expr.getPreviousSibling();
        JavaNode next = (JavaNode) expr.getNextSibling();
        cond.setParent(parent);
        cond.setPreviousSibling(prev);

        if (parent == prev)
        {
            prev.setFirstChild(cond);
        }
        else
        {
            prev.setNextSibling(cond);
        }

        JavaNode lparen =
            (JavaNode) cond.getFirstChild().getNextSibling().getNextSibling();
        lparen.setNextSibling(expr);
        expr.setParent(cond);
        expr.setPreviousSibling(lparen);
        expr.setNextSibling(null);

        if (next != null)
        {
            cond.setNextSibling(next);
            next.setPreviousSibling(cond);
        }
    }


    /**
     * Creates the enclosing conditional expression.
     *
     * @param name name name of the debug method call.
     *
     * @return enclosing expression.
     */
    private JavaNode createConditional(AST name)
    {
        JavaNode qualifiedName = (JavaNode) _factory.create(JavaTokenTypes.DOT);
        qualifiedName.addChild(_factory.dupTree(name));
        ((JavaNode)qualifiedName.getFirstChild()).setHiddenBefore(null);
        ((JavaNode)qualifiedName.getFirstChild()).setHiddenAfter(null);

        /**
         * @todo make name configurable
         */
        AST methodName = _factory.create(JavaTokenTypes.IDENT, "isDebugEnabled");
        qualifiedName.addChild(methodName);

        AST methodCall = _factory.create(JavaTokenTypes.METHOD_CALL);
        methodCall.addChild(qualifiedName);
        methodCall.addChild(_factory.create(JavaTokenTypes.ELIST));
        methodCall.addChild(_factory.create(JavaTokenTypes.RPAREN));

        AST expr = _factory.create(JavaTokenTypes.EXPR);
        expr.addChild(methodCall);

        JavaNode ifNode = (JavaNode) _factory.create(JavaTokenTypes.LITERAL_if);
        ifNode.addChild(_factory.create(JavaTokenTypes.LPAREN));
        ifNode.addChild(expr);
        ifNode.addChild(_factory.create(JavaTokenTypes.RPAREN));
        
        return ifNode;
    }
}
