/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.io.File;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.util.ResourceBundleFactory;


/**
 * Inspects a Java parse tree for convention violations and possible code weaknesses.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.7 $
 *
 * @since 1.0b8
 */
public final class CodeInspector
    extends TreeWalker
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Type names for which a base type should be favored. */
    private static final List _favorableTypes = new ArrayList(20); // List of <String>

    /** Collection type names. */
    private static final List _collectionTypes = new ArrayList(28); // List of <String>

    /** The cache with the regular expression patterns. */
    private static final HashMap _patternMap = new HashMap(30);

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME =
        "de.hunsicker.jalopy.language.Bundle" /* NOI18N */;

    /** The code convention. */
    protected static final Convention _settings = Convention.getInstance();
    private static final String STR_boolean = "boolean" /* NOI18N */;
    private static final String STR_Object = "Object" /* NOI18N */;
    private static final String STR_equals = "equals" /* NOI18N */;
    private static final String STR_javalangObject = "java.lang.Object" /* NOI18N */;
    private static final String STR_String = "String" /* NOI18N */;
    private static final String STR_toString = "toString" /* NOI18N */;
    private static final String STR_void = "void" /* NOI18N */;
    private static final String STR_finalize = "finalize" /* NOI18N */;
    private static final String STR_int = "int" /* NOI18N */;
    private static final String STR_hashCode = "hashCode" /* NOI18N */;
    private static final String STR_wait = "wait" /* NOI18N */;
    private static final String STR_Exception = "Exception" /* NOI18N */;
    private static final String STR_Throwable = "Throwable" /* NOI18N */;
    private static final String STR_STRING_LITERAL_I18N =
        "STRING_LITERAL_I18N" /* NOI18N */;

    private static final String STR_OBEY_CONTRACT_EQUALS =
        "OBEY_CONTRACT_EQUALS" /* NOI18N */;

    private static final String STR_DONT_SUBSTITUTE_OBJECT_EQUALS =
        "DONT_SUBSTITUTE_OBJECT_EQUALS" /* NOI18N */;
    private static final String STR_OVERRIDE_HASHCODE = "OVERRIDE_HASHCODE" /* NOI18N */;

    private static final String STR_OVERRIDE_TO_STRING =
        "OVERRIDE_TO_STRING" /* NOI18N */;


    private static final String STR_RETURN_ZERO_ARRAY = "RETURN_ZERO_ARRAY" /* NOI18N */;

    private static final String STR_INTERFACE_ONLY_FOR_TYPE =
        "INTERFACE_ONLY_FOR_TYPE" /* NOI18N */;

    private static final String STR_REPLACE_STRUCTURE_WITH_CLASS =
        "REPLACE_STRUCTURE_WITH_CLASS" /* NOI18N */;

    private static final String STR_REFER_BY_INTERFACE =
        "REFER_BY_INTERFACE" /* NOI18N */;
    private static final String STR_VARIABLE_SHADOW = "VARIABLE_SHADOW" /* NOI18N */;
    private static final String STR_DONT_IGNORE_EXCEPTION =
        "DONT_IGNORE_EXCEPTION" /* NOI18N */;

    private static final String STR_NEVER_THROW_EXCEPTION =
        "NEVER_THROW_EXCEPTION" /* NOI18N */;
    private static final String STR_NEVER_THROW_THROWABLE =
        "NEVER_THROW_THROWABLE" /* NOI18N */;

    private static final String STR_DECLARE_COLLECTION_COMMENT =
        "DECLARE_COLLECTION_COMMENT" /* NOI18N */;
    private static final String STR_WRONG_COLLECTION_COMMENT =
        "WRONG_COLLECTION_COMMENT" /* NOI18N */;

    private static final String STR_EMPTY_FINALLY = "EMPTY_FINALLY" /* NOI18N */;

    private static final String STR_NEVER_WAIT_OUTSIDE_LOOP =
        "NEVER_WAIT_OUTSIDE_LOOP" /* NOI18N */;
    private static final String STR_NAMING_CONVENTION = "NAMING_CONVENTION" /* NOI18N */;
    private static final String EMPTY_STRING = "" /* NOI18N */;

    static
    {
        _favorableTypes.add("Vector" /* NOI18N */);
        _favorableTypes.add("ArrayList" /* NOI18N */);
        _favorableTypes.add("HashSet" /* NOI18N */);
        _favorableTypes.add("TreeSet" /* NOI18N */);
        _favorableTypes.add("HashMap" /* NOI18N */);
        _favorableTypes.add("TreeMap" /* NOI18N */);
        _favorableTypes.add("IdentityHashMap" /* NOI18N */);
        _favorableTypes.add("WeakHashMap" /* NOI18N */);
        _favorableTypes.add("Hashtable" /* NOI18N */);
        _favorableTypes.add("java.util.ArrayList" /* NOI18N */);
        _favorableTypes.add("java.util.Vector" /* NOI18N */);
        _favorableTypes.add("java.util.HashSet" /* NOI18N */);
        _favorableTypes.add("java.util.TreeSet" /* NOI18N */);
        _favorableTypes.add("java.util.HashMap" /* NOI18N */);
        _favorableTypes.add("java.util.TreeMap" /* NOI18N */);
        _favorableTypes.add("java.util.IdentityHashMap" /* NOI18N */);
        _favorableTypes.add("java.util.WeakHashMap" /* NOI18N */);
        _favorableTypes.add("java.util.Hashtable" /* NOI18N */);

        _collectionTypes.add("Collection" /* NOI18N */);
        _collectionTypes.add("List" /* NOI18N */);
        _collectionTypes.add("ArrayList" /* NOI18N */);
        _collectionTypes.add("LinkedList" /* NOI18N */);
        _collectionTypes.add("Vector" /* NOI18N */);
        _collectionTypes.add("Set" /* NOI18N */);
        _collectionTypes.add("ArraySet" /* NOI18N */);
        _collectionTypes.add("HashSet" /* NOI18N */);
        _collectionTypes.add("LinkedHashSet" /* NOI18N */);
        _collectionTypes.add("SortedSet" /* NOI18N */);
        _collectionTypes.add("TreeSet" /* NOI18N */);
        _collectionTypes.add("Map" /* NOI18N */);
        _collectionTypes.add("SortedMap" /* NOI18N */);
        _collectionTypes.add("HashMap" /* NOI18N */);
        _collectionTypes.add("Hashtable" /* NOI18N */);
        _collectionTypes.add("TreeMap" /* NOI18N */);
        _collectionTypes.add("IdentityHashMap" /* NOI18N */);
        _collectionTypes.add("WeakHashMap" /* NOI18N */);

        _collectionTypes.add("java.util.Collection" /* NOI18N */);
        _collectionTypes.add("java.util.List" /* NOI18N */);
        _collectionTypes.add("java.util.ArrayList" /* NOI18N */);
        _collectionTypes.add("java.util.LinkedList" /* NOI18N */);
        _collectionTypes.add("java.util.Vector" /* NOI18N */);
        _collectionTypes.add("java.util.Set" /* NOI18N */);
        _collectionTypes.add("java.util.ArraySet" /* NOI18N */);
        _collectionTypes.add("java.util.HashSet" /* NOI18N */);
        _collectionTypes.add("java.util.LinkedHashSet" /* NOI18N */);
        _collectionTypes.add("java.util.SortedSet" /* NOI18N */);
        _collectionTypes.add("java.util.TreeSet" /* NOI18N */);
        _collectionTypes.add("java.util.Map" /* NOI18N */);
        _collectionTypes.add("java.util.SortedMap" /* NOI18N */);
        _collectionTypes.add("java.util.HashMap" /* NOI18N */);
        _collectionTypes.add("java.util.Hashtable" /* NOI18N */);
        _collectionTypes.add("java.util.TreeMap" /* NOI18N */);
        _collectionTypes.add("java.util.IdentityHashMap" /* NOI18N */);
        _collectionTypes.add("java.util.WeakHashMap" /* NOI18N */);
    }

    //~ Instance variables ---------------------------------------------------------------

    /**
     * Holds the issues found during inspection. Maps one node to either exaclty one
     * issue or a list of the resource keys of several issues.
     */
    private Map _issues; // Map of <JavaNode>:<Object>

    /** The pattern matcher. */
    //private final PatternMatcher _matcher = new Perl5Matcher();

    /** Helper array used to store the arguments for the message formatter. */
    protected final String[] _args = new String[3];
	
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new CodeInspector object.
     *
     * @param issues map to hold the found issues.
     */
    public CodeInspector(Map issues)
    {
        _issues = issues;
    }

    //~ Methods --------------------------------------------------------------------------

	private Pattern getPattern(String pattern) {
		if (_patternMap.get(pattern)==null) {
			_patternMap.put(pattern,Pattern.compile(pattern));
		}
		
		return (Pattern) _patternMap.get(pattern);
	}
    /**
     * Inspects the given Java parse tree for code convention violations and coding
     * weaknesses.
     *
     * @param tree root node of the parse tree.
     * @param file the file that is inspected.
     */
    public void inspect(
        AST  tree,
        File file)
    {
        walk(tree);
    }


    /**
     * {@inheritDoc}
     */
    public void visit(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.VARIABLE_DEF :

                AST type = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.TYPE);
                String returnType = type.getFirstChild().getText();

                if (
                    _settings.getBoolean(ConventionKeys.TIP_REFER_BY_INTERFACE, false)
                    && _favorableTypes.contains(returnType))
                {
                    addIssue(node, STR_REFER_BY_INTERFACE, _args);
                }

                checkCollectionReturnType((JavaNode) node, returnType);

                String name = type.getNextSibling().getText();
                checkVariableName(node, name);

                break;

            case JavaTokenTypes.METHOD_CALL :
                checkMethodCall(node);

                break;

            case JavaTokenTypes.ASSIGN :
                checkAssignment(node);

                break;

            case JavaTokenTypes.LITERAL_catch :
                checkCatch(node);

                break;

            case JavaTokenTypes.LITERAL_finally :
                checkFinally(node);

                break;

            case JavaTokenTypes.LABELED_STAT :
                checkLabelName(node);

                break;

            case JavaTokenTypes.METHOD_DEF :

                Method method = new Method(node);
                checkObjectEquals(node, method);
                checkReturnType(node, method);
                checkParameters(node, method);
                checkThrows(node, method);
                checkMethodName(node, method);

                if (!method.isAbstract())
                {
                    checkArrayReturnType(node, method);
                }

                break;

            case JavaTokenTypes.CTOR_DEF :

                Constructor ctor = new Constructor(node);
                checkParameters(node, ctor);
                checkThrows(node, ctor);

                break;

            case JavaTokenTypes.CLASS_DEF :
                checkClass(node);
                checkClassName(node);

                break;

            case JavaTokenTypes.INTERFACE_DEF :
                checkInterface(node);
                checkInterfaceName(node);

                break;

            case JavaTokenTypes.PACKAGE_DEF :
                checkPackageName(node);

                break;

            case JavaTokenTypes.STRING_LITERAL :
                checkStringLiteral(node);

                break;
        }
    }


    /**
     * Indicates whether the given object represents the &quot;public boolean
     * equals(java.lang.Object o)&quot; method.
     *
     * @param method the method object.
     *
     * @return <code>true</code> if the given method type represents the equals method.
     */
    private boolean isEqualsMethod(Method method)
    {
        if (method.getParameterCount() != 1)
        {
            return false;
        }

        if (
            (method.modifierMask != Modifier.PUBLIC)
            && !Modifier.isPublic(method.modifierMask))
        {
            return false;
        }

        if (!STR_boolean.equals(method.returnType))
        {
            return false;
        }

        if (!STR_equals.equals(method.name))
        {
            return false;
        }

        Parameter parameter = (Parameter) method.parameters.get(0);

        if (parameter.modifierMask != 0)
        {
            return false;
        }

        if (
            !(STR_Object.equals(parameter.typeName))
            && STR_javalangObject.equals(parameter.typeName))
        {
            return false;
        }

        return true;
    }


    /**
     * Indicates whether the given object represents the &quot;protected void
     * finalize()&quot; method.
     *
     * @param method the method object.
     *
     * @return <code>true</code> if the given method type represents the finalize method.
     */
    private boolean isFinalizeMethod(Method method)
    {
        if (method.getParameterCount() != 0)
        {
            return false;
        }

        if (
            (method.modifierMask != Modifier.PROTECTED)
            && !Modifier.isProtected(method.modifierMask))
        {
            return false;
        }

        if (!STR_void.equals(method.returnType))
        {
            return false;
        }

        if (!STR_finalize.equals(method.name))
        {
            return false;
        }

        return true;
    }


    /**
     * Indicates whether the given object represents the  &quot;<code>public int
     * hashCode()</code>&quot; method.
     *
     * @param method the method object.
     *
     * @return <code>true</code> if the given method type represents the hashCode method.
     */
    private boolean isHashCodeMethod(Method method)
    {
        if (method.getParameterCount() != 0)
        {
            return false;
        }

        if (
            (method.modifierMask != Modifier.PUBLIC)
            && !Modifier.isPublic(method.modifierMask))
        {
            return false;
        }

        if (!STR_int.equals(method.returnType))
        {
            return false;
        }

        if (!STR_hashCode.equals(method.name))
        {
            return false;
        }

        return true;
    }


    /**
     * Searches the tree way back to determine the OBJBLOCK parent of the given node.
     *
     * @param node a node of the tree below an OBJBLOCK node.
     *
     * @return the OBJBLOCK parent of the given node.
     */
    private JavaNode getObjectBlock(JavaNode node)
    {
        JavaNode parent = node.getParent();

        switch (node.getType())
        {
            case JavaTokenTypes.OBJBLOCK :
                return node;

            default :
                return getObjectBlock(parent);
        }
    }


    /**
     * Indicates whether the given object represents a substituted &quot;<code>public
     * boolean equals(java.lang.Object o)</code>&quot; method (e.g. &quot;public boolean
     * equals(MyObject o)&quot;).
     *
     * @param method the method object.
     *
     * @return <code>true</code> if the given method type represents a substituted equals
     *         method.
     */
    private boolean isSubstituteEqualsMethod(Method method)
    {
        if (method.getParameterCount() != 1)
        {
            return false;
        }

        if (
            (method.modifierMask != Modifier.PUBLIC)
            && !Modifier.isPublic(method.modifierMask))
        {
            return false;
        }

        if (!STR_boolean.equals(method.returnType))
        {
            return false;
        }

        if (!STR_equals.equals(method.name))
        {
            return false;
        }

        Parameter parameter = (Parameter) method.parameters.get(0);

        if (parameter.modifierMask != 0)
        {
            return false;
        }

        if (
            (STR_Object.equals(parameter.typeName))
            || (STR_javalangObject.equals(parameter.typeName)))
        {
            return false;
        }

        return true;
    }


    /**
     * Indicates whether the given object represents the &quot;public String
     * toString()&quot; method.
     *
     * @param method the method object.
     *
     * @return <code>true</code> if the given method type represents the toString method.
     */
    private boolean isToStringMethod(Method method)
    {
        if (method.getParameterCount() != 0)
        {
            return false;
        }

        if (
            (method.modifierMask != Modifier.PUBLIC)
            && !Modifier.isPublic(method.modifierMask))
        {
            return false;
        }

        if (!STR_String.equals(method.returnType))
        {
            return false;
        }

        return STR_toString.equals(method.name);
    }


    /**
     * Adds the given rule message for the given node.
     *
     * @param node node to add a tip for.
     * @param resourceKey resource key of the rule.
     * @param args the arguments for message formatting.
     */
    protected void addIssue(
        AST      node,
        String   resourceKey,
        Object[] args)
    {
        if (!_issues.containsKey(node))
        {
            _issues.put(
                node,
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(resourceKey),
                    args));
        }
        else
        {
            Object value = _issues.get(node);

            if (value instanceof List)
            {
                List issuesForNode = (List) value;
                issuesForNode.add(
                    MessageFormat.format(
                        ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                            resourceKey), args));
            }
            else
            {
                List issuesForNode = new ArrayList(4);
                issuesForNode.add(value);
                issuesForNode.add(
                    MessageFormat.format(
                        ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                            resourceKey), args));
                _issues.put(node, issuesForNode);
            }
        }
    }


    /**
     * Checks whether, in case the method has an array return type, the given method
     * returns always an array and not <code>null</code>.
     *
     * @param node the METHOD_DEF node.
     * @param method the method object.
     */
    private void checkArrayReturnType(
        AST    node,
        Method method)
    {
        if (_settings.getBoolean(ConventionKeys.TIP_RETURN_ZERO_ARRAY, false))
        {
            String returnType = method.returnType;

            if (returnType.indexOf('[') > -1) // it's an array thingy
            {
                TreeWalker walker =
                    new TreeWalker()
                    {
                        public void visit(AST newNode)
                        {
                            switch (newNode.getType())
                            {
                                case JavaTokenTypes.LITERAL_return :

                                    AST child = newNode.getFirstChild();
                                    int retType = 0;

                                    switch (child.getType())
                                    {
                                        case JavaTokenTypes.EXPR :
                                            retType = child.getFirstChild().getType();

                                            break;

                                        case JavaTokenTypes.LPAREN :

                                            AST expr =
                                                JavaNodeHelper.advanceToFirstNonParen(child);

                                            switch (expr.getType())
                                            {
                                                case JavaTokenTypes.EXPR :
                                                    retType = expr.getFirstChild().getType();

                                                    break;
                                            }

                                            break;
                                    }

                                    switch (retType)
                                    {
                                        case JavaTokenTypes.LITERAL_null :

                                            if (
                                                _settings.getBoolean(
                                                    ConventionKeys.TIP_RETURN_ZERO_ARRAY,
                                                    false))
                                            {
                                                addIssue(newNode, STR_RETURN_ZERO_ARRAY, _args);
                                            }

                                            stop();

                                            break;
                                    }

                                    break;
                            }
                        }
                    };

                walker.walk(JavaNodeHelper.getFirstChild(node, JavaTokenTypes.SLIST));
            }
        }
    }


    private void checkAssignment(AST node)
    {
        if (_settings.getBoolean(ConventionKeys.TIP_VARIABLE_SHADOW, false))
        {
            AST lhs = node.getFirstChild();

            switch (lhs.getType())
            {
                case JavaTokenTypes.IDENT :

                    AST rhs = lhs.getNextSibling();

                    if (rhs != null)
                    {
                        switch (rhs.getType())
                        {
                            case JavaTokenTypes.IDENT :

                                // possible shadowing found,
                                if (lhs.getText().equals(rhs.getText()))
                                {
                                    JavaNode objBlock = getObjectBlock((JavaNode) node);
                                    boolean foundReference = false;

                                    for (
                                        AST child = objBlock.getFirstChild(); child != null;
                                        child = child.getNextSibling())
                                    {
                                        switch (child.getType())
                                        {
                                            case JavaTokenTypes.VARIABLE_DEF :

                                                String name =
                                                    JavaNodeHelper.getFirstChild(
                                                        child, JavaTokenTypes.IDENT).getText();

                                                if (name.equals(lhs.getText()))
                                                {
                                                    StringBuffer buf = new StringBuffer(100);
                                                    buf.append("this." /* NOI18N */);
                                                    buf.append(name);
                                                    buf.append(" == " /* NOI18N */);
                                                    buf.append(name);

                                                    _args[2] = buf.toString();

                                                    if (
                                                        _settings.getBoolean(
                                                            ConventionKeys.TIP_VARIABLE_SHADOW,
                                                            false))
                                                    {
                                                        addIssue(
                                                            node, STR_VARIABLE_SHADOW, _args);
                                                    }

                                                    foundReference = true;
                                                }

                                                break;
                                        }
                                    }

                                    if (!foundReference)
                                    {
                                        /**
                                         * @todo
                                         */

                                        // 1. if inner class and non-static, search parents
                                        // 2. if anonymous inner class, search parents
                                        // 3. if dereived class, print message about possible shadowing
                                    }
                                }

                                break;
                        }
                    }

                    break;
            }
        }
    }


    /**
     * Checks whether the given catch clause is not empty.
     *
     * @param node a LITERAL_catch node.
     */
    private void checkCatch(AST node)
    {
        if (_settings.getBoolean(ConventionKeys.TIP_DONT_IGNORE_EXCEPTION, false))
        {
            JavaNode lcurly = (JavaNode) JavaNodeHelper.getFirstChild(node, JavaTokenTypes.SLIST);
            JavaNode child = (JavaNode)lcurly.getFirstChild();

            // if this is an empty catch and the user does not document why it is empty
            if (child.getType() == JavaTokenTypes.RCURLY && !child.hasCommentsBefore())
            {
                addIssue(node, STR_DONT_IGNORE_EXCEPTION, _args);
            }
        }
    }


    /**
     * Checks whether the given class represents a structure and, in case equals is
     * overriden, that hashCode is also overriden (or vice versa). Also checks whether
     * the toString method is overriden.
     *
     * @param node a CLASS_DEF node.
     */
    private void checkClass(AST node)
    {
        boolean checkReplaceStructure =
            _settings.getBoolean(ConventionKeys.TIP_REPLACE_STRUCTURE_WITH_CLASS, false);
        boolean checkOverrideHashCode =
            _settings.getBoolean(ConventionKeys.TIP_OVERRIDE_HASHCODE, false);
        boolean checkOverrideToString =
            _settings.getBoolean(ConventionKeys.TIP_OVERRIDE_TO_STRING, false);

        if (
            !checkReplaceStructure && !checkOverrideHashCode
            && !checkOverrideToString)
        {
            return;
        }

        AST classModifierMask =
            JavaNodeHelper.getFirstChild(node, JavaTokenTypes.MODIFIERS);
        boolean isPublicClass =
            Modifier.isPublic(JavaNodeModifier.valueOf(classModifierMask));
        AST body = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.OBJBLOCK);
        boolean violate = isPublicClass;
        boolean foundEquals = false;
        AST equalsNode = null;
        boolean foundHashCode = false;
        boolean foundToString = false;

        for (AST child = body.getFirstChild(); child != null;
            child = child.getNextSibling())
        {
            switch (child.getType())
            {
                case JavaTokenTypes.LCURLY :
                case JavaTokenTypes.RCURLY :
                    break;

                case JavaTokenTypes.VARIABLE_DEF :

                    if (checkReplaceStructure && isPublicClass)
                    {
                        int vaiableModifierMask =
                            JavaNodeModifier.valueOf(child.getFirstChild());

                        if (!Modifier.isPublic(vaiableModifierMask))
                        {
                            violate = false;
                        }
                    }

                    break;

                case JavaTokenTypes.METHOD_DEF :
                    violate = false;

                    Method method = null;

                    if (checkOverrideHashCode &&
                        !foundEquals && isEqualsMethod(method == null ? (method = new Method(child)): method))
                    {
                        equalsNode = child;
                        foundEquals = true;
                    }

                    if (checkOverrideHashCode &&
                        !foundHashCode && isHashCodeMethod(method == null ? (method = new Method(child)): method))
                    {
                        foundHashCode = true;
                    }

                    if (
                        checkOverrideToString && !foundToString
                        && isToStringMethod(method == null ? (method = new Method(child)): method))
                    {
                        foundToString = true;
                    }

                    break;

                default :
                    violate = false;

                    break;
            }
        }

        if (checkReplaceStructure && isPublicClass && violate && checkReplaceStructure)
        {
            addIssue(node, STR_REPLACE_STRUCTURE_WITH_CLASS, _args);
        }

        if (checkOverrideHashCode && foundEquals && !foundHashCode )
        {
            addIssue(equalsNode, STR_OVERRIDE_HASHCODE, _args);
        }

        if (checkOverrideToString && !foundToString)
        {
            addIssue(node, STR_OVERRIDE_TO_STRING, _args);
        }
    }


    /**
     * Checks whether the given class name adheres to the naming convention.
     *
     * @param node a CLASS_DEF node.
     */
    private void checkClassName(AST node)
    {
        if (
            _settings.getBoolean(
                ConventionKeys.TIP_ADHERE_TO_NAMING_CONVENTION, false))
        {
            String name = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.IDENT).getText();

            if (!JavaNodeModifier.isAbstract(node))
            {
                Pattern pattern =
                    getPattern(
                        _settings.get(
                            ConventionKeys.REGEXP_CLASS, ConventionDefaults.REGEXP_CLASS));

                if (
                    !pattern.pattern().equals(EMPTY_STRING)
                    && !pattern.matcher(name).matches())
                {
                    _args[0] = "Class" /* NOI18N */;
                    _args[1] = name;
                    _args[2] = pattern.pattern();
                    addIssue(node, STR_NAMING_CONVENTION, _args);
                }
            }
            else
            {
                Pattern pattern =
                    getPattern(
                        _settings.get(
                            ConventionKeys.REGEXP_CLASS_ABSTRACT,
                            ConventionDefaults.REGEXP_CLASS_ABSTRACT));

                if (
	                    !pattern.pattern().equals(EMPTY_STRING)
	                    && !pattern.matcher(name).matches())
                {
                    _args[0] = "Abstract class" /* NOI18N */;
                    _args[1] = name;
                    _args[2] = pattern.pattern();
                    addIssue(node, STR_NAMING_CONVENTION, _args);
                }
            }
        }
    }


    /**
     * Checks, in case the variable declares a collection type, whether an endline
     * comment states the type of the elements
     *
     * @param node a VARIABLE_DEF node.
     * @param returnType the returnType of the variable declaration.
     */
    private void checkCollectionReturnType(
        JavaNode node,
        String   returnType)
    {
        if (!JavaNodeHelper.isLocalVariable(node))
        {
            if (_collectionTypes.contains(returnType))
            {
                if (
                    !node.hasCommentsAfter()
                    && _settings.getBoolean(
                        ConventionKeys.TIP_DECLARE_COLLECTION_COMMENT, false))
                {
                    addIssue(node, STR_DECLARE_COLLECTION_COMMENT, _args);
                }
                else if (
                    _settings.getBoolean(
                        ConventionKeys.TIP_WRONG_COLLECTION_COMMENT, false))
                {
                    CommonHiddenStreamToken comment = node.getHiddenAfter();
                    if (comment==null) {
                        addIssue(node, STR_WRONG_COLLECTION_COMMENT, _args);
                    }
                    else {
	                    switch (comment.getType())
	                    {
	                        case JavaTokenTypes.SL_COMMENT :
	
	                            String text = comment.getText();
	
	                            if ((text.indexOf('<') == -1) || (text.indexOf('>') == -1))
	                            {
	                                addIssue(node, STR_WRONG_COLLECTION_COMMENT, _args);
	                            }
	
	                            break;
	
	                        default :
	
	                            /**
	                             * @todo print warning about wrong comment type
	                             */
	                            break;
	                    }
                    }
                }
            }
        }
    }


    /**
     * Checks whether method throws an exception (which would violate the equals
     * contract).
     *
     * @param node the METHOD_DEF node.
     * @param method the method object.
     */
    private void checkEqualsReturnType(
        AST    node,
        Method method)
    {
        if (
            _settings.getBoolean(
                ConventionKeys.TIP_OBEY_CONTRACT_EQUALS, false))
        {
            AST slist = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.SLIST);

            // no body for abstract methods!
            if (slist != null)
            {
                AST child = slist.getFirstChild();

                // if the method body does not soley consist of a throw statement
                if (child.getType() != JavaTokenTypes.LITERAL_throw ||
                    child.getNextSibling().getType() != JavaTokenTypes.RCURLY)
                {
                    TreeWalker walker =
                        new TreeWalker()
                        {
                            public void visit(AST newNode)
                            {
                                switch (newNode.getType())
                                {
                                    case JavaTokenTypes.LITERAL_throw :

                                        addIssue(newNode, STR_OBEY_CONTRACT_EQUALS, _args);
                                        stop();

                                        break;
                                }
                            }
                        };

                    walker.walk(slist);
                }
            }
        }
    }


    /**
     * Checks the given finally clause whether is it not empty.
     *
     * @param node a LITERAL_finally node.
     */
    private void checkFinally(AST node)
    {
        if (_settings.getBoolean(ConventionKeys.TIP_EMPTY_FINALLY, false))
        {
            if (
                (node.getFirstChild().getFirstChild().getType() == JavaTokenTypes.RCURLY)
                )
            {
                addIssue(node, STR_EMPTY_FINALLY, _args);
            }
        }
    }


    /**
     * Checks whether the given interface is misused to export constants.
     *
     * @param node a INTERFACE_DEF node.
     */
    private void checkInterface(AST node)
    {
        if (_settings.getBoolean(ConventionKeys.TIP_INTERFACE_ONLY_FOR_TYPE, false))
        {
            AST body = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.OBJBLOCK);
            boolean violate = true;
SEARCH:
            for (
                AST child = body.getFirstChild(); child != null;
                child = child.getNextSibling())
            {
                switch (child.getType())
                {
                    case JavaTokenTypes.RCURLY :

                        if (violate)
                        {
                            // tagging interfaces are just fine
                            violate = body.getFirstChild() != child;
                        }
                        break;

                    case JavaTokenTypes.VARIABLE_DEF :
                        // fields defined in interfaces are automatically 'static'
                        // and 'final', so no further checking here
                        break;

                    default :
                        // if we find something other than a VARIABLE_DEF than we know
                        // this can't be a constant interface
                        violate = false;

                        break SEARCH;
                }
            }

            if (violate)
            {
                addIssue(node, STR_INTERFACE_ONLY_FOR_TYPE, _args);
            }
        }
    }


    /**
     * Checks whether the given interface name adheres to the naming convention.
     *
     * @param node a INTERFACE_DEF node.
     */
    private void checkInterfaceName(AST node)
    {
        if (
            _settings.getBoolean(
                ConventionKeys.TIP_ADHERE_TO_NAMING_CONVENTION, false))
        {
            String name = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.IDENT).getText();
            Pattern pattern =
                getPattern(
                    _settings.get(
                        ConventionKeys.REGEXP_INTERFACE, ConventionDefaults.REGEXP_INTERFACE));

            if (
                    !pattern.pattern().equals(EMPTY_STRING)
                    && !pattern.matcher(name).matches())
            {
                _args[0] = "Interface" /* NOI18N */;
                _args[1] = name;
                _args[2] = pattern.pattern();
                addIssue(node, STR_NAMING_CONVENTION, _args);
            }
        }
    }


    /**
     * Checks whether the given label adheres to the naming convention.
     *
     * @param node a LABELED_STAT node.
     */
    private void checkLabelName(AST node)
    {
        if (
            _settings.getBoolean(
                ConventionKeys.TIP_ADHERE_TO_NAMING_CONVENTION, false))
        {
            String name = node.getFirstChild().getText();
            Pattern pattern =
                getPattern(
                    _settings.get(
                        ConventionKeys.REGEXP_LABEL, ConventionDefaults.REGEXP_LABEL));

            if (
                    !pattern.pattern().equals(EMPTY_STRING)
                    && !pattern.matcher(name).matches())
            {
                _args[0] = "Label" /* NOI18N */;
                _args[1] = name;
                _args[2] = pattern.pattern();
                addIssue(node, STR_NAMING_CONVENTION, _args);
            }
        }
    }


    /**
     * Checks, in case the method call represents the Object.wait() method, whether  it
     * is invoked from within a loop.
     *
     * @param node a METHOD_CALL node.
     */
    private void checkMethodCall(AST node)
    {
        if (!_settings.getBoolean(ConventionKeys.TIP_NEVER_WAIT_OUTSIDE_LOOP, false))
        {
            return;
        }

        AST child = node.getFirstChild();
        String name = null;

        switch (child.getType())
        {
            case JavaTokenTypes.IDENT :
                name = child.getText();

                break;

            case JavaTokenTypes.DOT :

                if (JavaNodeHelper.isChained(child))
                {
                    name =
                        JavaNodeHelper.getFirstChainLink(node).getNextSibling().getText();
                }
                else
                {
                    name = child.getFirstChild().getNextSibling().getText();
                }

                break;
        }

        if (STR_wait.equals(name))
        {
            JavaNode n = (JavaNode) node;
            JavaNode expr = n.getParent();

            switch (expr.getParent().getType())
            {
                case JavaTokenTypes.SLIST :

                    switch (expr.getParent().getParent().getType())
                    {
                        case JavaTokenTypes.LITERAL_while :
                        case JavaTokenTypes.LITERAL_for :
                            break;

                        default :
                            addIssue(n, STR_NEVER_WAIT_OUTSIDE_LOOP, _args);

                            break;
                    }

                    break;

                case JavaTokenTypes.LITERAL_while :
                case JavaTokenTypes.LITERAL_for :
                    break;

                default :
                    addIssue(n, STR_NEVER_WAIT_OUTSIDE_LOOP, _args);

                    break;
            }
        }
    }


    /**
     * Checks the given method name for naming violations.
     *
     * @param node a METHOD_DEF node.
     * @param method the method object for this node.
     *
     * @since 1.0b9
     */
    private void checkMethodName(
        AST    node,
        Method method)
    {
        if (
            _settings.getBoolean(
                ConventionKeys.TIP_ADHERE_TO_NAMING_CONVENTION, false))
        {
            AST modifiers = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.MODIFIERS);
            int modifierMask = JavaNodeModifier.valueOf(modifiers);

            String name = method.name;

            if (Modifier.isStatic(modifierMask)) // static methods
            {
                if (Modifier.isFinal(modifierMask)) // static final
                {
                    if (Modifier.isPublic(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_METHOD_PUBLIC_STATIC_FINAL,
                                    ConventionDefaults.REGEXP_METHOD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "public static final method" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else if (Modifier.isProtected(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_METHOD_PROTECTED_STATIC_FINAL,
                                    ConventionDefaults.REGEXP_METHOD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "protected static final method" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else if (Modifier.isPrivate(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_METHOD_PRIVATE_STATIC_FINAL,
                                    ConventionDefaults.REGEXP_METHOD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "private static final method" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_METHOD_DEFAULT_STATIC_FINAL,
                                    ConventionDefaults.REGEXP_METHOD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "static final method" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                }
                else // static
                {
                    if (Modifier.isPublic(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_METHOD_PUBLIC_STATIC,
                                    ConventionDefaults.REGEXP_METHOD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "public static method" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else if (Modifier.isProtected(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_METHOD_PROTECTED_STATIC,
                                    ConventionDefaults.REGEXP_METHOD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "protected static method" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else if (Modifier.isPrivate(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_METHOD_PRIVATE_STATIC,
                                    ConventionDefaults.REGEXP_METHOD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "private static method" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_METHOD_DEFAULT_STATIC,
                                    ConventionDefaults.REGEXP_METHOD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "static method" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                }
            }
            else // instance methods
            {
                if (Modifier.isPublic(modifierMask))
                {
                    Pattern pattern =
                        getPattern(
                            _settings.get(
                                ConventionKeys.REGEXP_METHOD_PUBLIC,
                                ConventionDefaults.REGEXP_METHOD));

	                if (
		                    !pattern.pattern().equals(EMPTY_STRING)
		                    && !pattern.matcher(name).matches())
                    {
                        _args[0] = "public method" /* NOI18N */;
                        _args[1] = name;
                        _args[2] = pattern.pattern();
                        addIssue(node, STR_NAMING_CONVENTION, _args);
                    }
                }
                else if (Modifier.isProtected(modifierMask))
                {
                    Pattern pattern =
                        getPattern(
                            _settings.get(
                                ConventionKeys.REGEXP_METHOD_PROTECTED,
                                ConventionDefaults.REGEXP_METHOD));

	                if (
		                    !pattern.pattern().equals(EMPTY_STRING)
		                    && !pattern.matcher(name).matches())
                    {
                        _args[0] = "protected method" /* NOI18N */;
                        _args[1] = name;
                        _args[2] = pattern.pattern();
                        addIssue(node, STR_NAMING_CONVENTION, _args);
                    }
                }
                else if (Modifier.isPrivate(modifierMask))
                {
                    Pattern pattern =
                        getPattern(
                            _settings.get(
                                ConventionKeys.REGEXP_METHOD_PRIVATE,
                                ConventionDefaults.REGEXP_METHOD));

	                if (
		                    !pattern.pattern().equals(EMPTY_STRING)
		                    && !pattern.matcher(name).matches())
                    {
                        _args[0] = "private method" /* NOI18N */;
                        _args[1] = name;
                        _args[2] = pattern.pattern();
                        addIssue(node, STR_NAMING_CONVENTION, _args);
                    }
                }
                else
                {
                    Pattern pattern =
                        getPattern(
                            _settings.get(
                                ConventionKeys.REGEXP_METHOD_DEFAULT,
                                ConventionDefaults.REGEXP_METHOD));

	                if (
		                    !pattern.pattern().equals(EMPTY_STRING)
		                    && !pattern.matcher(name).matches())
                    {
                        _args[0] = "method" /* NOI18N */;
                        _args[1] = name;
                        _args[2] = pattern.pattern();
                        addIssue(node, STR_NAMING_CONVENTION, _args);
                    }
                }
            }
        }
    }


    /**
     * Checks, in case the given method node represents an overriden equals method,
     * whether it substitutes an object.
     *
     * @param node a METHOD_DEF node.
     * @param method the method object.
     */
    private void checkObjectEquals(
        AST    node,
        Method method)
    {
        if (isSubstituteEqualsMethod(method))
        {
            addIssue(node, STR_DONT_SUBSTITUTE_OBJECT_EQUALS, _args);
        }
        else if (isEqualsMethod(method))
        {
            checkEqualsReturnType(node, method);
        }
    }


    /**
     * Checks whether the given package name adheres to the naming convention.
     *
     * @param node a PACKAGE_DEF node.
     */
    private void checkPackageName(AST node)
    {
        if (
            _settings.getBoolean(
                ConventionKeys.TIP_ADHERE_TO_NAMING_CONVENTION, false))
        {
            String name = JavaNodeHelper.getDottedName(JavaNodeHelper.getFirstChild(node, JavaTokenTypes.DOT));
            Pattern pattern =
                getPattern(
                    _settings.get(
                        ConventionKeys.REGEXP_PACKAGE, ConventionDefaults.REGEXP_PACKAGE));

            if (
                    !pattern.pattern().equals(EMPTY_STRING)
                    && !pattern.matcher(name).matches())
            {
                _args[0] = "Package" /* NOI18N */;
                _args[1] = name;
                _args[2] = pattern.pattern();
                addIssue(node, STR_NAMING_CONVENTION, _args);
            }
        }
    }


    /**
     * Checks whether all given parameters represents interface (base) types.
     *
     * @param node a CTOR_DEF or METHOD_DEF node.
     * @param type object representing a CTOR_DEF or METHOD_DEF.
     */
    private void checkParameters(
        AST         node,
        Constructor type)
    {
        if (!_settings.getBoolean(ConventionKeys.TIP_REFER_BY_INTERFACE, false))
        {
            return;
        }

        /**
         * @todo add check for naming convention
         */
        for (int i = 0, size = type.parameters.size(); i < size; i++)
        {
            Parameter parameter = (Parameter) type.parameters.get(i);

            if (_favorableTypes.contains(parameter.typeName))
            {
                addIssue(node, STR_REFER_BY_INTERFACE, _args);
            }
        }
    }


    /**
     * Checks whether the given return type represents an interface (base) type.
     *
     * @param node a METHOD_DEF node.
     * @param method the method object.
     */
    private void checkReturnType(
        AST    node,
        Method method)
    {
        if (!_settings.getBoolean(ConventionKeys.TIP_REFER_BY_INTERFACE, false))
        {
            return;
        }

        if (method.hasReturnType())
        {
            String returnType = method.returnType;

            if (_favorableTypes.contains(returnType))
            {
                addIssue(node, STR_REFER_BY_INTERFACE, _args);
            }
        }
    }


    /**
     * Checks whether the given String literal contains a trailing comment that informs
     * about I18N state.
     *
     * @param node a STRING_LITERAL node.
     *
     * @since 1.0b9
     */
    private void checkStringLiteral(AST node)
    {
        if (_settings.getBoolean(ConventionKeys.TIP_STRING_LITERAL_I18N, false))
        {
            JavaNode n = (JavaNode) node;

            CommonHiddenStreamToken comment = n.getCommentAfter();

            if (comment == null)
            {
                _args[0] = String.valueOf(n.getStartColumn());
                addIssue(node, STR_STRING_LITERAL_I18N, _args);
            }
            else
            {
                switch (comment.getType())
                {
                    case JavaTokenTypes.ML_COMMENT :

                        if (comment.getText().indexOf("NOI18N" /* NOI18N */) == -1)
                        {
                            _args[0] = String.valueOf(n.getStartColumn());
                            addIssue(node, STR_STRING_LITERAL_I18N, _args);
                        }

                        break;

                    default :
                        _args[0] = String.valueOf(n.getStartColumn());
                        addIssue(node, STR_STRING_LITERAL_I18N, _args);

                        break;
                }
            }
        }
    }


    /**
     * Checks whether the given constructor or method node does throw a superclass
     * exception.
     *
     * @param node a CTOR_DEF or METHOD_DEF node.
     * @param type the constructor or method object.
     */
    private void checkThrows(
        AST         node,
        Constructor type)
    {
        boolean checkThrowException =
            _settings.getBoolean(ConventionKeys.TIP_NEVER_THROW_EXCEPTION, false);
        boolean checkThrowThrowable =
            _settings.getBoolean(ConventionKeys.TIP_NEVER_THROW_THROWABLE, false);

        if (!checkThrowException && !checkThrowThrowable)
        {
            return;
        }

        if (type.hasExceptions())
        {
            switch (node.getType())
            {
                case JavaTokenTypes.METHOD_DEF :

                    // user overrides finalizer, this is OK
                    if (isFinalizeMethod((Method) type))
                    {
                        return;
                    }
            }

            List exceptions = type.exceptions;

            for (int i = 0, size = exceptions.size(); i < size; i++)
            {
                String name = (String) exceptions.get(i);

                if (checkThrowException && STR_Exception.equals(name))
                {
                    addIssue(node, STR_NEVER_THROW_EXCEPTION, _args);
                }
                else if (checkThrowThrowable && STR_Throwable.equals(name))
                {
                    addIssue(node, STR_NEVER_THROW_THROWABLE, _args);
                }
            }
        }
    }


    /**
     * Checks the given variable name (either a field or local variable) for naming
     * violations.
     *
     * @param node a VARIABLE_DEF node.
     * @param name the name of the variable.
     */
    private void checkVariableName(
        AST    node,
        String name)
    {
        if (
            _settings.getBoolean(
                ConventionKeys.TIP_ADHERE_TO_NAMING_CONVENTION, false))
        {
            if (JavaNodeHelper.isLocalVariable(node))
            {
                Pattern pattern =
                    getPattern(
                        _settings.get(
                            ConventionKeys.REGEXP_LOCAL_VARIABLE,
                            ConventionDefaults.REGEXP_LOCAL_VARIABLE));

                if (
	                    !pattern.pattern().equals(EMPTY_STRING)
	                    && !pattern.matcher(name).matches())
                {
                    _args[0] = "local variable" /* NOI18N */;
                    _args[1] = name;
                    _args[2] = pattern.pattern();
                    addIssue(node, STR_NAMING_CONVENTION, _args);
                }
            }
            else
            {
                AST modifiers = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.MODIFIERS);
                int modifierMask = JavaNodeModifier.valueOf(modifiers);

                if (Modifier.isStatic(modifierMask)) // static fields
                {
                    if (Modifier.isFinal(modifierMask)) // static final
                    {
                        if (Modifier.isPublic(modifierMask))
                        {
                            Pattern pattern =
                                getPattern(
                                    _settings.get(
                                        ConventionKeys.REGEXP_FIELD_PUBLIC_STATIC_FINAL,
                                        ConventionDefaults.REGEXP_FIELD_STATIC_FINAL));

			                if (
				                    !pattern.pattern().equals(EMPTY_STRING)
				                    && !pattern.matcher(name).matches())
                            {
                                _args[0] = "public static final field" /* NOI18N */;
                                _args[1] = name;
                                _args[2] = pattern.pattern();
                                addIssue(node, STR_NAMING_CONVENTION, _args);
                            }
                        }
                        else if (Modifier.isProtected(modifierMask))
                        {
                            Pattern pattern =
                                getPattern(
                                    _settings.get(
                                        ConventionKeys.REGEXP_FIELD_PROTECTED_STATIC_FINAL,
                                        ConventionDefaults.REGEXP_FIELD_STATIC_FINAL));

			                if (
				                    !pattern.pattern().equals(EMPTY_STRING)
				                    && !pattern.matcher(name).matches())
                            {
                                _args[0] = "protected static final field" /* NOI18N */;
                                _args[1] = name;
                                _args[2] = pattern.pattern();
                                addIssue(node, STR_NAMING_CONVENTION, _args);
                            }
                        }
                        else if (Modifier.isPrivate(modifierMask))
                        {
                            Pattern pattern =
                                getPattern(
                                    _settings.get(
                                        ConventionKeys.REGEXP_FIELD_PRIVATE_STATIC_FINAL,
                                        ConventionDefaults.REGEXP_FIELD_STATIC_FINAL));

			                if (
				                    !pattern.pattern().equals(EMPTY_STRING)
				                    && !pattern.matcher(name).matches())
                            {
                                _args[0] = "private static final field" /* NOI18N */;
                                _args[1] = name;
                                _args[2] = pattern.pattern();
                                addIssue(node, STR_NAMING_CONVENTION, _args);
                            }
                        }
                        else
                        {
                            Pattern pattern =
                                getPattern(
                                    _settings.get(
                                        ConventionKeys.REGEXP_FIELD_DEFAULT_STATIC_FINAL,
                                        ConventionDefaults.REGEXP_FIELD_STATIC_FINAL));

			                if (
				                    !pattern.pattern().equals(EMPTY_STRING)
				                    && !pattern.matcher(name).matches())
                            {
                                _args[0] = "static final field" /* NOI18N */;
                                _args[1] = name;
                                _args[2] = pattern.pattern();
                                addIssue(node, STR_NAMING_CONVENTION, _args);
                            }
                        }
                    }
                    else // static
                    {
                        if (Modifier.isPublic(modifierMask))
                        {
                            Pattern pattern =
                                getPattern(
                                    _settings.get(
                                        ConventionKeys.REGEXP_FIELD_PUBLIC_STATIC,
                                        ConventionDefaults.REGEXP_FIELD));

			                if (
				                    !pattern.pattern().equals(EMPTY_STRING)
				                    && !pattern.matcher(name).matches())
                            {
                                _args[0] = "public static field" /* NOI18N */;
                                _args[1] = name;
                                _args[2] = pattern.pattern();
                                addIssue(node, STR_NAMING_CONVENTION, _args);
                            }
                        }
                        else if (Modifier.isProtected(modifierMask))
                        {
                            Pattern pattern =
                                getPattern(
                                    _settings.get(
                                        ConventionKeys.REGEXP_FIELD_PROTECTED_STATIC,
                                        ConventionDefaults.REGEXP_FIELD));

			                if (
				                    !pattern.pattern().equals(EMPTY_STRING)
				                    && !pattern.matcher(name).matches())
                            {
                                _args[0] = "protected static field" /* NOI18N */;
                                _args[1] = name;
                                _args[2] = pattern.pattern();
                                addIssue(node, STR_NAMING_CONVENTION, _args);
                            }
                        }
                        else if (Modifier.isPrivate(modifierMask))
                        {
                            Pattern pattern =
                                getPattern(
                                    _settings.get(
                                        ConventionKeys.REGEXP_FIELD_PRIVATE_STATIC,
                                        ConventionDefaults.REGEXP_FIELD));

			                if (
				                    !pattern.pattern().equals(EMPTY_STRING)
				                    && !pattern.matcher(name).matches())
                            {
                                _args[0] = "private static field" /* NOI18N */;
                                _args[1] = name;
                                _args[2] = pattern.pattern();
                                addIssue(node, STR_NAMING_CONVENTION, _args);
                            }
                        }
                        else
                        {
                            Pattern pattern =
                                getPattern(
                                    _settings.get(
                                        ConventionKeys.REGEXP_FIELD_DEFAULT_STATIC,
                                        ConventionDefaults.REGEXP_FIELD));

			                if (
				                    !pattern.pattern().equals(EMPTY_STRING)
				                    && !pattern.matcher(name).matches())
                            {
                                _args[0] = "static field" /* NOI18N */;
                                _args[1] = name;
                                _args[2] = pattern.pattern();
                                addIssue(node, STR_NAMING_CONVENTION, _args);
                            }
                        }
                    }
                }
                else // instance fields
                {
                    if (Modifier.isPublic(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_FIELD_PUBLIC,
                                    ConventionDefaults.REGEXP_FIELD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "public field" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else if (Modifier.isProtected(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_FIELD_PROTECTED,
                                    ConventionDefaults.REGEXP_FIELD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "protected field" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else if (Modifier.isPrivate(modifierMask))
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_FIELD_PRIVATE,
                                    ConventionDefaults.REGEXP_FIELD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "private field" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                    else
                    {
                        Pattern pattern =
                            getPattern(
                                _settings.get(
                                    ConventionKeys.REGEXP_FIELD_DEFAULT,
                                    ConventionDefaults.REGEXP_FIELD));

		                if (
			                    !pattern.pattern().equals(EMPTY_STRING)
			                    && !pattern.matcher(name).matches())
                        {
                            _args[0] = "field" /* NOI18N */;
                            _args[1] = name;
                            _args[2] = pattern.pattern();
                            addIssue(node, STR_NAMING_CONVENTION, _args);
                        }
                    }
                }
            }
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Represents a CTOR_DEF node.
     */
    private static class Constructor
    {
        public static final String TYPE_VOID = "void" /* NOI18N */;
        List exceptions = Collections.EMPTY_LIST; // List of <String>
        List parameters = Collections.EMPTY_LIST; // List of <Parameter>
        String name;
        int modifierMask;

        public Constructor(AST node)
        {
            for (
                AST child = node.getFirstChild(); child != null;
                child = child.getNextSibling())
            {
                switch (child.getType())
                {
                    case JavaTokenTypes.MODIFIERS :
                        this.modifierMask = JavaNodeModifier.valueOf(child);

                        break;

                    case JavaTokenTypes.IDENT :
                        this.name = child.getText();

                        break;

                    case JavaTokenTypes.PARAMETERS :

                        AST parameter = child.getFirstChild();

                        if (parameter != null)
                        {
                            this.parameters = new ArrayList(5);

                            for (
                                AST pchild = parameter; pchild != null;
                                pchild = pchild.getNextSibling())
                            {
                                this.parameters.add(new Parameter(pchild));
                            }
                        }

                        break;

                    case JavaTokenTypes.LITERAL_throws :
                        this.exceptions = new ArrayList(4);

                        for (
                            AST exception = child.getFirstChild(); exception != null;
                            exception = exception.getNextSibling())
                        {
                            exceptions.add(exception.getText());
                        }

                        break;
                }
            }
        }


        private Constructor()
        {
        }

        public List getExceptions()
        {
            return this.exceptions;
        }


        public String getName()
        {
            return this.name;
        }


        public int getParameterCount()
        {
            return this.parameters.size();
        }


        public List getParameters()
        {
            return this.parameters;
        }


        public boolean hasExceptions()
        {
            return !this.exceptions.isEmpty();
        }


        public boolean hasParameters()
        {
            return !this.parameters.isEmpty();
        }
    }


    /**
     * Represents a METHOD_DEF node.
     */
    private static final class Method
        extends Constructor
    {
        String returnType;
        boolean isAbstract = true;

        public Method(AST node)
        {
            for (
                AST child = node.getFirstChild(); child != null;
                child = child.getNextSibling())
            {
                switch (child.getType())
                {
                    case JavaTokenTypes.MODIFIERS :
                        this.modifierMask = JavaNodeModifier.valueOf(child);

                        break;

                    case JavaTokenTypes.TYPE :
                        this.returnType = child.getFirstChild().getText();

                        break;

                    case JavaTokenTypes.IDENT :
                        this.name = child.getText();

                        break;

                    case JavaTokenTypes.PARAMETERS :

                        AST parameter = child.getFirstChild();

                        if (parameter != null)
                        {
                            this.parameters = new ArrayList(5);

                            for (
                                AST pchild = parameter; pchild != null;
                                pchild = pchild.getNextSibling())
                            {
                                this.parameters.add(new Parameter(pchild));
                            }
                        }

                        break;

                    case JavaTokenTypes.LITERAL_throws :
                        this.exceptions = new ArrayList(4);

                        for (
                            AST exception = child.getFirstChild(); exception != null;
                            exception = exception.getNextSibling())
                        {
                            exceptions.add(exception.getText());
                        }

                        break;

                    case JavaTokenTypes.SLIST :
                        this.isAbstract = false;

                        break;
                }
            }
        }

        public boolean isAbstract()
        {
            return this.isAbstract;
        }


        public String getReturnType()
        {
            return this.returnType;
        }


        public boolean hasReturnType()
        {
            return !TYPE_VOID.equals(this.returnType);
        }
    }


    /**
     * Represents a PARAMETER node.
     */
    private static final class Parameter
    {
        String name;
        String typeName;
        int modifierMask;

        public Parameter(AST node)
        {
            for (
                AST child = node.getFirstChild(); child != null;
                child = child.getNextSibling())
            {
                switch (child.getType())
                {
                    case JavaTokenTypes.MODIFIERS :
                        this.modifierMask = JavaNodeModifier.valueOf(child);

                        break;

                    case JavaTokenTypes.TYPE :
                        this.typeName = child.getFirstChild().getText();

                        break;

                    case JavaTokenTypes.IDENT :
                        this.name = child.getText();

                        break;
                }
            }
        }

        public int getModifierMask()
        {
            return this.modifierMask;
        }


        public String getName()
        {
            return this.name;
        }


        public String getTypeName()
        {
            return this.typeName;
        }


        public String toString()
        {
            StringBuffer buf = new StringBuffer(50);
            buf.append(Modifier.toString(this.modifierMask));
            buf.append(' ');
            buf.append(typeName);
            buf.append(' ');
            buf.append(name);

            return buf.toString().trim();
        }
    }
}
