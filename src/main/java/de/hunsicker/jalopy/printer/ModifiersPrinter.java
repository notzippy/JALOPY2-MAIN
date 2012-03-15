/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;
import de.hunsicker.jalopy.language.ModifierType;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for the imaginary modifiers node (<code>MODIFIERS</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class ModifiersPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Comparator to sort modifiers. */
    private static final Comparator COMP_MODIFIERS = new ModifiersComparator();

    /** Singleton. */
    private static final Printer INSTANCE = new ModifiersPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates new ModifiersPrinter object.
     */
    protected ModifiersPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return the sole instance of this class.
     */
    public static final Printer getInstance()
    {
        return INSTANCE;
    }


    /**
     * {@inheritDoc}
     */
    public void print(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SORT_MODIFIERS, ConventionDefaults.SORT_MODIFIERS))
        {
            JavaNode firstModifier = (JavaNode) node.getFirstChild();

            if (firstModifier != null)
            {
                CommonHiddenStreamToken firstComment = firstModifier.getHiddenBefore();
                firstModifier.setHiddenBefore(null);

                List modifiers = new ArrayList(5);

                for (
                    AST modifier = firstModifier; modifier != null;
                    modifier = modifier.getNextSibling())
                {
                    if (modifier.getType() == JavaTokenTypes.ANNOTATION) {
                        // TODO Sort the annotation modifiers ???
                        if (firstComment!=null) {
                            ((JavaNode)modifier).setHiddenBefore(firstComment);
                            firstComment = null;
                        }
                        PrinterFactory.create(modifier, out).print(modifier,out);
                    }
                    else {
                        modifiers.add(modifier);
                    }
                }

                Collections.sort(modifiers, COMP_MODIFIERS);

                if (!modifiers.isEmpty()) {
	                firstModifier = (JavaNode) modifiers.get(0);
	                firstModifier.setHiddenBefore(firstComment);
                }

                for (int i = 0, size = modifiers.size(); i < size; i++)
                {
                    AST modifier = (AST) modifiers.get(i);
                    PrinterFactory.create(modifier, out).print(modifier, out);
                }
            }
        }
        else
        {
            for (
                AST modifier = node.getFirstChild(); modifier != null;
                modifier = modifier.getNextSibling())
            {
                PrinterFactory.create(modifier, out).print(modifier, out);
            }
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    private static final class ModifiersComparator
        implements Comparator
    {
        public int compare(
            Object o1,
            Object o2)
        {
            AST node1 = (AST) o1;
            AST node2 = (AST) o2;

            ModifierType modifier1 = ModifierType.valueOf(node1.getText());
            ModifierType modifier2 = ModifierType.valueOf(node2.getText());

            return modifier1.compareTo(modifier2);
        }


        public boolean equals(Object o)
        {
            if (o instanceof ModifiersComparator)
            {
                return true;
            }

            return false;
        }


        public int hashCode()
        {
            return super.hashCode();
        }
    }
}
