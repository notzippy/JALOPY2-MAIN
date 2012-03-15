/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import javax.swing.JComboBox;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class NumberComboBoxPanel
    extends ComboBoxPanel
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new NumberComboBoxPanel object.
     *
     * @param text the label text.
     * @param items the items to display in the checkbox.
     */
    public NumberComboBoxPanel(
        String   text,
        Object[] items)
    {
        super(text.trim(), items);
        initialize();
    }


    /**
     * Creates a new NumberComboBoxPanel object.
     *
     * @param text the label text.
     * @param items the items to display in the checkbox.
     * @param item the item to be initially selected.
     */
    public NumberComboBoxPanel(
        String   text,
        Object[] items,
        Object   item)
    {
        super(text.trim(), items, item);
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void initialize()
    {
        JComboBox combo = getComboBox();
        combo.setEditor(
            new NumberComboBoxEditor(
                Integer.parseInt((String) combo.getSelectedItem()), 3));
        combo.setEditable(true);
    }
}
