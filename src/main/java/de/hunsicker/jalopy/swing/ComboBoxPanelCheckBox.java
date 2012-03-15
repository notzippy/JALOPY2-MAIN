/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;


/**
 * A component that combines a check box and a {@link ComboBoxPanel combo box panel}. The
 * state of the panel depends on the state of the check box in that items can only be
 * selected if the check box is selected.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class ComboBoxPanelCheckBox
    extends JPanel
{
    //~ Instance variables ---------------------------------------------------------------

    /** The combo box panel. */
    ComboBoxPanel _combo;

    /** The check box. */
    private JCheckBox _check;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ComboBoxPanelCheckBox.
     *
     * @param checkText the text of the panel label.
     * @param comboText the text of the check box label.
     * @param items the items to display in the check box.
     */
    public ComboBoxPanelCheckBox(
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
    public ComboBoxPanelCheckBox(
        String   checkText,
        boolean  selected,
        String   comboText,
        Object[] items,
        Object   item)
    {
        setLayout(new BorderLayout());
        _combo = new ComboBoxPanel(comboText, items, item);
        setCheckBox(new JCheckBox(checkText));

        // only after the initialization of the item listener, the state of the
        // combo box will depend on the state of the check box
        _check.setSelected(selected);
        add(_check, BorderLayout.WEST);
        add(Box.createHorizontalStrut(10), BorderLayout.CENTER);
        add(_combo, BorderLayout.EAST);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the check box to use.
     *
     * @param checkBox check box to use.
     */
    public void setCheckBox(JCheckBox checkBox)
    {
        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == _check)
            {
                remove(i);
                add(checkBox, BorderLayout.WEST, i);

                break;
            }
        }

        _check = checkBox;
        _check.addItemListener(
            new ItemListener()
            {
                public void itemStateChanged(ItemEvent ev)
                {
                    switch (ev.getStateChange())
                    {
                        case ItemEvent.SELECTED :
                            _combo.setEnabled(true);

                            break;

                        default :
                            _combo.setEnabled(false);
                    }
                }
            });
        _check.setSelected(checkBox.isSelected());
    }


    /**
     * Returns the used check box.
     *
     * @return the used check box.
     */
    public JCheckBox getCheckBox()
    {
        return _check;
    }


    /**
     * Sets the combo box panel to use.
     *
     * @param panel the combo box panel to use.
     */
    public void setComboBoxPanel(ComboBoxPanel panel)
    {
        panel.setEnabled(_check.isSelected());

        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == _combo)
            {
                remove(i);
                add(panel, BorderLayout.EAST, i);

                break;
            }
        }

        _combo = panel;
    }


    /**
     * Returns the used combo box panel.
     *
     * @return the used combo box panel.
     */
    public ComboBoxPanel getComboBoxPanel()
    {
        return _combo;
    }
}
