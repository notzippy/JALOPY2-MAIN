/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

/**
 * DOCUMENT ME!
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class NumberComboBoxPanelCheckBox
    extends ComboBoxPanelCheckBox
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ComboBoxPanelCheckBox.
     *
     * @param checkText the text of the panel label.
     * @param comboText the text of the check box label.
     * @param items the items to display in the check box.
     */
    public NumberComboBoxPanelCheckBox(
        String   checkText,
        String   comboText,
        Object[] items)
    {
        this(checkText, false, comboText, items, null);
    }


    /**
     * Creates a new ComboBoxPanelCheckBox.
     *
     * @param checkText the text of the check box.
     * @param selected a boolean value indicating the initial selection state. If
     *        <code>true</code> the check box is selected.
     * @param comboText the text of the combo box panel.
     * @param items the items to display in the combo box panel.
     * @param item the item to initially select in the combo box panel.
     */
    public NumberComboBoxPanelCheckBox(
        String   checkText,
        boolean  selected,
        String   comboText,
        Object[] items,
        Object   item)
    {
        super(checkText, selected, comboText, items, item);
        setComboBoxPanel(new NumberComboBoxPanel(comboText, items, item));
    }
}
