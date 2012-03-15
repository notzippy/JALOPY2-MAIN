/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.ExtendedToken;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Transformation which sorts the nodes of a tree according to some user configurable
 * policy.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
final class SortTransformation
    implements Transformation
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String EMPTY_STRING = "".intern() /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** Comparator for CLASS_DEF, INTERFACE_DEF, CTOR_DEF and METHOD_DEF nodes. */
    private final NodeComparator _defaultComparator = new NodeComparator();

    /** Comparator for VARIABLE_DEF nodes */
    private final VariableDefNodeComparator _variablesComparator =
        new VariableDefNodeComparator();
    
    CompositeFactory _factory = null;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new SortTransformation object.
     */
    public SortTransformation(CompositeFactory factory)
    {
        _factory = factory;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void apply(AST tree)
      throws TransformationException
    {
        Convention settings = Convention.getInstance();
        boolean sortModifiers = settings.getBoolean(
                            ConventionKeys.SORT_MODIFIERS, 
                            ConventionDefaults.SORT_MODIFIERS);
        boolean sortBeanNames=
            settings.getBoolean(ConventionKeys.SORT_METHOD_BEAN, ConventionDefaults.SORT_METHOD_BEAN);
        
        _defaultComparator.setBeanSorting(sortBeanNames);
        _defaultComparator.setModifierSorting(sortModifiers);

        _variablesComparator.setModifierSorting(sortModifiers);
        sort(tree, _defaultComparator);
    }


    /**
     * Sorts the given tree.
     *
     * @param tree root node of the tree.
     * @param comp comparator to use.
     */
    public void sort(
        AST        tree,
        Comparator comp)
    {
        if (tree == null)
        {
            return;
        }

        AST first = null;
LOOP:

        // advance to the first CLASS_DEF or INTERFACE_DEF
        for (AST child = tree.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.CLASS_DEF :
                case JavaTokenTypes.INTERFACE_DEF :
                    //case JavaTokenTypes.ENUM_DEF:
                    //case JavaTokenTypes.ANNOTATION_DEF:
                    first = child;

                    break LOOP;
            }
        }

        for (
            AST declaration = first; declaration != null;
            declaration = declaration.getNextSibling())
        {
            sortDeclarations(declaration, comp, 1);
        }
    }


    /**
     * Determines whether the given declaration node contains the <code>static</code>
     * modifier.
     *
     * @param node declaration node.
     *
     * @return <code>true</code> if the given node contains the <code>static</code>
     *         modifier.
     *
     * @since 1.0b8
     */
    private boolean isStatic(AST node)
    {
        return JavaNodeModifier.isStatic(
            JavaNodeHelper.getFirstChild(node, JavaTokenTypes.MODIFIERS));
    }


    /**
     * Appends <em>sibling</em> as a new sibling to <em>node</em>.
     *
     * @param node the currently last sibling.
     * @param sibling the new sibling that should be appended.
     */
    private void addChild(
        JavaNode node,
        JavaNode sibling)
    {
        node.setNextSibling(sibling);
        sibling.setPreviousSibling(node);
        sibling.setNextSibling(null); // don't leave any old pointers left
    }


    /**
     * Add all nodes of the given list as siblings to the given node.
     *
     * @param nodes nodes to add.
     * @param node node to add the nodes as siblings to.
     * @param addSeparator if <code>true</code> adds a separator comment before the first
     *        node of the siblings list.
     * @param indent current indentation length.
     * @param maxwidth the maximum line length.
     *
     * @return the last node that was added. Returns the given node if no nodes were
     *         added.
     *
     * @throws IllegalArgumentException if <em>node</em> contains an invalid node.
     */
    private JavaNode addSiblings(
        List     nodes,
        JavaNode node,
        boolean  addSeparator,
        int      indent,
        int      maxwidth)
    {
        JavaNode cur = node;

        if (nodes.size() > 0)
        {
            JavaNode next = (JavaNode) nodes.get(0);
            addChild(cur, next);
            cur = next;

            if (addSeparator)
            {
                ExtendedToken comment =
                    _factory.getExtendedTokenFactory().create(JavaTokenTypes.SEPARATOR_COMMENT, EMPTY_STRING);

                if (next.hasCommentsBefore())
                {
                    for (
                            ExtendedToken tok = (ExtendedToken)next.getHiddenBefore();
                        tok != null; tok = (ExtendedToken) tok.getHiddenBefore())
                    {
                        if (tok.getHiddenBefore() == null)
                        {
                            tok.setHiddenBefore(comment);
                            comment.setHiddenAfter(tok);

                            break;
                        }
                    }
                }
                else
                {
                    next.setHiddenBefore(comment);
                }

                Convention settings = Convention.getInstance();
                String fillCharacter =
                    settings.get(ConventionKeys.SEPARATOR_FILL_CHARACTER, "\u00b7");

                switch (next.getType())
                {
                    case JavaTokenTypes.VARIABLE_DEF :

                        if (isStatic(cur))
                        {
                            fillComment(
                                comment,
                                settings.get(
                                    ConventionKeys.SEPARATOR_STATIC_VAR_INIT,
                                    "Static variables/initializers"), fillCharacter,
                                indent, maxwidth);
                        }
                        else
                        {
                            fillComment(
                                comment,
                                settings.get(
                                    ConventionKeys.SEPARATOR_INSTANCE_VAR,
                                    "Instance variables"), fillCharacter, indent, maxwidth);
                        }

                        break;

                    case JavaTokenTypes.METHOD_DEF :
                        fillComment(
                            comment,
                            settings.get(ConventionKeys.SEPARATOR_METHOD, "Methods"),
                            fillCharacter, indent, maxwidth);

                        break;

                    case JavaTokenTypes.CTOR_DEF :
                        fillComment(
                            comment,
                            settings.get(ConventionKeys.SEPARATOR_CTOR, "Constructors"),
                            fillCharacter, indent, maxwidth);

                        break;

                    case JavaTokenTypes.CLASS_DEF :
                        fillComment(
                            comment,
                            settings.get(ConventionKeys.SEPARATOR_CLASS, "Inner classes"),
                            fillCharacter, indent, maxwidth);

                        break;

                    case JavaTokenTypes.INTERFACE_DEF :
                        fillComment(
                            comment,
                            settings.get(
                                ConventionKeys.SEPARATOR_INTERFACE, "Inner Interfaces"),
                            fillCharacter, indent, maxwidth);

                        break;

                    case JavaTokenTypes.STATIC_INIT :
                        fillComment(
                            comment,
                            settings.get(
                                ConventionKeys.SEPARATOR_STATIC_VAR_INIT,
                                "Static variables/initializers"), fillCharacter, indent,
                            maxwidth);

                        break;

                    case JavaTokenTypes.INSTANCE_INIT :
                        fillComment(
                            comment,
                            settings.get(
                                ConventionKeys.SEPARATOR_INSTANCE_INIT,
                                "Instance initializers"), fillCharacter, indent, maxwidth);

                        break;
                    case JavaTokenTypes.ENUM_DEF :
                        fillComment(
                            comment,
                            settings.get(
                                ConventionKeys.SEPARATOR_ENUM_INIT,
                                "Enumeration initializers"), fillCharacter, indent, maxwidth);

                        break;
                        
                    case JavaTokenTypes.ENUM_CONSTANT_DEF:
                        fillComment(
                            comment,
                            settings.get(
                                ConventionKeys.SEPARATOR_ENUM_CONSTANT_INIT,
                                "Enumeration constant initializers"), fillCharacter, indent, maxwidth);

                        break;
                        
                    case JavaTokenTypes.ANNOTATION_DEF :
                        fillComment(
                            comment,
                            settings.get(
                                ConventionKeys.SEPARATOR_ANNOTATION_INIT,
                                "Annotation initializers"), fillCharacter, indent, maxwidth);

                        break;
                        
                    default :
                        throw new IllegalArgumentException("unexpected type -- " + cur);
                }
            }
        }
        else
        {
            return node;
        }

        for (int i = 1, size = nodes.size(); i < size; i++)
        {
            JavaNode next = (JavaNode) nodes.get(i);
            addChild(cur, next);
            cur = next;
        }

        return cur;
    }


    /**
     * Fills the given separator comment up to the given maximum size with a given
     * character.
     *
     * @param comment comment to fill.
     * @param text comment text.
     * @param character character to use.
     * @param indent current indent length.
     * @param maxwidth maximum line length.
     */
    private void fillComment(
        ExtendedToken comment,
        String        text,
        String        character,
        int           indent,
        int           maxwidth)
    {
        StringBuffer buf = new StringBuffer(maxwidth);
        buf.append("//~ ");
        buf.append(text);
        buf.append(' ');

        for (int i = text.length() + 4, size = maxwidth - indent - 1; i < size; i++)
        {
            buf.append(character);
        }

        comment.setText(buf.toString());
    }


    /**
     * Sorts the given tree portion.
     *
     * @param node the root node of the tree to sort. Either a CLASS_DEF or
     *        INTERFACE_DEF.
     * @param comp comparator to use for sorting.
     * @param level the current recursion level (1-based).
     *
     * @return the updated node.
     *
     * @throws IllegalArgumentException if the given node contains a child of an unknown
     *         type.
     */
    private AST sortDeclarations(
        AST        node,
        Comparator comp,
        int        level)
    {
        JavaNode lcurly = null;

        switch (node.getType())
        {
            case JavaTokenTypes.CLASS_DEF :
                lcurly = (JavaNode) JavaNodeHelper.getFirstChild(node,JavaTokenTypes.OBJBLOCK);
//                    (JavaNode) node.getFirstChild().getNextSibling().getNextSibling().getNextSibling()
//                                   .getNextSibling();

                break;

            case JavaTokenTypes.INTERFACE_DEF :
                lcurly =
                     (JavaNode) JavaNodeHelper.getFirstChild(node,JavaTokenTypes.OBJBLOCK); 
                    //node.getFirstChild().getNextSibling().getNextSibling().getNextSibling();

                break;
            case JavaTokenTypes.ANNOTATION_DEF :
                lcurly =
                     (JavaNode) JavaNodeHelper.getFirstChild(node,JavaTokenTypes.OBJBLOCK); 
                    //node.getFirstChild().getNextSibling().getNextSibling().getNextSibling();
            
            case JavaTokenTypes.ENUM_DEF :
                lcurly =
                     (JavaNode) JavaNodeHelper.getFirstChild(node,JavaTokenTypes.OBJBLOCK); 
                    //node.getFirstChild().getNextSibling().getNextSibling().getNextSibling();

                break;

            default :
                return node;
        }

        switch (lcurly.getFirstChild().getType())
        {
            // empty block, nothing to do
            case JavaTokenTypes.RCURLY :
                return node;
        }

        List staticStuff = new ArrayList(3); // both variables and initializers
        List variables = new ArrayList(); // instance variables
        List initializers = new ArrayList(3); // instance initializers
        List ctors = new ArrayList(5);
        List methods = new ArrayList();
        List classes = new ArrayList(3);
        List interfaces = new ArrayList(3);
        List annotations = new ArrayList(3);
        List enums = new ArrayList(3);
        List enumdef = new ArrayList(3);
        List names = new ArrayList(); // type names of all instance variables

        AST rcurly = null; // stores the last rcurly

        // add nodes to the different lists
        for (
            AST child = lcurly.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.METHOD_DEF :
                    methods.add(child);

                    break;

                case JavaTokenTypes.VARIABLE_DEF :

                    if (isStatic(child))
                    {
                        staticStuff.add(child);
                    }
                    else
                    {
                        // store the name of the variable types
                        names.add(
                            JavaNodeHelper.getFirstChild(child, JavaTokenTypes.IDENT)
                                          .getText());
                        variables.add(child);
                    }

                    break;

                case JavaTokenTypes.CTOR_DEF :
                    ctors.add(child);

                    break;

                case JavaTokenTypes.STATIC_INIT :
                    staticStuff.add(child);

                    break;

                case JavaTokenTypes.INSTANCE_INIT :
                    initializers.add(child);

                    break;

                case JavaTokenTypes.CLASS_DEF :
                    classes.add(sortDeclarations(child, comp, level + 1));

                    break;

                case JavaTokenTypes.INTERFACE_DEF :
                    interfaces.add(sortDeclarations(child, comp, level + 1));

                    break;

                case JavaTokenTypes.RCURLY :
                    rcurly = child;

                    break;
                case JavaTokenTypes.ANNOTATION_DEF :
                    annotations.add(child);
                break;

                case JavaTokenTypes.ENUM_DEF :
                    enums.add(sortDeclarations(child, comp, level + 1));
                break;
                case JavaTokenTypes.ENUM_CONSTANT_DEF :
                    enumdef.add(child);
                break;

                case JavaTokenTypes.SEMI :
                    // it is perfectly valid to use a SEMI and totally
                    // useless, we ignore it and don't care (at least until
                    // someone rings a bell)
                    break;

                default :
                    throw new IllegalArgumentException("cannot handle node -- " + child);
            }
        }

        Convention settings = Convention.getInstance();

        if (
            settings.getBoolean(
                ConventionKeys.SORT_VARIABLE, ConventionDefaults.SORT_VARIABLE))
        {
            // because we recursively link into inner classes in the switch we
            // have to set our type names for every level
            _variablesComparator.names = names;
            Collections.sort(variables, _variablesComparator);
            names.clear();
        }

        if (settings.getBoolean(ConventionKeys.SORT_CTOR, ConventionDefaults.SORT_CTOR))
        {
            Collections.sort(ctors, comp);
        }

        if (
            settings.getBoolean(
                ConventionKeys.SORT_METHOD, ConventionDefaults.SORT_METHOD))
        {
            Collections.sort(methods, comp);
        }

        if (settings.getBoolean(ConventionKeys.SORT_CLASS, ConventionDefaults.SORT_CLASS))
        {
            Collections.sort(classes, comp);
        }

        if (
            settings.getBoolean(
                ConventionKeys.SORT_INTERFACE, ConventionDefaults.SORT_INTERFACE))
        {
            Collections.sort(interfaces, comp);
        }
        if (
                settings.getBoolean(
                    ConventionKeys.SORT_ENUM, ConventionDefaults.SORT_ENUM))
            {
                Collections.sort(enums, comp);
            }
        if (
                settings.getBoolean(
                    ConventionKeys.SORT_ANNOTATION, ConventionDefaults.SORT_ANNOTATION))
            {
                Collections.sort(annotations, comp);
            }

        Map nodemap = new HashMap(10, 1.0f);
        nodemap.put(DeclarationType.STATIC_VARIABLE_INIT.getName(), staticStuff);
        nodemap.put(DeclarationType.VARIABLE.getName(), variables);
        nodemap.put(DeclarationType.INIT.getName(), initializers);
        nodemap.put(DeclarationType.CTOR.getName(), ctors);
        nodemap.put(DeclarationType.METHOD.getName(), methods);
        nodemap.put(DeclarationType.INTERFACE.getName(), interfaces);
        nodemap.put(DeclarationType.CLASS.getName(), classes);
        nodemap.put(DeclarationType.ANNOTATION.getName(), annotations);
        nodemap.put(DeclarationType.ENUM.getName(), enums);

        boolean addSeparator = false;

        if (level == 1)
        {
            addSeparator =
                settings.getBoolean(
                    ConventionKeys.COMMENT_INSERT_SEPARATOR,
                    ConventionDefaults.COMMENT_INSERT_SEPARATOR);
        }
        else
        {
            addSeparator =
                settings.getBoolean(
                    ConventionKeys.COMMENT_INSERT_SEPARATOR_RECURSIVE,
                    ConventionDefaults.COMMENT_INSERT_SEPARATOR_RECURSIVE);
        }

        String sortString =
            settings.get(ConventionKeys.SORT_ORDER, DeclarationType.getOrder());
        int maxwidth =
            settings.getInt(ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);
        int indent =
            settings.getInt(ConventionKeys.INDENT_SIZE, ConventionDefaults.INDENT_SIZE);
        JavaNode tmp = (JavaNode) _factory.getJavaNodeFactory().create();
        JavaNode current = tmp;

        // add the different declaration groups in the specified order
        if (!enumdef.isEmpty()) {
            // Add in any enumeration definitions first
            current =
                addSiblings(
                    enumdef, current, addSeparator,
                    indent * level, maxwidth);
            
        } // end if
        for (
            StringTokenizer tokens = new StringTokenizer(sortString, "|");
            tokens.hasMoreTokens();)
        {
            String nextToken = tokens.nextToken(); 
            current =
                addSiblings(
                    (List) nodemap.get(nextToken), current, addSeparator,
                    indent * level, maxwidth);
        }

        current.setNextSibling(rcurly);

        // get the first sibling
        JavaNode sibling = (JavaNode) tmp.getNextSibling();

        // and link it into the tree
        sibling.setPreviousSibling(lcurly);
        lcurly.setFirstChild(sibling);

        tmp.setNextSibling(null); // don't leave any old pointers set

        current.setNextSibling(rcurly);

        staticStuff.clear(); // both variables and initializers
        variables.clear(); // instance variables
        initializers.clear(); // instance initializers
        ctors.clear();
        methods.clear();
        classes.clear();
        interfaces.clear();
        annotations.clear();
        enumdef.clear();
        enums.clear();
        names.clear(); // type names of all instance variables
        return node;
    }
}
