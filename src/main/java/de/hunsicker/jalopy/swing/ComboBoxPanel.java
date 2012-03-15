/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * A component that combines a combo box and a descriptive label.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
class ComboBoxPanel
    extends JPanel
{
    //~ Instance variables ---------------------------------------------------------------

    /** The combo box. */
    private JComboBox _combo;

    /** The label. */
    private JLabel _label;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ComboBoxPanel.
     *
     * @param text the label text.
     * @param items the items to display in the checkbox.
     * @param item the item to initially select. If the item does not exist, it will be
     *        added to the list.
     */
    public ComboBoxPanel(
        String   text,
        Object[] items,
        Object   item)
    {
        this(text, items);

        boolean found = false;

        for (int i = 0; i < items.length; i++)
        {
            if (items[i].equals(item))
            {
                found = true;

                break;
            }
        }

        if (!found)
        {
            _combo.addItem(item);
        }

        _combo.setSelectedItem(item);
    }


    /**
     * Creates a new ComboBoxPanel.
     *
     * @param text the label text.
     * @param items the items to display in the checkbox.
     */
    public ComboBoxPanel(
        String   text,
        Object[] items)
    {
        init(text, items);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the combo box to use.
     *
     * @param comboBox combo box to use.
     */
    public void setComboBox(JComboBox comboBox)
    {
        _combo = comboBox;
    }


    /**
     * Returns the used combo box.
     *
     * @return the used combo box.
     */
    public JComboBox getComboBox()
    {
        return _combo;
    }


    /**
     * Enables the component so that items can be selected.
     *
     * @param enable if <code>true</code> items will be selectable and values can be
     *        typed in the editor (if it is editable).
     */
    public void setEnabled(boolean enable)
    {
        super.setEnabled(enable);
        _combo.setEnabled(enable);
        _label.setEnabled(enable);
    }


    /**
     * Initializes the UI:
     *
     * @param text the label text.
     * @param items the items to display in the checkbox.
     */
    private void init(
        String   text,
        Object[] items)
    {
        setLayout(new BorderLayout());
        _label = new JLabel(text);
        _label.setLabelFor(_combo);
        _label.setForeground(Color.black);
        add(_label, BorderLayout.WEST);
        add(Box.createHorizontalStrut(10), BorderLayout.CENTER);
        _combo = new JComboBox(items);
        add(_combo, BorderLayout.EAST);
    }
}
