/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import antlr.ASTPair;
import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaNodeFactory;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.ImportPolicy;
import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.util.StringHelper;

import org.apache.log4j.Level;

/**
 * Transformation which replaces <em>single-type-import declarations</em> with their
 * <em>type-import-on-demand</em> counterpart or vice versa. Implements also the sorting
 * logic for the import declarations.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 *
 * @see de.hunsicker.jalopy.language.ClassRepository
 */
final class ImportTransformation
    extends TreeWalker
    implements Transformation
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The natural order for nodes: as they appeared in the source. */
    private static final Comparator COMP_LINE = new NodeLineComparator();

    /** Compares on-demand and single-type import nodes by package names. */
    private static final Comparator COMP_ON_DEMAND_SINGLE =
        new NodeOnDemandSingleComparator();

    /** Compares two nodes lexicographically. */
    private static final Comparator COMP_TEXT = new NodeStringComparator();

    /** Indicates the default package. */
    private static final int DEFAULT_PACKAGE = -99;

    /** The empty node array. */
    private static final JavaNode[] EMPTY_NODE_ARRAY = new JavaNode[0];

    /** Marker we use to mark the start of new package in the type array. */
    private static final String MARKER = "#" /* NOI18N */;

    /** The empty string. */
    private static final String EMPTY_STRING = "" /* NOI18N */.intern();

    /** The empty string array. */
    // TODO private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String PACKAGE_JAVA_LANG = "java.lang." /* NOI18N */;
    private static final String STAR = "*" /* NOI18N */;
    static final String DOT = "." /* NOI18N */;
    private static final String DELIMETER_ENTRY = "|" /* NOI18N */;
    private static final Object[] _args = new Object[4];

    //~ Instance variables ---------------------------------------------------------------

    private final ImportNodeComparator COMP_IMPORT = new ImportNodeComparator();

    /** The first CLASS_DEF node, if any. */
    private AST _class;

    /** The package node of the tree, if any. */
    private JavaNode _packageNameNode;

    /** Holds the on-demand (wildcard) import declarations. */
    private List _onDemandImports = new ArrayList(); // List of <JavaNode>

    /** Holds the reported qualified identifiers. */
    private final List _qualIdents; // List of <String>

    /** The root node of the tree. */
    private AST _root;

    /** Holds the single-type import declarations. */
    private List _singleTypeImports = new ArrayList(); // List of <JavaNode>

    /** Holds the reported unqualified identifiers. */
    private final List _unqualIdents; // List of <String>

    /** The name of the file currently being processed. */
    private String _filename;

    /** The package name of the tree. */
    private String _packageName = EMPTY_STRING;

    /** Should the nodes be sorted? */
    private boolean _sortImports = true;

    /** The line number of the first import declaration. */
    private int _line;
    private JavaNodeFactory _factory = null;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ImportTransformation object.
     *
     * @param qualIdents qualified identifiers as reported by the parser.
     * @param unqualIdents unqualified identifiers as reported by the parser.
     */
    public ImportTransformation(
        final List qualIdents,
        final List unqualIdents,
        JavaNodeFactory factory)
    {
        _qualIdents = qualIdents;
        _unqualIdents = unqualIdents;
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
            Convention settings = Convention.getInstance();

            ImportPolicy importPolicy =
                ImportPolicy.valueOf(
                    settings.get(
                        ConventionKeys.IMPORT_POLICY, ConventionDefaults.IMPORT_POLICY));
            boolean expand = importPolicy == ImportPolicy.EXPAND;
            boolean collapse = importPolicy == ImportPolicy.COLLAPSE;
            _sortImports =
                settings.getBoolean(
                    ConventionKeys.IMPORT_SORT, ConventionDefaults.IMPORT_SORT);

            // we only can expand/collapse if our repository is available
            if (!ClassRepository.getInstance().isEmpty())
            {
                if (expand)
                {
                    walk(tree);
                    expand(); // takes care of sorting too
                }
                else if (collapse)
                {
                    walk(tree);
                    collapse(); // takes care of sorting too
                }
                else if (_sortImports) // only sort
                {
                    walk(tree);
                    updateTree(_onDemandImports, _singleTypeImports);
                }
            }
            else if (_sortImports) // only sort
            {
                walk(tree);
                updateTree(_onDemandImports, _singleTypeImports);
            }
        }
        finally
        {
            cleanup();
        }
    }


    /**
     * Callback that will be called for every IMPORT node found.
     *
     * <p>
     * Adds the node to one of our collections (either single-type or on-demand).
     * </p>
     *
     * @param node an IMPORT node of the tree.
     */
    public void visit(AST node)
    {
        String identifier = JavaNodeHelper.getDottedName(node.getFirstChild());
        JavaNode importNode = (JavaNode) node;

        if (_line == 0)
        {
            _line = importNode.getStartLine();
        }

        if (identifier.endsWith(STAR))
        {
            // build the package name and update the node text (to make
            // accessing the package name easy)
            String packageName = identifier.substring(0, identifier.length() - 2);
            importNode.setText(packageName);

            // remove duplicates
            // (note that we compare by JavaNode.equals(JavaNode) so we really
            // only compare the text of the two nodes!)
            boolean contains = false;
            JavaNode dup = null;
            for(Iterator i = _onDemandImports.iterator();i.hasNext() && !contains;) {
                dup = (JavaNode) i.next();
                contains = dup.getText().equals(importNode.getText());
            }
            if (contains)
            {
                _args[0] = _root.getText();
                _args[1] = new Integer(importNode.getStartLine());
                _args[2] = identifier;
                _args[3] = new Integer(dup.getStartLine());

                Loggers.TRANSFORM.l7dlog(
                    Level.INFO, "TRANS_IMP_REMOVE_DUPLICATE", _args, null);
            }
            else
            {
                _onDemandImports.add(importNode);
            }
        }
        else // single-type import
        {
            // update the node text (to make accessing the path easy)
            importNode.setText(identifier);

            // remove duplicates
            // (note that we compare by JavaNode.equals(JavaNode) so we really
            // only compare the text of the two nodes!)
            if (_singleTypeImports.contains(importNode))
            {
                _args[0] = _root.getText();
                _args[1] = new Integer(importNode.getStartLine());
                _args[2] = identifier;
                _args[3] =
                    new Integer(
                        ((JavaNode) _singleTypeImports.get(
                            _singleTypeImports.indexOf(importNode))).getStartLine());

                Loggers.TRANSFORM.l7dlog(
                    Level.INFO, "TRANS_IMP_REMOVE_DUPLICATE", _args, null);
            }
            else
            {
                _singleTypeImports.add(importNode);
            }
        }
    }


    /**
     * Walks over the given node. Only links in children for IMPORT nodes. Walking ends
     * after the last IMPORT node.
     *
     * @param node a node of the tree.
     */
    protected void walkNode(AST node)
    {
        switch (node.getType())
        {
            case JavaTokenTypes.ROOT :
                _root = node;
                _filename = node.getText();

                AST first = node.getFirstChild();

                if (first != null)
                {
                    walkNode(first);
                }

                break;

            case JavaTokenTypes.PACKAGE_DEF :
            {
                _packageName = JavaNodeHelper.getDottedName(node.getFirstChild());
                _packageNameNode = (JavaNode) node;

                AST next = node.getNextSibling();

                if (next != null)
                {
                    walkNode(next);
                }

                break;
            }

            case JavaTokenTypes.IMPORT :
            case JavaTokenTypes.STATIC_IMPORT:
            {
                visit(node);

                AST next = node.getNextSibling();

                if (next != null)
                {
                    walkNode(next);
                }

                break;
            }

            case JavaTokenTypes.SEMI :
                walkNode(node.getNextSibling());

                break;
            
            // Annotation's and enums live outside the class also now
            case JavaTokenTypes.ENUM_DEF :
            case JavaTokenTypes.ANNOTATION_DEF :
                _class = node;
            
            break;
            case JavaTokenTypes.CLASS_DEF :
            case JavaTokenTypes.INTERFACE_DEF :
                _class = node;

                // remove all import nodes from the tree
                // either from the package name node...
                if (_packageNameNode != null)
                {
                    _packageNameNode.setNextSibling(node);
                }
                else // or the root node
                {
                    _root.setFirstChild(node);
                }

                // we're done after the last import
                return;

            default :
                break;
        }
    }


    /**
     * Determines whether the given import declaration has the same package name as the
     * given package.
     *
     * @param name import declaration to check.
     * @param packageName package name to check against.
     *
     * @return <code>true</code> if the package name of the import declaration is the
     *         same as the given package name.
     */
    private boolean isDefaultPackage(
        String name,
        String packageName)
    {
        boolean result = false;

        if (packageName != null)
        {
            int pos = name.lastIndexOf('.');

            if (pos != -1)
            {
                result = name.substring(0, pos).equals(packageName);
            }
        }
        else
        {
            result = name.indexOf('.') == -1;
        }

        return result;
    }


    /**
     * Returns the import-type-on-demand declarations which probably can be expanded to
     * several single-type declarations.
     *
     * @param onDemandImports found on-demand import declaration nodes.
     *
     * @return on-demand import nodes which can be expanded. Returns an empty list if no
     *         such imports where found.
     */
    private List getExpandable(final List onDemandImports)
    {
        List result = new ArrayList(onDemandImports.size());

        // if we find it in the repository, we know it is expandable
        for (int i = 0, size = onDemandImports.size(); i < size; i++)
        {
            JavaNode node = (JavaNode) onDemandImports.get(i);
            result.add(node);

            /**
             * @todo use a comparator that performs partial string matching
             */

            /*
               if (Arrays.binarySearch(repository, next, COMP) > -1)
               {
                   result.add(node);
               }
             */
        }

        // add the default package (necessary to detect conflicts)
        JavaNode defaultPckg = _factory.create(DEFAULT_PACKAGE, 1, DEFAULT_PACKAGE, 2);
        defaultPckg.setText(_packageName);
        defaultPckg.setType(JavaTokenTypes.IMPORT);
        result.add(defaultPckg);

        return result;
    }


    static int getIndex(
        List   identifiers,
        String packageName)
    {
        for (int i = 0, size = identifiers.size(); i < size; i++)
        {
            if (packageName.startsWith((String) identifiers.get(i)))
            {
                return i;
            }
        }

        return -1;
    }


    /**
     * Determines whether the given import declaration imports the standard Java package
     * (<code>java.lang.</code>).
     *
     * @param name import declaration to check.
     *
     * @return <code>true</code> if the import declaration imports the
     *         <code>java.lang</code> package.
     */
    private boolean isLangPackage(String name)
    {
        return (name.startsWith(PACKAGE_JAVA_LANG) && (name.lastIndexOf('.') == 9));
    }


    /**
     * Returns a list with all type names of the given package (without package name).
     *
     * @param packageName a package name.
     *
     * @return list with all type names of the given package. If
     *         <code><em>packageName</em> == null</code> or the given package is not
     *         contained in the repository, an empty list will be returned.
     */
    private List getPackageTypes(String packageName)
    {
        if (packageName == null)
        {
            return Collections.EMPTY_LIST;
        }

        String[] content = ClassRepository.getInstance().getContent();

        if (content.length == 0)
        {
            return Collections.EMPTY_LIST;
        }

        // search the start marker for the package (that is why we have
        // added the '#' marker!)
        int startOffset = Arrays.binarySearch(content, packageName + '#');
        List result = new ArrayList(20);

        // we found a marker...
        if (startOffset > -1)
        {
            int depth = StringHelper.occurs('.', packageName) + 1;

            // so search from the next entry on
            for (int i = startOffset + 1; i < content.length; i++)
            {
                // as long as the type starts with the package name
                if (content[i].startsWith(packageName))
                {
                    // and resides in the exact same package, not a
                    // sub-package
                    //
                    //   java.util      --> OK
                    //   java.util.jar  --> SKIP
                    if (StringHelper.occurs('.', content[i]) == depth)
                    {
                        result.add(StringHelper.getClassName(content[i]));
                    }
                }
                else
                {
                    break;
                }
            }
        }

        return result;
    }


    /**
     * Returns all possible single-type import declarations for the given list of
     * identifiers.
     *
     * @param identifiers found unqualified identifiers.
     * @param singleTypeImports found single-type import declarations.
     *
     * @return possible single-type import declarations. Returns an empty list if all
     *         identifiers are already imported by single-type import declarations.
     */
    private List getPossibleSingleTypeImports(
        final List identifiers,
        final List singleTypeImports)
    {
        int length = identifiers.size();
        List result = new ArrayList(length);

        // we first build the list with all identifiers
        for (int i = 0; i < length; i++)
        {
            result.add(identifiers.get(i));
        }

        // and afterwards remove those that are already single-typed
        for (int i = 0, size = singleTypeImports.size(); i < size; i++)
        {
            for (int j = 0; j < length; j++)
            {
                JavaNode node = (JavaNode) singleTypeImports.get(i);

                if (node.getText().endsWith('.' + (String) identifiers.get(j)))
                {
                    result.remove(identifiers.get(j));
                }
            }
        }

        return result;
    }


    /**
     * Returns a list with the qualified identifiers which are possibly type names.
     *
     * @param identifiers identifiers as reported by the parser.
     *
     * @return list with the possible type names.
     */
    /*
    
    TODO private List getPossibleTypes(final List identifiers)
    {
        String[] contents = ClassRepository.getInstance().getContent();
        List result = new ArrayList();

        for (int i = 0, size = identifiers.size(); i < size; i++)
        {
            String ident = (String) identifiers.get(i);

            
            // todo use partial string matching indexOf() ???
            
            if (Arrays.binarySearch(contents, ident) > -1)
            {
                result.add(ident);
            }
        }

        return result;
    }*/


    /**
     * Determines whether the given unqualified type identifier is actually used within a
     * source file.
     *
     * @param identifier unqualified type identifier.
     * @param types DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @since 1.0b9
     */
    private boolean isUsed(
        String identifier,
        List   types)
    {
        for (int i = 0, size = types.size(); i < size; i++)
        {
            String type = (String) types.get(i);

            if (type.equals(identifier))
            {
                return true;
            }
        }

        for (int i = 0, size = _qualIdents.size(); i < size; i++)
        {
            String type = StringHelper.getClassName((String) _qualIdents.get(i));

            if (type.equals(identifier))
            {
                return true;
            }
        }

        for (int i = 0, size = _unqualIdents.size(); i < size; i++)
        {
            String type = (String) _unqualIdents.get(i);

            if (type.endsWith(identifier))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Adds all comments from <em>source</em> to <em>target</em>.
     *
     * @param source DOCUMENT ME!
     * @param target DOCUMENT ME!
     *
     * @todo implement for CommonHiddenStreamToken
     */
    private void addComments(
        JavaNode source,
        JavaNode target)
    {
        // TODO Should this be removed ?
        /*if (source.hasCommentsBefore())
        {
            if (target.hasCommentsBefore())
            {
                List comments = target.getCommentsBefore();
                comments.addAll(source.getCommentsBefore());
            }
            else
            {
                target.setCommentsBefore(source.getCommentsBefore());
            }
        }

        if (source.hasCommentsAfter())
        {
            if (target.hasCommentsAfter())
            {
                List comments = target.getCommentsAfter();
                comments.addAll(source.getCommentsAfter());
            }
            else
            {
                target.setCommentsAfter(source.getCommentsAfter());
            }
        }*/
    }


    /**
     * Performs cleanup.
     */
    private void cleanup()
    {
        _onDemandImports.clear();
        _singleTypeImports.clear();
        _packageName = EMPTY_STRING;
        _packageNameNode = null;
        _class = null;
        _root = null;
        _filename = null;
        _line = 0;
    }

    private void collapse()
    {
        if (_singleTypeImports.size() > 0)
        {

        List singleTypeImports = _singleTypeImports;

        // this will hold all on-demand imports (both original and newly collapsed ones)
        List newOnDemandImports = new ArrayList(singleTypeImports.size());

        // add the original on-demand imports
        newOnDemandImports.addAll(_onDemandImports);

        // add the default package (needed to detect conflicts)
        JavaNode defaultPackage = (JavaNode) _factory.create(JavaTokenTypes.IMPORT, _packageName);
        defaultPackage.setStartLine(DEFAULT_PACKAGE);
        newOnDemandImports.add(defaultPackage);

        // add the to-be-collapsed single-type imports
        for (int i = 0, size = singleTypeImports.size(); i < size; i++)
        {
            JavaNode singleType = (JavaNode) singleTypeImports.get(i);
            String packageName = StringHelper.getPackageName(singleType.getText());

            // no package name, no collapsing!
            if (EMPTY_STRING.equals(packageName))
            {
                continue;
            }

            // create an on-demand import out of the single-type import
            JavaNode onDemand = (JavaNode) _factory.create(JavaTokenTypes.IMPORT, packageName);
            onDemand.setFirstChild(singleType.getFirstChild());

            if (singleType.hasCommentsBefore())
            {
                /**
                 * @todo implement
                 */

                /*onDemand.setCommentsBefore(
                        new ArrayList(singleType.getCommentsBefore()));
                singleType.setCommentsBefore(null);*/
            }

            if (singleType.hasCommentsAfter())
            {
                /**
                 * @todo implement
                 */

                /*onDemand.setCommentsAfter(
                        new ArrayList(singleType.getCommentsAfter()));
                singleType.setCommentsAfter(null);*/
            }

            if (!newOnDemandImports.contains(onDemand))
            {
                newOnDemandImports.add(onDemand);
            }
            else
            {
                // the package name is already added but we don't want to loose
                // any comments so we have to check
                addComments(
                    onDemand,
                    (JavaNode) newOnDemandImports.get(
                        newOnDemandImports.indexOf(onDemand)));
            }
        }

        Map allTypes = new HashMap(newOnDemandImports.size());
        List retainedOnDemandImports = new ArrayList();
        JavaNode template =  (JavaNode) _factory.create(JavaTokenTypes.IMPORT, EMPTY_STRING);

        // for every package that should be collapsed, build a list
        // with the contained type names
        for (int j = 0, packages = newOnDemandImports.size(); j < packages; j++)
        {
            JavaNode path = (JavaNode) newOnDemandImports.get(j);

            // build the list with the types of the package library
            // and add it to the types store with all known types
            List packageTypes = getPackageTypes(path.getText());
            allTypes.put(path.getText(), packageTypes);
        }

        Set conflicts = new HashSet(5);

        List defaultTypes = getPackageTypes(_packageName);

        // now that we know about all types we look if a type is contained
        // in several packages which would make a conflict
        for (
            Iterator firstPackages = allTypes.entrySet().iterator();
            firstPackages.hasNext();)
        {
            Map.Entry firstPackageData = (Map.Entry) firstPackages.next();
            String firstPackageName = (String) firstPackageData.getKey();
            List firstPackageTypes = (List) firstPackageData.getValue();

            for (
                Iterator secondPackages = allTypes.entrySet().iterator();
                secondPackages.hasNext();)
            {
                Map.Entry secondPackageData = (Map.Entry) secondPackages.next();
                String secondPackageName = (String) secondPackageData.getKey();

                // if the package is already detected as being in conflict with
                // another, we can savely skip it
                if (conflicts.contains(firstPackageName))
                {
                    continue;
                }

                // no sense in checking the same package for conflicts
                if (secondPackageName.equals(firstPackageName))
                {
                    continue;
                }

                List secondPackageTypes = (List) secondPackageData.getValue();

                for (int i = 0, size = firstPackageTypes.size(); i < size; i++)
                {
                    String type = (String) firstPackageTypes.get(i);

                    if (
                        !type.endsWith(MARKER) && secondPackageTypes.contains(type)
                        && isUsed(type, defaultTypes))
                    {
                        conflicts.add(firstPackageName);
                        template.setText(firstPackageName);

                        int index = 0;

                        if ((index = newOnDemandImports.indexOf(template)) > -1)
                        {
                            JavaNode importNode = (JavaNode)newOnDemandImports.remove(index);

                            // if this is an original on-demand import we must
                            // retain it in the output
                            if (_onDemandImports.contains(template))
                            {
                                importNode.setText(template.getText());
                                importNode.setNextSibling(null);
                                importNode.setPreviousSibling(null);
                                retainedOnDemandImports.add(importNode);
                            }
                            else
                            {
                                if (Loggers.TRANSFORM.isDebugEnabled())
                                {
                                    Loggers.TRANSFORM.debug(
                                        _filename + ":0:0: cannot unqualify "
                                        + firstPackageName + " due to conflict between "
                                        + firstPackageName + '.' + type + " and "
                                        + secondPackageName + '.' + type + " (Check 3)");
                                }
                            }
                        }
                    }
                }
            }
        }

        newOnDemandImports.remove(defaultPackage);
        Collections.sort(newOnDemandImports, COMP_TEXT);

        for (int i = 0, size = singleTypeImports.size(); i < size; i++)
        {
            JavaNode node = (JavaNode) singleTypeImports.get(i);

            // finding the node's package name means we are
            // able to collapse the node's package...
            if (
                Collections.binarySearch(newOnDemandImports, node, COMP_ON_DEMAND_SINGLE) > -1)
            {
                // therefore we remove it from the list with the single-types
                JavaNode singleType = (JavaNode) singleTypeImports.remove(i);
                size--;
                i--;

                singleType.setFirstChild(null);
                singleType.setParent(null);
                singleType.setPreviousSibling(null);
                singleType.setNextSibling(null);
            }

            /*_args[0] = _root.getText();
            _args[1] = new Integer(node.startLine);
            _args[2] = node.text;

            Loggers.TRANSFORM.l7dlog(
            Level.INFO, "TRANS_IMP_COLLAPSE_SINGLE_TYPE", _args, null);*/
        }

        newOnDemandImports.addAll(retainedOnDemandImports);
        updateTree(newOnDemandImports, singleTypeImports);
        }
        else
        {
            updateTree(_onDemandImports, Collections.EMPTY_LIST);
        }
    }

    /**
     * Updates the given unqualified import node to .
     *
     * @param node a Java identifier.
     *
     * @return new IMPORT node.
     */
    private JavaNode createImportNode(JavaNode node)
    {
     //   List parts = 
//		new Vector(Arrays.asList(node.text.split("/([.])/" /* NOI18N */)));
        
        // TODO Check to see if this can be better optimized...
        String identifier = node.getText();
        List parts = new ArrayList(8);

        int endOffset = -1;
        int startOffset = 0;

        // split the identifier into parts
        while ((endOffset = identifier.indexOf('.', startOffset)) > -1)
        {
            parts.add(identifier.substring(startOffset, endOffset));
            parts.add(DOT);
            startOffset = endOffset + 1;
        }

        parts.add(identifier.substring(startOffset));

        ASTPair curAST = new ASTPair();

        // add the childs
        AST last = _factory.create(JavaTokenTypes.IDENT, (String) parts.remove(0));
        _factory.addASTChild(curAST, last);

        for (int i = 0, size = parts.size(); i < size; i++)
        {
            String next = (String) parts.remove(0);

            if (next.equals(DOT))
            {
                last = _factory.create(JavaTokenTypes.DOT, next);
                _factory.makeASTRoot(curAST, last);
            }
            else
            {
                last = _factory.create(JavaTokenTypes.IDENT, next);
                _factory.addASTChild(curAST, last);
            }
        }

        JavaNode identifierNode = (JavaNode) node.getFirstChild();
        JavaNode semi = (JavaNode) identifierNode.getNextSibling();

        JavaNode newIdentifierNode = (JavaNode) curAST.root;
        newIdentifierNode.setNextSibling(semi);
        newIdentifierNode.setParent(node);

        semi.setPreviousSibling(newIdentifierNode);
        node.setFirstChild(newIdentifierNode);

        // don't leave any old pointers left
        identifierNode.setParent(null);
        identifierNode.setPreviousSibling(null);

        _line++;

        return node;
    }


    /**
     * Adds the given imports to the tree.
     *
     * @param imports qualified IMPORT nodes.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    private void createImportNodes(JavaNode[] imports)
    {
        // nothing to do
        if (imports.length == 0)
        {
            return;
        }

        //Convention settings = Convention.getInstance();
        ImportPolicy importPolicy =
            ImportPolicy.valueOf(
                Convention.getInstance().get(
                    ConventionKeys.IMPORT_POLICY, ConventionDefaults.IMPORT_POLICY));
        boolean report = importPolicy == ImportPolicy.EXPAND;
        String filename = _root.getText();
        JavaNode node = _packageNameNode;
        boolean showWarnings = !ClassRepository.getInstance().isEmpty();

        // create the first import node
        for (int i = 0; (imports.length > 0) && (i < 1); i++)
        {
            JavaNode imp = createImportNode(imports[i]);

            switch (imp.getType())
            {
                case JavaTokenTypes.IMPORT :
                case JavaTokenTypes.STATIC_IMPORT :
                    break;

                default :
                    throw new RuntimeException("" + imp);
            }

            if (report && imports[i].getText().endsWith(STAR))
            {
                _args[0] = filename;
                _args[1] = new Integer(imports[i].getStartLine());
                _args[2] = imports[i].getText();

                if (showWarnings)
                {
                    // we couldn't resolve the import, spit out warning
                    Loggers.TRANSFORM.l7dlog(
                        Level.WARN, "TRANS_IMP_NOT_POSSIBLE", _args, null);
                }
            }

            // if the file contains no package info, use the root node of the
            // tree
            if (node == null)
            {
                _root.setFirstChild(imp);
                imp.setParent((JavaNode) _root);
                imp.setPreviousSibling(imp.getParent());
                _packageNameNode = imp.getPreviousSibling();
            }
            else
            {
                node.setNextSibling(imp);
                imp.setPreviousSibling(node);
                imp.setParent ( _packageNameNode);
            }

            node = imp;
        }

        for (int i = 1; i < imports.length; i++)
        {
            JavaNode imp = createImportNode(imports[i]);

            switch (imp.getType())
            {
                case JavaTokenTypes.IMPORT :
                case JavaTokenTypes.STATIC_IMPORT :
                    break;

                default :
                    throw new RuntimeException("2 " + imp);
            }

            if (report && imports[i].getText().endsWith(STAR))
            {
                _args[0] = filename;
                _args[1] = new Integer(imports[i].getStartLine());
                _args[2] = imports[i].getText();

                if (showWarnings)
                {
                    Loggers.TRANSFORM.l7dlog(
                        Level.WARN, "TRANS_IMP_NOT_POSSIBLE", _args, null);
                }
            }

            node.setNextSibling(imp);
            imp.setPreviousSibling(node);
            imp.setParent(node);
            node = imp;
        }

        node.setNextSibling(_class);
    }


    private List decodeGroupingInfo(String info)
    {
        List result = new ArrayList();

        for (
            StringTokenizer tokens = new StringTokenizer(info, DELIMETER_ENTRY);
            tokens.hasMoreElements();)
        {
            String pair = tokens.nextToken();
            String name = pair.substring(0, pair.indexOf(':'));

            result.add(name);
        }

        return result;
    }


    /**
     * Tries to expand all on-demand import declarations.
     *
     * <p>
     * Duplicate and obsolete import declarations will be removed.
     * </p>
     */
    private void expand()
    {
        List expandableImports = getExpandable(_onDemandImports);

        // we must use all identifiers reported because the list of
        // the qualified identifiers contains also the inner class
        // identifiers (e.g. de.hunsicker.jalopy.Jalopy and ClassRepositoryEntry.Info)
        List identifiers = new ArrayList(_unqualIdents);

        /**
         * @todo use getPossibleTypes()
         */
        identifiers.addAll(_qualIdents);

        List expandedImports =
            expandImports(identifiers, _singleTypeImports, expandableImports);

        // add already single-typed to the newly expanded ones
        expandedImports.addAll(
            removeObsoleteImports(_singleTypeImports, _qualIdents, _unqualIdents));

        // create the list with those imports that weren't expandable
        List unexpandedImports = _onDemandImports;
        unexpandedImports.removeAll(expandableImports);

        // and finally update the AST
        updateTree(unexpandedImports, expandedImports);
    }


    /**
     * Tries to resolve all unqualified import statements.
     *
     * @param identifiers_ unqualified identifiers found.
     * @param singleTypeImports qualified imports.
     * @param expandableImports imports which can be resolved, i.e. their path was found
     *        in the repository.
     *
     * @return expanded import statements. Returns an empty list if nothing was expanded.
     */
    private List expandImports(
        final List identifiers_,
        final List singleTypeImports,
        final List expandableImports)
    {
        // we're only interested in the identifiers which aren't already
        // imported via single-typed imports
        List identifiers = getPossibleSingleTypeImports(identifiers_, singleTypeImports);

        if (identifiers.isEmpty())
        {
            // we cannot return Collections.EMPTY_LIST because we later
            // add elements to it
            return new ArrayList();
        }

        StringBuffer buf = new StringBuffer(50);
        String defaultPackageName = _packageName;
        JavaNode template =  (JavaNode) _factory.create(JavaTokenTypes.IMPORT, EMPTY_STRING);
        String[] repository = ClassRepository.getInstance().getContent();
        List result = new ArrayList(20);
        Map conflicts = new HashMap(20);

        // we first build a list with the possible single-type import declarations
        for (int i = 0, size = expandableImports.size(); i < size; i++)
        {
            for (int j = 0, tempSize = identifiers.size(); j < tempSize; j++)
            {
                String unresolvedIdent = (String) identifiers.get(j);
                JavaNode resolvableImport = (JavaNode) expandableImports.get(i);

                // construct a single-type import out of
                // the package name from the on-demand import...
                buf.append(resolvableImport.getText());
                buf.append('.');

                // and the type name found in the source
                buf.append(unresolvedIdent);
                template.setText(buf.toString());
                buf.setLength(0);

                // check if this single-type is contained in the repository
                if (Arrays.binarySearch(repository, template.getText()) > -1)
                {
                    // don't add an already existing declaration
                    if (
                        !singleTypeImports.contains(template)
                        && !result.contains(template))
                    {
                        JavaNode node =
                             (JavaNode) _factory.create(JavaTokenTypes.IMPORT, template.getText());
                        node.setStartLine(resolvableImport.getStartLine());
                        node.setParent(resolvableImport.getParent());
                        node.setFirstChild(resolvableImport.getFirstChild());
                        node.setNextSibling(resolvableImport.getNextSibling());
                        node.setHiddenBefore(resolvableImport.getHiddenBefore());
                        node.setHiddenAfter(resolvableImport.getHiddenAfter());

                        // add the comments from the on-demand import only to
                        // the first node we resolve to avoid duplication
                        resolvableImport.setHiddenBefore(null);
                        resolvableImport.setHiddenAfter(null);

                        // check for conflicts
                        JavaNode other = (JavaNode) conflicts.put(unresolvedIdent, node);

                        if (other == null)
                        {
                            result.add(node);
                        }
                        else // conflict found
                        {
                            // if either one represents the default package...
                            if (isDefaultPackage(node.getText(), defaultPackageName))
                            {
                                // exchange or...
                                result.remove(other);
                                result.add(node);
                            }
                            else if (isDefaultPackage(node.getText(), defaultPackageName))
                            {
                                ;
                            }
                            else
                            {
                                if (Loggers.TRANSFORM.isDebugEnabled())
                                {
                                    Loggers.TRANSFORM.debug(
                                        _filename
                                        + ":0:0: cannot expand due to unresolvable conflict -- "
                                        + node + " " + other);
                                }

                                return Collections.EMPTY_LIST;
                            }
                        }

CHECK:

                        // inner class check (this sucks)
                        for (int k = 0, s = _qualIdents.size(); k < s; k++)
                        {
                            String type = (String) _qualIdents.get(k);

                            if (node.getText().endsWith(type))
                            {
                                String name =
                                    node.getText().substring(0, node.getText().indexOf(type))
                                    + type.substring(0, type.lastIndexOf('.'));

                                if (Arrays.binarySearch(repository, name) > -1)
                                {
                                    node.setText(name);

                                    break CHECK;
                                }
                            }
                        }
                    }
                }
            }
        }

        // remove obsolete imports
        for (int i = 0, size = result.size(); i < size; i++)
        {
            JavaNode node = (JavaNode) result.get(i);

            // remove the default package we added to check for conflicts
            if (node.getStartLine() == DEFAULT_PACKAGE)
            {
                result.remove(i);
                size--;
                i--;
            }
            else if (
                isLangPackage(node.getText())
                || isDefaultPackage(node.getText(), defaultPackageName))
            {
                result.remove(i);
                size--;
                i--;
            }
            else
            {
                _args[0] = _root.getText();
                _args[1] = new Integer(node.getStartLine());
                _args[2] = node.getText();

                Loggers.TRANSFORM.l7dlog(
                    Level.INFO, "TRANS_IMP_EXPAND_ON_DEMAND", _args, null);
            }
        }

        return result;
    }


    /**
     * Checks the list with the single-type import declaration for obsolete entries
     * wrongly transformed inner classes imports.
     *
     * @param singleTypeImports found qualified import nodes.
     * @param qualIdents reported qualified identifiers.
     * @param unqualIdents reported unqualified identifiers.
     *
     * @return modified single-type imports list.
     */
    private List removeObsoleteImports(
        final List singleTypeImports,
        final List qualIdents,
        final List unqualIdents)
    {
        List result = new ArrayList();
        String packageName = _packageName;
        StringBuffer buf = new StringBuffer(100);

        for (int i = 0, size = singleTypeImports.size(); i < size; i++)
        {
            JavaNode node = (JavaNode) singleTypeImports.get(i);

            // first check: obsolete imports (java.lang && default package)
            if (isLangPackage(node.getText()) || isDefaultPackage(node.getText(), packageName))
            {
                _args[0] = _root.getText();
                _args[1] = new Integer(node.getStartLine());
                _args[2] = node.getText();

                Loggers.TRANSFORM.l7dlog(
                    Level.INFO, "TRANS_IMP_REMOVE_OBSOLETE", _args, null);
            }
            else
            {
                String path = StringHelper.getPackageName(node.getText());
                boolean furtherCheck = true;
CHECK:

                // second check: inner classes
                // we take every given qualified identifier
                for (int j = 0, s = qualIdents.size(); j < s; j++)
                {
                    String type = (String) qualIdents.get(j);

                    // and build a type name by adding the recursively
                    // (starting rightmost) stripped path part from the
                    // identifier to the given import package name
                    //
                    // import package name: de.hunsicker.jalopy.language
                    // qual identifier: ClassRepositoryEntry.Info.Entry
                    //     --> de.hunsicker.jalopy.language.ClassRepositoryEntry.Info
                    //     --> de.hunsicker.jalopy.language.ClassRepositoryEntry
                    for (
                        int lastdot = type.lastIndexOf('.'); lastdot > -1;
                        lastdot = type.lastIndexOf('.'))
                    {
                        type = type.substring(0, lastdot);
                        buf.setLength(0);
                        buf.append(path);
                        buf.append('.');
                        buf.append(type);

                        // if the import node matches the built name
                        // we know this identifier is used in the source
                        if (node.getText().equals(buf.toString()))
                        {
                            result.add(node);

                            // no further checking necessary
                            furtherCheck = false;

                            break CHECK;
                        }
                    }
                }

                if (furtherCheck)
                {
                    // third check: unused import
                    if (!unqualIdents.contains(StringHelper.getClassName(node.getText())))
                    {
                        _args[0] = _root.getText();
                        _args[1] = new Integer(node.getStartLine());
                        _args[2] = node.getText();

                        Loggers.TRANSFORM.l7dlog(
                            Level.INFO, "TRANS_IMP_REMOVE_UNUSED", _args, null);
                    }
                    else
                    {
                        result.add(node);
                    }
                }
            }
        }

        return result;
    }


    /**
     * Updates the tree with the given information.
     *
     * @param onDemandImports found on-demand import declaration nodes.
     * @param singleTypeImports found single-tpye import declaration nodes.
     */
    private void updateTree(
        final List onDemandImports,
        final List singleTypeImports)
    {
        List imports = new ArrayList(onDemandImports.size() + singleTypeImports.size());

        // add the trailing star to the on-demand imports
        for (int i = 0, size = onDemandImports.size(); i < size; i++)
        {
            JavaNode node = (JavaNode) onDemandImports.get(i);
            node.setText(node.getText() + ".*");
            imports.add(node);
        }

        imports.addAll(singleTypeImports);

        // sort lexicographically
        if (_sortImports)
        {
            List info =
                decodeGroupingInfo(
                    Convention.getInstance().get(
                        ConventionKeys.IMPORT_GROUPING, ConventionDefaults.IMPORT_GROUPING));
            COMP_IMPORT.identifiers = info;
            Collections.sort(imports, COMP_IMPORT);
        }
        else // we should preserve the original order, so sort by line
        {
            Collections.sort(imports, COMP_LINE);
        }

        createImportNodes((JavaNode[]) imports.toArray(EMPTY_NODE_ARRAY));
    }

    //~ Inner Classes --------------------------------------------------------------------

    private static final class ImportNodeComparator
        implements Comparator
    {
        List identifiers;

        public int compare(
            Object o1,
            Object o2)
        {
            if (o1 == o2)
            {
                return 0;
            }

            JavaNode n1 = (JavaNode) o1;
            JavaNode n2 = (JavaNode) o2;

            int i1 = getIndex(identifiers, n1.getText());
            int i2 = getIndex(identifiers, n2.getText());

            if (i1 > -1)
            {
                if (i2 > -1)
                {
                    if (i1 > i2)
                    {
                        return 1;
                    }
                    else if (i2 > i1)
                    {
                        return -1;
                    }
                }
                else
                {
                    i2 = getIndex(identifiers, STAR);

                    if (i1 > i2)
                    {
                        return 1;
                    }
                    else if (i2 > i1)
                    {
                        return -1;
                    }
                }
            }
            else if (i2 > -1)
            {
                i1 = getIndex(identifiers, STAR);

                if (i1 > i2)
                {
                    return 1;
                }
                else if (i2 > i1)
                {
                    return -1;
                }
            }

            return n1.getText().compareTo(n2.getText());
        }
    }


    /**
     * Compares two nodes by their natural order (the order as given in the original Java
     * source file).
     */
    private static final class NodeLineComparator
        implements Comparator
    {
        public int compare(
            Object o1,
            Object o2)
        {
            if (o1 == o2)
            {
                return 0;
            }

            JavaNode n1 = (JavaNode) o1;
            JavaNode n2 = (JavaNode) o2;

            if (n1.getStartLine() == n2.getStartLine())
            {
                return n1.getText().compareTo(n2.getText());
            }
            if (n1.getStartLine() > n2.getStartLine())
            {
                return 1;
            }
            else if (n1.getStartLine() < n2.getStartLine())
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
    }


    /**
     * Compares two import declaration nodes.
     */
    private static final class NodeOnDemandSingleComparator
        implements Comparator
    {
        /**
         * DOCUMENT ME!
         *
         * @param o1 on-demand import declaration node.
         * @param o2 single-type import declaration node.
         *
         * @return DOCUMENT ME!
         */
        public int compare(
            Object o1,
            Object o2)
        {
            if (o1 == o2)
            {
                return 0;
            }

            JavaNode packagePath = (JavaNode) o1;
            JavaNode importNode = (JavaNode) o2;

            return packagePath.getText().compareTo(
                StringHelper.getPackageName(importNode.getText()));
        }
    }


    /**
     * Compares two nodes lexicographically.
     */
    private static final class NodeStringComparator
        implements Comparator
    {
        public int compare(
            Object o1,
            Object o2)
        {
            if (o1 == o2)
            {
                return 0;
            }

            JavaNode n1 = (JavaNode) o1;
            JavaNode n2 = (JavaNode) o2;

            return n1.getText().compareTo(n2.getText());
        }
    }
}
