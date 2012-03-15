/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.EmptyButtonGroup;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy printer general brace settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class BracesSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Indicates C style. */
    private static final String STYLE_C = "c" /* NOI18N */;

    /** Indicates custom style. */
    private static final String STYLE_CUSTOM = "custom" /* NOI18N */;

    /** Indicates old GNU style. */
    private static final String STYLE_GNU = "gnu" /* NOI18N */;

    /** Indicates Sun Java style. */
    private static final String STYLE_SUN = "sun" /* NOI18N */;
    private static final String ZERO = "0" /* NOI18N */;
    private static final String TWO = "2" /* NOI18N */;
    private static final String ONE = "1" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _cStyleCheckBox;
    private JCheckBox _cuddleEmptyBracesCheckBox;
    private JCheckBox _customStyleCheckBox;
    private JCheckBox _gnuStyleCheckBox;
    private JCheckBox _insertDoWhileCheckBox;
    private JCheckBox _insertEmptyStatementCheckBox;
    private JCheckBox _insertForCheckBox;
    private JCheckBox _insertIfElseCheckBox;
    private JCheckBox _insertWhileCheckBox;
    JCheckBox _newlineLeftCheckBox;
    JCheckBox _newlineRightCheckBox;
    private JCheckBox _removeBlockCheckBox;
    private JCheckBox _removeDoWhileCheckBox;
    private JCheckBox _removeForCheckBox;
    private JCheckBox _removeIfElseCheckBox;
    private JCheckBox _removeWhileCheckBox;
    private JCheckBox _sunStyleCheckBox;
    JCheckBox _treatDifferentCheckBox;
    JCheckBox _treatDifferentIfWrappedCheckBox;
    private JComboBox _cuddleEmptyBracesComboBox;
    JComboBox _indentAfterRightBraceComboBox;
    JComboBox _indentLeftBraceComboBox;
    JComboBox _indentRightBraceComboBox;
    private JTabbedPane _tabbedPane;
    private NumberComboBoxPanel _indentAfterRightBraceComboBoxPnl;
    private NumberComboBoxPanel _indentLeftBraceComboBoxPnl;
    private NumberComboBoxPanel _indentRightBraceComboBoxPnl;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new BracesSettingsPage.
     */
    public BracesSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new BracesSettingsPage.
     *
     * @param container the parent container.
     */
    BracesSettingsPage(SettingsContainer container)
    {
        super(container);
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public String getPreviewFileName()
    {
        switch (_tabbedPane.getSelectedIndex())
        {
            case 1 :
                return "bracesmisc" /* NOI18N */;

            default :
                return super.getPreviewFileName();
        }
    }


    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
        this.settings.putBoolean(
            ConventionKeys.BRACE_EMPTY_CUDDLE, _cuddleEmptyBracesCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_EMPTY_INSERT_STATEMENT,
            _insertEmptyStatementCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_BRACE_CUDDLED,
            (String) _cuddleEmptyBracesComboBox.getSelectedItem());
        this.settings.putBoolean(
            ConventionKeys.BRACE_INSERT_IF_ELSE, _insertIfElseCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_INSERT_FOR, _insertForCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_INSERT_DO_WHILE, _insertDoWhileCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_INSERT_WHILE, _insertWhileCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_REMOVE_IF_ELSE, _removeIfElseCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_REMOVE_FOR, _removeForCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_REMOVE_DO_WHILE, _removeDoWhileCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_REMOVE_WHILE, _removeWhileCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_REMOVE_BLOCK, _removeBlockCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_NEWLINE_LEFT, _newlineLeftCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_TREAT_DIFFERENT, _treatDifferentCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_TREAT_DIFFERENT_IF_WRAPPED,
            _treatDifferentIfWrappedCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_NEWLINE_RIGHT, _newlineRightCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_BRACE_RIGHT_AFTER,
            (String) _indentAfterRightBraceComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_BRACE_RIGHT,
            (String) _indentRightBraceComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_BRACE_LEFT,
            (String) _indentLeftBraceComboBox.getSelectedItem());
    }


    /**
     * Determines whether the current settings forms the C brace style.
     *
     * @return <code>true</code> if the the current settings form the C brace style.
     */
    private boolean isCStyle()
    {
        if (!_newlineLeftCheckBox.isSelected())
        {
            return false;
        }
        else if (!_newlineRightCheckBox.isSelected())
        {
            return false;
        }
        else if (!ZERO.equals(_indentLeftBraceComboBox.getSelectedItem()))
        {
            return false;
        }
        else if (!ZERO.equals(_indentRightBraceComboBox.getSelectedItem()))
        {
            return false;
        }
        else if (!ZERO.equals(_indentAfterRightBraceComboBox.getSelectedItem()))
        {
            return false;
        }

        return true;
    }


    /**
     * Sets whether the custom mode should be enabled.
     *
     * @param enable if <code>true</code> this custom mode will be enabled.
     */
    void setCustomMode(boolean enable)
    {
        _indentLeftBraceComboBoxPnl.setEnabled(enable);
        _indentRightBraceComboBoxPnl.setEnabled(enable);
        _indentAfterRightBraceComboBoxPnl.setEnabled(enable);
        _newlineLeftCheckBox.setEnabled(enable);
        _newlineRightCheckBox.setEnabled(enable);
        _treatDifferentCheckBox.setEnabled(enable);
        _treatDifferentIfWrappedCheckBox.setEnabled(enable);
    }


    /**
     * Determines whether the current settings forms the GNU brace style.
     *
     * @return <code>true</code> if the the current settings form the GNU brace style.
     */
    private boolean isGnuStyle()
    {
        if (!_newlineLeftCheckBox.isSelected())
        {
            return false;
        }
        else if (!_newlineRightCheckBox.isSelected())
        {
            return false;
        }
        else if (!TWO.equals(_indentLeftBraceComboBox.getSelectedItem()))
        {
            return false;
        }
        else if (!TWO.equals(_indentRightBraceComboBox.getSelectedItem()))
        {
            return false;
        }
        else if (!ZERO.equals(_indentAfterRightBraceComboBox.getSelectedItem()))
        {
            return false;
        }

        return true;
    }


    /**
     * Determines whether the current settings forms the Sun brace style.
     *
     * @return <code>true</code> if the the current settings form the Sun brace style.
     */
    private boolean isSunStyle()
    {
        if (_newlineLeftCheckBox.isSelected())
        {
            return false;
        }
        else if (_newlineRightCheckBox.isSelected())
        {
            return false;
        }
        else if (!ONE.equals(_indentLeftBraceComboBox.getSelectedItem()))
        {
            return false;
        }
        else if (!ZERO.equals(_indentRightBraceComboBox.getSelectedItem()))
        {
            return false;
        }
        else if (!ONE.equals(_indentAfterRightBraceComboBox.getSelectedItem()))
        {
            return false;
        }

        return true;
    }


    /**
     * Initializes the misc pane.
     *
     * @return the misc pane.
     */
    private JPanel createMiscPane()
    {
        Object[] items = createItemList(new int[] { 0, 1, 2, 3, 4, 5 });
        ComboBoxPanelCheckBox emptyBraces =
            new NumberComboBoxPanelCheckBox(
                this.bundle.getString("CHK_CUDDLE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_EMPTY_CUDDLE,
                    ConventionDefaults.BRACE_EMPTY_CUDDLE),
                this.bundle.getString("LBL_SPACE_BEFORE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_BRACE_CUDDLED,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_BRACE_CUDDLED)));
        _cuddleEmptyBracesCheckBox = emptyBraces.getCheckBox();
        _cuddleEmptyBracesCheckBox.addActionListener(this.trigger);
        _cuddleEmptyBracesComboBox = emptyBraces.getComboBoxPanel().getComboBox();
        _cuddleEmptyBracesComboBox.addActionListener(this.trigger);

        JPanel emptyPanel = new JPanel();
        GridBagLayout emptyPanelLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        emptyPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_EMPTY_BRACES" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        emptyPanel.setLayout(emptyPanelLayout);
        _insertEmptyStatementCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INSERT_EMPTY" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_EMPTY_INSERT_STATEMENT,
                    ConventionDefaults.BRACE_EMPTY_INSERT_STATEMENT));
        _insertEmptyStatementCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        emptyPanelLayout.setConstraints(_insertEmptyStatementCheckBox, c);
        emptyPanel.add(_insertEmptyStatementCheckBox);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        emptyPanelLayout.setConstraints(emptyBraces, c);
        emptyPanel.add(emptyBraces);

        EmptyButtonGroup emptyButtonGroup = new EmptyButtonGroup();
        emptyButtonGroup.add(_insertEmptyStatementCheckBox);
        emptyButtonGroup.add(_cuddleEmptyBracesCheckBox);

        JPanel insertBracesSettingsPage = new JPanel();
        insertBracesSettingsPage.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        insertBracesSettingsPage.setBorder(
            BorderFactory.createTitledBorder(
                this.bundle.getString("BDR_INSERT_BRACES" /* NOI18N */)));
        _insertIfElseCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_IF_ELSE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_INSERT_IF_ELSE,
                    ConventionDefaults.BRACE_INSERT_IF_ELSE));
        _insertIfElseCheckBox.addActionListener(this.trigger);
        insertBracesSettingsPage.add(_insertIfElseCheckBox);
        _insertForCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_FOR" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_INSERT_FOR, ConventionDefaults.BRACE_INSERT_FOR));
        _insertForCheckBox.addActionListener(this.trigger);
        insertBracesSettingsPage.add(_insertForCheckBox);
        _insertWhileCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WHILE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_INSERT_WHILE,
                    ConventionDefaults.BRACE_INSERT_WHILE));
        _insertWhileCheckBox.addActionListener(this.trigger);
        insertBracesSettingsPage.add(_insertWhileCheckBox);
        _insertDoWhileCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_DO_WHILE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_INSERT_DO_WHILE,
                    ConventionDefaults.BRACE_INSERT_DO_WHILE));
        _insertDoWhileCheckBox.addActionListener(this.trigger);
        insertBracesSettingsPage.add(_insertDoWhileCheckBox);

        JPanel removeBracesSettingsPage = new JPanel();
        removeBracesSettingsPage.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        removeBracesSettingsPage.setBorder(
            BorderFactory.createTitledBorder(
                this.bundle.getString("BDR_REMOVE_BRACES" /* NOI18N */)));
        _removeIfElseCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_IF_ELSE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_REMOVE_IF_ELSE,
                    ConventionDefaults.BRACE_REMOVE_IF_ELSE));
        _removeIfElseCheckBox.addActionListener(this.trigger);
        removeBracesSettingsPage.add(_removeIfElseCheckBox);
        _removeForCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_FOR" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_REMOVE_FOR, ConventionDefaults.BRACE_REMOVE_FOR));
        _removeForCheckBox.addActionListener(this.trigger);
        removeBracesSettingsPage.add(_removeForCheckBox);
        _removeWhileCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WHILE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_REMOVE_WHILE,
                    ConventionDefaults.BRACE_REMOVE_WHILE));
        _removeWhileCheckBox.addActionListener(this.trigger);
        removeBracesSettingsPage.add(_removeWhileCheckBox);
        _removeDoWhileCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_DO_WHILE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_REMOVE_DO_WHILE,
                    ConventionDefaults.BRACE_REMOVE_DO_WHILE));
        _removeDoWhileCheckBox.addActionListener(this.trigger);
        removeBracesSettingsPage.add(_removeDoWhileCheckBox);
        _removeBlockCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BLOCKS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_REMOVE_BLOCK,
                    ConventionDefaults.BRACE_REMOVE_BLOCK));
        _removeBlockCheckBox.addActionListener(this.trigger);
        removeBracesSettingsPage.add(_removeBlockCheckBox);

        ButtonGroup ifElseButtonGroup = new EmptyButtonGroup();
        ifElseButtonGroup.add(_insertIfElseCheckBox);
        ifElseButtonGroup.add(_removeIfElseCheckBox);

        ButtonGroup doWhileButtonGroup = new EmptyButtonGroup();
        doWhileButtonGroup.add(_insertDoWhileCheckBox);
        doWhileButtonGroup.add(_removeDoWhileCheckBox);

        ButtonGroup forButtonGroup = new EmptyButtonGroup();
        forButtonGroup.add(_insertForCheckBox);
        forButtonGroup.add(_removeForCheckBox);

        ButtonGroup whileButtonGroup = new EmptyButtonGroup();
        whileButtonGroup.add(_insertWhileCheckBox);
        whileButtonGroup.add(_removeWhileCheckBox);

        GridBagLayout miscPaneLayout = new GridBagLayout();
        JPanel miscPane = new JPanel();
        miscPane.setLayout(miscPaneLayout);

        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        c.insets.bottom = 0;

        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscPaneLayout.setConstraints(insertBracesSettingsPage, c);
        miscPane.add(insertBracesSettingsPage);

        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscPaneLayout.setConstraints(removeBracesSettingsPage, c);
        miscPane.add(removeBracesSettingsPage);

        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscPaneLayout.setConstraints(emptyPanel, c);
        miscPane.add(emptyPanel);

        return miscPane;
    }


    /**
     * Creates the pane with the general brace style options.
     *
     * @return general brace style pane.
     */
    private JPanel createStylePane()
    {
        JPanel alignmentPanel = new JPanel();
        alignmentPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_WRAPPING" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        alignmentPanel.setLayout(new BoxLayout(alignmentPanel, BoxLayout.Y_AXIS));

        _newlineLeftCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_NEWLINE_BEFORE_LEFT" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_NEWLINE_LEFT,
                    ConventionDefaults.BRACE_NEWLINE_LEFT));
        _newlineLeftCheckBox.addActionListener(this.trigger);
        alignmentPanel.add(_newlineLeftCheckBox);

        _newlineRightCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_NEWLINE_AFTER_RIGHT" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_NEWLINE_RIGHT,
                    ConventionDefaults.BRACE_NEWLINE_RIGHT));
        _newlineRightCheckBox.addActionListener(this.trigger);
        alignmentPanel.add(_newlineRightCheckBox);

        _treatDifferentCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_TREAT_DIFFERENT" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_TREAT_DIFFERENT,
                    ConventionDefaults.BRACE_TREAT_DIFFERENT));
        _treatDifferentCheckBox.addActionListener(this.trigger);
        alignmentPanel.add(_treatDifferentCheckBox);

        _treatDifferentIfWrappedCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_NEWLINE_BEFORE_LEFT_WRAPPED" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.BRACE_TREAT_DIFFERENT_IF_WRAPPED,
                    ConventionDefaults.BRACE_TREAT_DIFFERENT_IF_WRAPPED));
        _treatDifferentIfWrappedCheckBox.addActionListener(this.trigger);
        alignmentPanel.add(_treatDifferentIfWrappedCheckBox);

        String[] items = createItemList(new int[] { 0, 1, 2, 3, 4, 5 });
        _indentLeftBraceComboBoxPnl =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE_LEFT_BRACE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_BRACE_LEFT,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_BRACE_LEFT)));
        _indentLeftBraceComboBox = _indentLeftBraceComboBoxPnl.getComboBox();
        _indentLeftBraceComboBox.addActionListener(this.trigger);
        _indentRightBraceComboBoxPnl =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE_RIGHT_BRACE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_BRACE_RIGHT,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_BRACE_RIGHT)));
        _indentRightBraceComboBox = _indentRightBraceComboBoxPnl.getComboBox();
        _indentRightBraceComboBox.addActionListener(this.trigger);
        _indentAfterRightBraceComboBoxPnl =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_AFTER_RIGHT_BRACE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_BRACE_RIGHT_AFTER,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_BRACE_RIGHT_AFTER)));
        _indentAfterRightBraceComboBox = _indentAfterRightBraceComboBoxPnl.getComboBox();
        _indentAfterRightBraceComboBox.addActionListener(this.trigger);

        JPanel stylesPanel = new JPanel();
        stylesPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_STYLES" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout stylesLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        stylesPanel.setLayout(stylesLayout);
        _cStyleCheckBox =
            new JCheckBox(this.bundle.getString("CHK_C_STYLE" /* NOI18N */));
        _cStyleCheckBox.setActionCommand(STYLE_C);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        stylesLayout.setConstraints(_cStyleCheckBox, c);
        stylesPanel.add(_cStyleCheckBox);
        _sunStyleCheckBox =
            new JCheckBox(this.bundle.getString("CHK_SUN_STYLE" /* NOI18N */));
        _sunStyleCheckBox.setActionCommand(STYLE_SUN);
        SwingHelper.setConstraints(
            c, 1, 0, 1, GridBagConstraints.REMAINDER, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        stylesLayout.setConstraints(_sunStyleCheckBox, c);
        stylesPanel.add(_sunStyleCheckBox);
        _gnuStyleCheckBox =
            new JCheckBox(this.bundle.getString("CHK_GNU_STYLE" /* NOI18N */));
        _gnuStyleCheckBox.setActionCommand(STYLE_GNU);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        stylesLayout.setConstraints(_gnuStyleCheckBox, c);
        stylesPanel.add(_gnuStyleCheckBox);
        _customStyleCheckBox =
            new JCheckBox(this.bundle.getString("CHK_CUSTOM_STYLE" /* NOI18N */));
        _customStyleCheckBox.setActionCommand(STYLE_CUSTOM);
        SwingHelper.setConstraints(
            c, 1, 1, 1, GridBagConstraints.REMAINDER, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        stylesLayout.setConstraints(_customStyleCheckBox, c);
        stylesPanel.add(_customStyleCheckBox);

        if (isSunStyle())
        {
            _sunStyleCheckBox.setSelected(true);
            setCustomMode(false);
            _treatDifferentCheckBox.setEnabled(true);
            _treatDifferentIfWrappedCheckBox.setEnabled(true);
        }
        else if (isCStyle())
        {
            _cStyleCheckBox.setSelected(true);
            setCustomMode(false);
            _treatDifferentCheckBox.setEnabled(false);
            _treatDifferentCheckBox.setSelected(false);
        }
        else if (isGnuStyle())
        {
            _gnuStyleCheckBox.setSelected(true);
            setCustomMode(false);
            _treatDifferentCheckBox.setEnabled(true);
        }
        else
        {
            _customStyleCheckBox.setSelected(true);
        }

        ButtonGroup group = new ButtonGroup();
        group.add(_cStyleCheckBox);
        group.add(_sunStyleCheckBox);
        group.add(_gnuStyleCheckBox);
        group.add(_customStyleCheckBox);

        ActionListener buttonHandler =
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    if (ev.getActionCommand() == STYLE_SUN)
                    {
                        setCustomMode(false);
                        _newlineLeftCheckBox.setSelected(false);
                        _newlineRightCheckBox.setSelected(false);
                        _indentLeftBraceComboBox.setSelectedItem(ONE);
                        _indentRightBraceComboBox.setSelectedItem(ZERO);
                        _indentAfterRightBraceComboBox.setSelectedItem(ONE);
                        _treatDifferentCheckBox.setEnabled(true);
                        _treatDifferentIfWrappedCheckBox.setEnabled(true);
                    }
                    else if (ev.getActionCommand() == STYLE_C)
                    {
                        setCustomMode(false);
                        _newlineLeftCheckBox.setSelected(true);
                        _newlineRightCheckBox.setSelected(true);
                        _indentLeftBraceComboBox.setSelectedItem(ZERO);
                        _indentRightBraceComboBox.setSelectedItem(ZERO);
                        _indentAfterRightBraceComboBox.setSelectedItem(ZERO);
                        _treatDifferentCheckBox.setSelected(false);
                        _treatDifferentIfWrappedCheckBox.setSelected(false);
                    }
                    else if (ev.getActionCommand() == STYLE_GNU)
                    {
                        setCustomMode(false);
                        _newlineLeftCheckBox.setSelected(true);
                        _newlineRightCheckBox.setSelected(true);
                        _indentLeftBraceComboBox.setSelectedItem(TWO);
                        _indentRightBraceComboBox.setSelectedItem(TWO);
                        _indentAfterRightBraceComboBox.setSelectedItem(ZERO);
                        _treatDifferentCheckBox.setEnabled(true);
                        _treatDifferentIfWrappedCheckBox.setSelected(false);
                    }
                    else
                    {
                        setCustomMode(true);
                    }
                }
            };

        _cStyleCheckBox.addActionListener(buttonHandler);
        _sunStyleCheckBox.addActionListener(buttonHandler);
        _gnuStyleCheckBox.addActionListener(buttonHandler);
        _customStyleCheckBox.addActionListener(buttonHandler);

        JPanel whitespacePanel = new JPanel();
        whitespacePanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_WHITESPACE" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout whitespacePanelLayout = new GridBagLayout();
        whitespacePanel.setLayout(whitespacePanelLayout);
        c.insets.top = 0;
        c.insets.bottom = 0;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        whitespacePanelLayout.setConstraints(_indentLeftBraceComboBoxPnl, c);
        whitespacePanel.add(_indentLeftBraceComboBoxPnl);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        whitespacePanelLayout.setConstraints(_indentRightBraceComboBoxPnl, c);
        whitespacePanel.add(_indentRightBraceComboBoxPnl);
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        whitespacePanelLayout.setConstraints(_indentAfterRightBraceComboBoxPnl, c);
        whitespacePanel.add(_indentAfterRightBraceComboBoxPnl);

        JPanel stylePane = new JPanel();
        GridBagLayout styleLayout = new GridBagLayout();
        stylePane.setLayout(styleLayout);
        c.insets.top = 10;
        c.insets.bottom = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        styleLayout.setConstraints(stylesPanel, c);
        stylePane.add(stylesPanel);
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        styleLayout.setConstraints(alignmentPanel, c);
        stylePane.add(alignmentPanel);
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        styleLayout.setConstraints(whitespacePanel, c);
        stylePane.add(whitespacePanel);

        return stylePane;
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        _tabbedPane = new JTabbedPane();
        _tabbedPane.add(
            createStylePane(), this.bundle.getString("TAB_GENERAL" /* NOI18N */));
        _tabbedPane.add(createMiscPane(), this.bundle.getString("TAB_MISC" /* NOI18N */));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(_tabbedPane, BorderLayout.CENTER);

        if (getContainer() != null)
        {
            _tabbedPane.addChangeListener(
                new ChangeListener()
                {
                    public void stateChanged(ChangeEvent ev)
                    {
                        String text = getContainer().loadPreview(getPreviewFileName());
                        getContainer().getPreview().setText(text);
                    }
                });
        }
    }
}
