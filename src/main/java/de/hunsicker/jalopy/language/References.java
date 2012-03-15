/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.5 $
 *
 * @since 1.0b8
 */
final class References
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Indicates a new class scope. */
    public static final int SCOPE_CLASS = 2;

    /** Indicates all scopes besides {@link #SCOPE_CLASS}. */
    public static final int SCOPE_DEFAULT = 1;

    //~ Instance variables ---------------------------------------------------------------

    /** Provides a stack of currently active scopes. */
    private final LinkedList _scopesStack = new LinkedList(); // List of <References.Scope>

    /** Holds all scopes found during the parsing. */
    private final List _scopes = new ArrayList(); // List of <References.Scope>

    /**
     * Maps all variables with their references. <code>null</code> values means there
     * exist no references for a variable.
     */
    final Map _variables = new HashMap(); // Map of <JavaNode>:<List>

    /** The default scope represents the top of a compilation unit. */
    private final Scope _defaultScope = new Scope();

    /** The current scope. */
    private Scope _curScope = _defaultScope;
    private final Set _identifiers = new HashSet(150); // Set of <String>
    private final Set _types = new HashSet(30); // Set of <String>

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new References object.
     */
    public References()
    {
        _scopesStack.addFirst(_defaultScope);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns a map with all declared variables along with their references.
     *
     * @return all declared variables (Map of &lt;JavaNode:List&gt;).
     */
    public Map getVariables()
    {
        return _variables;
    }


    /**
     * Adds a reference to the current scope.
     *
     * @param name name of the reference.
     * @param node the node of the reference.
     */
    public void addReference(
        String   name,
        JavaNode node)
    {
        _curScope.addReference(node, name);
        _identifiers.add(name);
    }


    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param node DOCUMENT ME!
     */
    public void defineType(
        String   name,
        JavaNode node)
    {
        _types.add(name);
    }


    /**
     * Defines a variable that may be referenced in the current or a subordinated scope.
     *
     * @param name the name of the variable.
     * @param node the VARIABLE_DEF node.
     */
    public void defineVariable(
        String   name,
        JavaNode node)
    {
        _curScope.addVariable(name, node);
        _variables.put(node, null);
    }


    /**
     * Enters a new scope.
     */
    public void enterScope()
    {
        enterScope(SCOPE_DEFAULT);
    }


    /**
     * Enters a new scope of the given type.
     *
     * @param type the scope type. Either <ul><li>{@link #SCOPE_DEFAULT} or</li>
     *        <li>{@link #SCOPE_CLASS}</li> </ul>.
     */
    public void enterScope(int type)
    {
        Scope scope = new Scope(_curScope, type);
        _curScope = scope;
        _scopesStack.addFirst(scope);
        _scopes.add(scope);
    }


    /**
     * Leaves the current scope.
     */
    public void leaveScope()
    {
        //Scope s = (Scope) _scopesStack.removeFirst();
        _curScope = (Scope) _scopesStack.getFirst();

        if (_curScope == _defaultScope)
        {
            //resolveReferences();
        }
    }


    /**
     * Resets the instance so it can easily be reused.
     */
    public void reset()
    {
        _variables.clear();
        _types.clear();
        _identifiers.clear();
        _scopesStack.clear();
        _scopesStack.addFirst(_defaultScope);
        _scopes.clear();

        // release all subordinate scopes to let gc happen!
        if (_defaultScope.children != null)
        {
            _defaultScope.children.clear();
        }

        _curScope = _defaultScope;
    }


    /**
     * Resolves all local references.
     */
    public void resolveReferences()
    {
        _curScope = _defaultScope;
        System.out.println("Resolving...");
        _curScope.resolveReferences();

        for (Iterator i = _variables.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();
            List references = (List) entry.getValue();
            JavaNode node = (JavaNode) entry.getKey();
            int modifierMask = JavaNodeModifier.valueOf(node);

            // constant, ignore
            if (
                Modifier.isFinal(modifierMask)
                && Modifier.isStatic(modifierMask))
            {
                continue;
            }

            if (references == null)
            {
                switch (node.getType())
                {
                    case JavaTokenTypes.VARIABLE_DEF :

                        JavaNode assign =
                            (JavaNode) JavaNodeHelper.getFirstChild(
                                node, JavaTokenTypes.ASSIGN);

                        if (assign != null)
                        {
                            System.out.println(
                                "XXX:" + node.getStartLine() + ":" + node.getStartColumn()
                                + ": Variable "
                                + JavaNodeHelper.getFirstChild(
                                    node, JavaTokenTypes.IDENT).getText()
                                + " is declared but never assigned");
                        }
                        else
                        {
                            System.out.println(
                                "XXX:" + node.getStartLine() + ":" + node.getStartColumn()
                                + ": Variable "
                                + JavaNodeHelper.getFirstChild(
                                    node, JavaTokenTypes.IDENT).getText()
                                + " is assigned but never accessed");
                        }

                        break;
                }
            }
            else
            {
                switch (node.getType())
                {
                    case JavaTokenTypes.VARIABLE_DEF :
                    {
                        int usages = references.size();

                        if (usages == 1)
                        {
                            JavaNode usage = (JavaNode) references.get(0);

                            switch (usage.getParent().getType())
                            {
                                case JavaTokenTypes.ASSIGN :

                                    switch (usage.getPreviousSibling().getType())
                                    {
                                        case JavaTokenTypes.ASSIGN :

                                            // node is lhs of assignment

                                            /**
                                             * @todo make the level configurable
                                             */
                                            if (Modifier.isPrivate(modifierMask))
                                            {
                                                System.out.println(
                                                    "XXX:" + node.getStartLine() + ":"
                                                    + node.getStartColumn() + ": Variable "
                                                    + JavaNodeHelper.getFirstChild(
                                                        node, JavaTokenTypes.IDENT)
                                                                    .getText()
                                                    + " is assigned but never accessed (assigned at "
                                                    + usage.getStartLine() + ":"
                                                    + usage.getStartColumn() + ")");
                                            }

                                            break;
                                    }

                                    break;

                                default :

                                    if (!Modifier.isFinal(modifierMask))
                                    {
                                        /**
                                         * @todo :" + node.startLine + ":" +
                                         *       node.startColumn + ": Variable " +
                                         *       JavaNodeHelper.getFirstChild(node,
                                         *       JavaTokenTypes.IDENT).getText() + " is
                                         *       only assigned once, consider making it
                                         *       final");
                                         */
                                    }

                                    break;
                            }
                        }
                        else
                        {
                            if (!Modifier.isFinal(modifierMask))
                            {
                                JavaNode assign =
                                    (JavaNode) JavaNodeHelper.getFirstChild(
                                        node, JavaTokenTypes.ASSIGN);
                                int assignments = 0;

                                if (assign != null)
                                {
                                    JavaNode expr = (JavaNode) assign.getFirstChild();

                                    switch (expr.getType())
                                    {
                                        case JavaTokenTypes.EXPR :

                                            if (
                                                !"null".equals(
                                                    expr.getFirstChild().getText()))
                                            {
                                                assignments = 1;
                                            }

                                            break;
                                    }
                                }

LOOP: 
                                for (int j = 0, size = references.size(); j < size;
                                    j++)
                                {
                                    JavaNode n = (JavaNode) references.get(j);

                                    switch (n.getParent().getType())
                                    {
                                        case JavaTokenTypes.ASSIGN :
                                            assignments++;

                                            if (assignments > 1)
                                            {
                                                break LOOP;
                                            }

                                            break;
                                    }
                                }

                                if (assignments == 1)
                                {
                                    /**
                                     * @todo :" + node.startLine + ":" + node.startColumn
                                     *       + ": Variable " +
                                     *       JavaNodeHelper.getFirstChild(node,
                                     *       JavaTokenTypes.IDENT).getText() + " is only
                                     *       assigned once, consider making it final");
                                     */
                                }
                            }
                        }
                    }
                }

                /*   System.out.println(entry.getKey());

                   List refs = (List)entry.getValue();

                   for (int j = 0, n = refs.size(); j < n; j++)
                   {
                       System.out.println("    " + refs.get(j));
                   }

                   System.out.println("------------------------------------");
                */
            }
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    private class Scope
    {
        /** The subordinated scopes. */
        List children; // List of <Scope>

        /** The references that are to be resolved. */
        Map references; // Map of <JavaNode>:<String>

        /** The variables of this scope. */
        Map variables; // Map of <String>:<JavaNode>

        /** The superordinated scope. */
        Scope parent;
        int type;

        public Scope(
            Scope parent,
            int   type)
        {
            this.type = type;

            if (parent != null)
            {
                this.parent = parent;

                if (parent.children == null)
                {
                    parent.children = new ArrayList();
                }

                parent.children.add(this);
            }
        }


        Scope()
        {
        }

        public void addReference(
            JavaNode node,
            String   name)
        {
            if (this.references == null)
            {
                this.references = new HashMap();
            }

            this.references.put(node, name);
        }


        public void addVariable(
            String   name,
            JavaNode node)
        {
            if (this.variables == null)
            {
                this.variables = new HashMap();
            }

            this.variables.put(name, node);
        }


        public void resolveReferences()
        {
            resolveVariableReferences(this);

            // resolve all references in the subordinated scopes
            if (this.children != null)
            {
                for (int i = 0, size = this.children.size(); i < size; i++)
                {
                    Scope scope = (Scope) this.children.get(i);
                    scope.resolveReferences();
                }
            }
        }


        private void resolveVariableReferences(Scope scope)
        {
            if ((this.references != null) && !this.references.isEmpty())
            {
                if ((scope.variables != null) && !scope.variables.isEmpty())
                {
LOOKUP: 
                    for (Iterator i = this.references.entrySet().iterator(); i.hasNext();)
                    {
                        Map.Entry reference = (Map.Entry) i.next();
                        String refName = (String) reference.getValue();
                        JavaNode refNode = (JavaNode) reference.getKey();
                        int dot = refName.indexOf('.');

                        if (dot == -1) // no qualification
                        {
                            switch (refNode.getType())
                            {
                                // no qualification and METHOD_CALL means not
                                // a variable reference so we can avoid further
                                // processing here
                                case JavaTokenTypes.METHOD_CALL :

                                    continue LOOKUP;
                            }
                        }
                        else
                        {
                            // does the name contains a referrer to the
                            // instance or outer class?
                            int referrer = refName.indexOf(".this.");

                            // use only the first part of the qualified name
                            // for the lookup
                            if (referrer == -1) // no referrer
                            {
                                switch (scope.type)
                                {
                                    case SCOPE_CLASS :

                                        if (refName.startsWith("this."))
                                        {
                                            int nextDot = refName.indexOf('.', 5);

                                            switch (nextDot)
                                            {
                                                case -1 : // no further dots
                                                    // strip the keyword
                                                    refName = refName.substring(5);

                                                    break;

                                                default :
                                                    // strip everything before
                                                    // first and after second dot
                                                    refName =
                                                        refName.substring(5, nextDot);

                                                    break;
                                            }

                                            break;
                                        }

                                    // fall through
                                    default : //strip everything after the first dot
                                        refName = refName.substring(0, dot);

                                        break;
                                }

                                /*if (!refName.startsWith("this."))
                                {
                                    refName = refName.substring(0, dot);
                                }
                                else if (scope.type == SCOPE_CLASS)
                                {
                                    // if we're in class scope, we can savely
                                    // strip the keyword to see whether we will
                                    // find a match
                                    refName = refName.substring(5);
                                }*/
                            }
                            else if (scope.type == SCOPE_CLASS)
                            {
                                // but for referrers we have to strip the
                                // outer class reference part
                                refName = refName.substring(referrer + 6);
                            }
                        }

                        for (
                            Iterator j = scope.variables.entrySet().iterator();
                            j.hasNext();)
                        {
                            Map.Entry variable = (Map.Entry) j.next();
                            String varName = (String) variable.getKey();
                            //Object value = variable.getValue();

                            if (refName.equals(varName))
                            {
                                JavaNode varNode = (JavaNode) variable.getValue();
                                List theReferences = (List) _variables.get(varNode);

                                if (theReferences == null)
                                {
                                    theReferences = new ArrayList();
                                    _variables.put(varNode, theReferences);
                                }

                                theReferences.add(refNode);

                                // the reference has been resolved, so remove
                                // it from the map
                                i.remove();
                            }
                        }
                    }
                }

                // search along the parent scopes as long as there are references
                if ((scope.parent != null) && !this.references.isEmpty())
                {
                    resolveVariableReferences(scope.parent);
                }
            }
        }
    }
}
