/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy printer comment settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public class EnumSettingsPage
    extends AbstractSettingsPage
{
    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _formatMultiLineCheckBox;
    private JCheckBox _removeJavadocCheckBox;
    private JCheckBox _removeMultiCheckBox;
    private JCheckBox _removeSingleCheckBox;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new CommentsSettingsPage object.
     */
    public EnumSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new CommentsSettingsPage.
     *
     * @param container the parent container.
     */
    EnumSettingsPage(SettingsContainer container)
    {
        super(container);
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
        this.settings.putBoolean(
            ConventionKeys.COMMENT_REMOVE_SINGLE_LINE, _removeSingleCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.COMMENT_REMOVE_MULTI_LINE, _removeMultiCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.COMMENT_JAVADOC_REMOVE, _removeJavadocCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.COMMENT_FORMAT_MULTI_LINE,
            _formatMultiLineCheckBox.isSelected());
    }


    private void initialize()
    {
        JPanel removePanel = new JPanel();
        GridBagLayout removeLayout = new GridBagLayout();
        removePanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_REMOVE" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 0)));
        removePanel.setLayout(removeLayout);

        GridBagConstraints c = new GridBagConstraints();

        _removeSingleCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_SINGLE_LINE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_REMOVE_SINGLE_LINE,
                    ConventionDefaults.COMMENT_REMOVE_SINGLE_LINE));
        _removeSingleCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_removeSingleCheckBox, c);
        removePanel.add(_removeSingleCheckBox);

        _removeMultiCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_MULTI_LINE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_REMOVE_MULTI_LINE,
                    ConventionDefaults.COMMENT_REMOVE_MULTI_LINE));
        _removeMultiCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_removeMultiCheckBox, c);
        removePanel.add(_removeMultiCheckBox);

        _removeJavadocCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_JAVADOC" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_REMOVE,
                    ConventionDefaults.COMMENT_JAVADOC_REMOVE));
        _removeJavadocCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_removeJavadocCheckBox, c);
        removePanel.add(_removeJavadocCheckBox);

        JPanel formatPanel = new JPanel();
        GridBagLayout formatPanelLayout = new GridBagLayout();
        formatPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_FORMAT" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 0)));
        formatPanel.setLayout(formatPanelLayout);

        _formatMultiLineCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_MULTI_LINE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_FORMAT_MULTI_LINE,
                    ConventionDefaults.COMMENT_FORMAT_MULTI_LINE));
        _formatMultiLineCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        formatPanelLayout.setConstraints(_formatMultiLineCheckBox, c);
        formatPanel.add(_formatMultiLineCheckBox);

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        c.insets.top = 10;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(removePanel, c);
        add(removePanel);

        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(formatPanel, c);
        add(formatPanel);
    }
}
