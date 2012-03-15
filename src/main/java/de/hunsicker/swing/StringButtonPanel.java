/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.ResourceBundleFactory;


/**
 * A component to display and change a string value. It consists of a text field with a
 * label to the left and a button to the right to change the value of the text field.
 *
 * @author Marco Hunsicker
 */
public abstract class StringButtonPanel
    extends JPanel
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME = "de.hunsicker.swing.Bundle" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** DOCUMENT ME! */
    protected JButton button;

    /** DOCUMENT ME! */
    protected JLabel label;

    /** DOCUMENT ME! */
    protected JTextField textField;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new StringButtonPanel.
     *
     * @param label the label text.
     * @param value the initial value of the text field.
     */
    public StringButtonPanel(
        String label,
        String value)
    {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();

        this.label = new JLabel(label);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        layout.setConstraints(this.label, c);
        add(this.label);

        this.textField = new JTextField(value);
        this.textField.setEditable(false);
        c.insets.left = 10;
        SwingHelper.setConstraints(
            c, 1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(this.textField, c);
        add(this.textField);

        this.button =
            new JButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_CHANGE" /* NOI18N */));
        this.button.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    changePressed();
                }
            });
        SwingHelper.setConstraints(
            c, 2, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        layout.setConstraints(this.button, c);
        add(this.button);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the text field to display edit the value.
     *
     * @return the used text field.
     */
    public JTextField getTextComponent()
    {
        return this.textField;
    }


    /**
     * Called when the users presses the change button.
     */
    public abstract void changePressed();
}
