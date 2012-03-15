/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.util.Comparator;
import java.util.List;

import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;

import antlr.collections.AST;


/**
 * Compares two VARIABLE_DEF nodes first by accessibility, then by name. Special checking
 * is applied to avoid forward references as a result to mindless sorting.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class VariableDefNodeComparator
    extends NodeComparator
{
    //~ Instance variables ---------------------------------------------------------------

    /** Holds all instance variable type names of a given class. */
    List names; // List of <String>

    /** Simple tree walker to search tree portions. */
    private final TreeSearcher _searcher = new TreeSearcher();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new VariableDefNodeComparator object.
     */
    public VariableDefNodeComparator()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Compares its two arguments for order. Returns a negative integer, zero, or a
     * positive integer as the first argument is less than, equal to, or greater than
     * the second.
     *
     * @param o1 the first node.
     * @param o2 the second node
     *
     * @return a negative integer, zero, or a positive integer as the first argument is
     *         less than, equal to, or greater than the second.
     *
     * @throws IllegalStateException DOCUMENT ME!
     */
    public int compare(
        Object o1,
        Object o2)
    {
        if (o1 == o2)
        {
            return 0;
        }

        if (this.names == null)
        {
            throw new IllegalStateException("no variable type names has been set");
        }

        AST node1 = (AST) o1;
        AST node2 = (AST) o2;
        String name1 =
            JavaNodeHelper.getFirstChild(node1, JavaTokenTypes.IDENT).getText();
        //String name2 =
        //    JavaNodeHelper.getFirstChild(node2, JavaTokenTypes.IDENT).getText();

        // first make sure we don't introduce forward references
        //
        //      private short indentSize = 4;
        //      private short currentIndent = indentSize;
        //
        AST assign = JavaNodeHelper.getFirstChild(node2, JavaTokenTypes.ASSIGN);

        if (assign != null)
        {
            _searcher.reset();
            _searcher.name = name1;
            _searcher.walk(assign);

            // if the name of the first node is contained in the second one,
            if (_searcher.result == TreeSearcher.FOUND)
            {
                return -1;
            }
        }

        // now check the accessibility
        int mod1 = JavaNodeModifier.valueOf(node1);
        int mod2 = JavaNodeModifier.valueOf(node2);
        int result = compareModifiers(mod1, mod2);

        if (result != 0)
        {
            return result;
        }

        result = NodeComparator.compareTypes(node1, node2);

        if (result != 0)
        {
            return result;
        }

        return NodeComparator.compareNames(node1, node2);
    }

    //~ Inner Classes --------------------------------------------------------------------

    private static final class TreeSearcher
        extends TreeWalker
    {
        static final int FOUND = 1;
        String name;
        int result;

        public void reset()
        {
            super.reset();
            this.result = 0;
        }


        public void visit(AST node)
        {
            if (this.name.equals(node.getText()))
            {
                this.result = FOUND;
                stop();
            }
        }
    }
}
