/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.Color;
import java.awt.Component;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


/**
 * A component that can be used to display/edit the Jalopy brace settings for the if-else
 * handling.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
class BracesIfElsePanel
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    static ResourceBundle res =
        ResourceBundle.getBundle("de.hunsicker.jalopy.swing.Bundle" /* NOI18N */);

    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _singleElseCheckBox;
    private JCheckBox _singleIfCheckBox;
    private JCheckBox _specialIfElseCheckBox;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new BracesIfElsePanel.
     */
    public BracesIfElsePanel()
    {
        initialize();
    }


    /**
     * Creates a new BracesIfElsePanel.
     *
     * @param container the parent container.
     */
    BracesIfElsePanel(SettingsContainer container)
    {
        super(container);
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Empty
     */
    public void updateSettings()
    {
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        JPanel ifElsePanel = new JPanel();
        ifElsePanel.setBorder(
            BorderFactory.createCompoundBorder(
                new TitledBorder(
                    BorderFactory.createLineBorder(new Color(153, 153, 153), 1),
                    " if-else "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        ifElsePanel.setLayout(new BoxLayout(ifElsePanel, BoxLayout.Y_AXIS));
        ifElsePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        _singleIfCheckBox = new JCheckBox("Single if-statement in same line");
        ifElsePanel.add(_singleIfCheckBox);
        _singleElseCheckBox = new JCheckBox("Single else-statement in same line");
        ifElsePanel.add(_singleElseCheckBox);
        _specialIfElseCheckBox = new JCheckBox("Special if-else treatment");
        ifElsePanel.add(_specialIfElseCheckBox);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(10));
        add(ifElsePanel);
    }
}
