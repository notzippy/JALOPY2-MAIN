/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.StringHelper;


/**
 * Settings page for the Jalopy printer header settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class HeaderSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String LINE_SEPARATOR = "\n" /* NOI18N */;
    private static final String EMPTY_STRING = "" /* NOI18N */.intern();

    //~ Instance variables ---------------------------------------------------------------

    private AddRemoveList _keysList;
    private JButton _addButton;
    private JButton _removeButton;
    private JCheckBox _useCheckBox;
    private JCheckBox _ignoreIfExistsCheckBox;
    private JComboBox _blankLinesAfterComboBox;
    private JComboBox _blankLinesBeforeComboBox;
    private JComboBox _smartModeComboBox;
    private JTabbedPane _tabs;
    private JTextArea _textTextArea;
    private NumberComboBoxPanel _blankLinesAfterComboBoxPnl;
    private NumberComboBoxPanel _blankLinesBeforeComboBoxPnl;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new HeaderSettingsPage object.
     */
    public HeaderSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new HeaderSettingsPage.
     *
     * @param container the parent container.
     */
    HeaderSettingsPage(SettingsContainer container)
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
        this.settings.put(
            getBlankLinesBeforeKey(), (String) _blankLinesBeforeComboBox.getSelectedItem());
        this.settings.put(
            getBlankLinesAfterKey(), (String) _blankLinesAfterComboBox.getSelectedItem());
        this.settings.putBoolean(getUseKey(), _useCheckBox.isSelected());
        this.settings.putBoolean(getIgnoreIfExistsKey(), _ignoreIfExistsCheckBox.isSelected());
        this.settings.put(
            getSmartModeKey(), (String) _smartModeComboBox.getSelectedItem());

        storeText();

        DefaultListModel keysListModel = (DefaultListModel) _keysList.getModel();

        if (keysListModel.size() > 0)
        {
            String[] items = new String[keysListModel.size()];
            keysListModel.copyInto(items);

            StringBuffer buf = new StringBuffer(100);

            for (int i = 0; i < items.length; i++)
            {
                buf.append(items[i]);
                buf.append(DELIMETER);
            }

            buf.deleteCharAt(buf.length() - 1);
            this.settings.put(getConventionKeysKey(), buf.toString());
        }
        else
        {
            this.settings.put(getConventionKeysKey(), EMPTY_STRING);
        }
    }


    /**
     * {@inheritDoc}
     */
    public void validateSettings()
      throws ValidationException
    {
        DefaultListModel keysListModel = (DefaultListModel) _keysList.getModel();

        // we need at least one identify key to make the header/footer feature
        // work
        if (
            !isSmartModeEnabled() && _useCheckBox.isSelected()
            && (keysListModel.size() == 0))
        {
            Object[] args = { getDeleteLabel() };
            JOptionPane.showMessageDialog(
                HeaderSettingsPage.this,
                MessageFormat.format(
                    this.bundle.getString("MSG_MISSING_IDENTIFY_KEY" /* NOI18N */), args),
                this.bundle.getString("TLE_MISSING_IDENTIFY_KEY" /* NOI18N */),
                JOptionPane.ERROR_MESSAGE);

            throw new ValidationException();
        }
    }


    /**
     * Returns the settings key to store the setting.
     *
     * @return settings key.
     *
     * @see de.hunsicker.jalopy.storage.ConventionKeys#BLANK_LINES_AFTER_HEADER
     */
    protected Convention.Key getBlankLinesAfterKey()
    {
        return ConventionKeys.BLANK_LINES_AFTER_HEADER;
    }


    /**
     * Returns the settings key to store the setting.
     *
     * @return settings key.
     *
     * @see de.hunsicker.jalopy.storage.ConventionKeys#BLANK_LINES_BEFORE_HEADER
     */
    protected Convention.Key getBlankLinesBeforeKey()
    {
        return ConventionKeys.BLANK_LINES_BEFORE_HEADER;
    }


    /**
     * Returns the settings key to store the setting.
     *
     * @return settings key.
     *
     * @see de.hunsicker.jalopy.storage.ConventionKeys#HEADER_KEYS
     */
    protected Convention.Key getConventionKeysKey()
    {
        return ConventionKeys.HEADER_KEYS;
    }


    /**
     * Returns the default value for the BLANK_LINES_AFTER_XXX setting.
     *
     * @return default value of the BLANK_LINES_AFTER_XXX setting.
     *
     * @since 1.0b9
     */
    protected String getDefaultAfter()
    {
        return String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_HEADER);
    }


    /**
     * Returns the label text for the identiy panel.
     *
     * @return label text.
     */
    protected String getDeleteLabel()
    {
        return this.bundle.getString("BDR_DELETE_HEADERS" /* NOI18N */);
    }


    /**
     * Returns the default values for the combo box entries to choose the value for the
     * BLANK_LINES_AFTER_XXX setting.
     *
     * @return the default values for the blank lines after combo box.
     *
     * @since 1.0b9
     */
    protected String[] getItemsAfter()
    {
        return createItemList(new int[] { 0, 1, 2, 3, 4, 5 });
    }


    /**
     * Returns the settings key to store the setting.
     *
     * @return settings key.
     *
     * @see de.hunsicker.jalopy.storage.ConventionKeys#HEADER_SMART_MODE_LINES
     */
    protected Convention.Key getSmartModeKey()
    {
        return ConventionKeys.HEADER_SMART_MODE_LINES;
    }


    /**
     * Returns the settings key to store the setting.
     *
     * @return settings key.
     *
     * @see de.hunsicker.jalopy.storage.ConventionKeys#HEADER_TEXT
     */
    protected Convention.Key getTextKey()
    {
        return ConventionKeys.HEADER_TEXT;
    }


    /**
     * Returns the settings key to store the setting.
     *
     * @return settings key.
     *
     * @see de.hunsicker.jalopy.storage.ConventionKeys#HEADER
     */
    protected Convention.Key getUseKey()
    {
        return ConventionKeys.HEADER;
    }


    /**
     * Returns the settings key to store the setting.
     *
     * @return settings key.
     *
     * @see de.hunsicker.jalopy.storage.ConventionKeys#HEADER_IGNORE_IF_EXISTS
     */
    protected Convention.Key getIgnoreIfExistsKey()
    {
        return ConventionKeys.HEADER_IGNORE_IF_EXISTS;
    }


    /**
     * Returns the text for the use label.
     *
     * @return text for use label.
     */
    protected String getUseLabel()
    {
        return this.bundle.getString("CHK_USE_HEADER" /* NOI18N */);
    }


    /**
     * Returns the text for the ignore if exists label.
     *
     * @return text for ignore if exists label.
     */
    protected String getIgnoreIfExistsLabel()
    {
        return this.bundle.getString("CHK_IGNORE_HEADER_IF_EXISTS" /* NOI18N */);
    }


    /**
     * Determines whether the SmartMode feature is enabled.
     *
     * @return <code>true</code> if the SmartMode feature is enabled.
     *
     * @since 1.0b8
     */
    private boolean isSmartModeEnabled()
    {
        try
        {
            return Integer.parseInt((String) _smartModeComboBox.getSelectedItem()) > 0;
        }
        catch (NumberFormatException neverOccurs)
        {
            return false;
        }
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        _textTextArea = new JTextArea(loadText(), 7, 50);
        _textTextArea.setFont(new Font("Monospaced" /* NOI18N */, Font.PLAIN, 12));
        _textTextArea.setForeground(new Color(0, 128, 128));
        _textTextArea.setCaretPosition(0);

        JScrollPane textScroller = new JScrollPane(_textTextArea);
        JPanel textPanel = new JPanel();
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textPanel.setLayout(new BorderLayout());
        textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(textScroller);

        JPanel headerPanel = new JPanel();
        GridBagLayout headerLayout = new GridBagLayout();
        headerPanel.setLayout(headerLayout);
        headerPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_GENERAL" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 0)));

        GridBagConstraints c = new GridBagConstraints();
        _useCheckBox =
            new JCheckBox(getUseLabel(), this.settings.getBoolean(getUseKey(), false));
        _useCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        headerLayout.setConstraints(_useCheckBox, c);
        headerPanel.add(_useCheckBox);


        GridBagConstraints c2 = new GridBagConstraints();
        _ignoreIfExistsCheckBox =
            new JCheckBox(getIgnoreIfExistsLabel(), this.settings.getBoolean(getIgnoreIfExistsKey(), false));
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        headerLayout.setConstraints(_ignoreIfExistsCheckBox, c);
        headerPanel.add(_ignoreIfExistsCheckBox);
        
        int lines =
            this.settings.getInt(
                getSmartModeKey(), ConventionDefaults.HEADER_SMART_MODE_LINES);
        String[] lineItems = createItemList(new int[] { 0, 5, 10, 15, 20 });
        NumberComboBoxPanel smartModeComboBoxPanel =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_SMART_MODE" /* NOI18N */), lineItems,
                String.valueOf(lines));
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        headerLayout.setConstraints(smartModeComboBoxPanel, c);
        headerPanel.add(smartModeComboBoxPanel);
        _smartModeComboBox = smartModeComboBoxPanel.getComboBox();

        JPanel blankLinesPanel = new JPanel();
        blankLinesPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_BLANK_LINES" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout blankLinesLayout = new GridBagLayout();
        blankLinesPanel.setLayout(blankLinesLayout);

        String[] items = createItemList(new int[] { 0, 1, 2, 3, 4, 5 });
        _blankLinesBeforeComboBoxPnl =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BLANK_LINES_BEFORE" /* NOI18N */), items,
                this.settings.get(getBlankLinesBeforeKey(), "0" /* NOI18N */));
        _blankLinesBeforeComboBox = _blankLinesBeforeComboBoxPnl.getComboBox();
        _blankLinesBeforeComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        blankLinesLayout.setConstraints(_blankLinesBeforeComboBoxPnl, c);
        blankLinesPanel.add(_blankLinesBeforeComboBoxPnl);

        _blankLinesAfterComboBoxPnl =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BLANK_LINES_AFTER" /* NOI18N */),
                getItemsAfter(),
                this.settings.get(getBlankLinesAfterKey(), getDefaultAfter()));
        _blankLinesAfterComboBox = _blankLinesAfterComboBoxPnl.getComboBox();
        _blankLinesAfterComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        blankLinesLayout.setConstraints(_blankLinesAfterComboBoxPnl, c);
        blankLinesPanel.add(_blankLinesAfterComboBoxPnl);

        JPanel identifyPanel = new JPanel();
        identifyPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(getDeleteLabel()),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout identifyLayout = new GridBagLayout();
        identifyPanel.setLayout(identifyLayout);

        String keysString = this.settings.get(getConventionKeysKey(), EMPTY_STRING);
        List keys = Collections.EMPTY_LIST;

        if ((keysString != null) && !keysString.trim().equals(EMPTY_STRING))
        {
            keys = new ArrayList();

            for (
                StringTokenizer tokens = new StringTokenizer(keysString, DELIMETER);
                tokens.hasMoreElements();)
            {
                keys.add(tokens.nextElement());
            }
        }

        _keysList =
            new AddRemoveList(
                this.bundle.getString("TLE_ADD_IDENTIFY_KEY" /* NOI18N */),
                this.bundle.getString("LBL_ADD_IDENTIFY_KEY" /* NOI18N */), keys);

        JScrollPane keysScrollPane = new JScrollPane(_keysList);
        SwingHelper.setConstraints(
            c, 0, 0, 8, 8, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        identifyLayout.setConstraints(keysScrollPane, c);
        identifyPanel.add(keysScrollPane);

        c.insets.bottom = 2;
        c.insets.top = 10;
        c.insets.left = 10;
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 9, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        _addButton = _keysList.getAddButton();
        identifyLayout.setConstraints(_addButton, c);
        identifyPanel.add(_addButton);

        c.insets.left = 10;
        c.insets.right = 0;
        c.insets.bottom = 0;
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 9, 2, GridBagConstraints.REMAINDER, 1, 0.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        _removeButton = _keysList.getRemoveButton();
        _removeButton.setEnabled(false);
        identifyLayout.setConstraints(_removeButton, c);
        identifyPanel.add(_removeButton);

        JPanel panels = new JPanel();
        GridBagLayout panelsLayout = new GridBagLayout();
        panels.setLayout(panelsLayout);

        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        panelsLayout.setConstraints(headerPanel, c);
        panels.add(headerPanel);

        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        panelsLayout.setConstraints(blankLinesPanel, c);
        panels.add(blankLinesPanel);

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        panelsLayout.setConstraints(identifyPanel, c);
        panels.add(identifyPanel);

        _tabs = new JTabbedPane();
        _tabs.add(panels, this.bundle.getString("TAB_OPTIONS" /* NOI18N */));
        _tabs.add(textPanel, this.bundle.getString("TAB_TEXT" /* NOI18N */));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(_tabs, BorderLayout.CENTER);
    }


    private String loadText()
    {
        String text = this.settings.get(getTextKey(), EMPTY_STRING);

        return text.replace('|', '\n');
    }


    private void storeText()
    {
        String text = _textTextArea.getText().trim();

        if (text.length() > 0)
        {
            String[] lines = StringHelper.split(text, LINE_SEPARATOR);

            StringBuffer buf = new StringBuffer(text.length());

            for (int i = 0; i < lines.length; i++)
            {
                buf.append(StringHelper.trimTrailing(lines[i]));
                buf.append(DELIMETER);
            }

            if (lines.length > 0)
            {
                buf.deleteCharAt(buf.length() - 1);
            }

            this.settings.put(getTextKey(), buf.toString());
        }
    }
}
