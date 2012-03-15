/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.StringHelper;


/**
 * Settings page for the Jalopy printer Javadoc settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
public class JavadocSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final int ROW_CLASS = 0;
    private static final int ROW_CTOR = 1;
    private static final int ROW_METHOD = 2;
    private static final int ROW_VARIABLE = 3;
    private static final int COL_PUBLIC = 1;
    private static final int COL_PROTECTED = 2;
    private static final int COL_DEFAULT = 3;
    private static final int COL_PRIVATE = 4;
    private static final String TPL_CLASS = "Class" /* NOI18N */;
    private static final String TPL_INTERFACE = "Interface" /* NOI18N */;
    private static final String TPL_CTOR = "Constructor" /* NOI18N */;
    private static final String TPL_METHOD = "Method" /* NOI18N */;
    private static final String TPL_FIELD = "Field" /* NOI18N */;
    private static final String LINE_SEPARATOR = "\n" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    private AddRemoveList _inlineTagsList;
    private AddRemoveList _standardTagsList;
    private DataModel _tableModel;
    JCheckBox _checkTagsCheckBox;
    JCheckBox _checkThrowsTagsCheckBox;
    JCheckBox _checkDontJavadocIfMlBox;
    private JCheckBox _createInnerCheckBox;
    JCheckBox _parseCheckBox;
    JCheckBox _parseDescriptionCheckBox;
    JCheckBox _singleLineFieldCommentsCheckBox;
    JCheckBox _braceCommentsCheckBox;
    Pattern _bottomTextPattern;
    Pattern _exceptionPattern;
    Pattern _paramPattern;
    Pattern _returnPattern;
    Pattern _tagNamePattern;
    Pattern _templatePattern;
    Pattern _topTextPattern;
    TemplateContainer _templatesContainer;
    boolean _disposed;
    {
        //PatternCompiler compiler = new Perl5Compiler();

            _tagNamePattern = Pattern.compile("@[a-zA-Z]+" /* NOI18N */);
            _topTextPattern =
				Pattern.compile(
                    "\\/\\*\\*(?:.*)+\\n\\s*\\*\\s*(.*)(?:\\n)*" /* NOI18N */);
            _paramPattern =
				Pattern.compile(
                    "\\s*\\*\\s*@param\\s+\\$paramType\\$.*" /*\\s+(?:.+) NOI18N */);
            _returnPattern =
				Pattern.compile("\\s*\\*\\s*@return.*" /*\\s+(?:.+) NOI18N */);
            _exceptionPattern =
				Pattern.compile(
                    "\\s*\\*\\s*@(?:throws|exception)\\s+\\$exceptionType\\$.*" /*\\s+(?:.+) NOI18N */);
            _bottomTextPattern = Pattern.compile("\\s*(?:\\*)+/" /* NOI18N */);
            _templatePattern =
				Pattern.compile("\\/\\*\\*[^*]*\\*+([^//*][^*]*\\*+)*\\/" /* NOI18N */);
    }

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new JavadocSettingsPage object.
     */
    public JavadocSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new JavadocSettingsPage.
     *
     * @param container the parent container.
     */
    JavadocSettingsPage(SettingsContainer container)
    {
        super(container);
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Frees allocated resources.
     */
    public void dispose()
    {
        if (!_disposed)
        {
            _inlineTagsList = null;
            _standardTagsList = null;
            _tableModel = null;
            _checkTagsCheckBox = null;
            _createInnerCheckBox = null;
            _parseCheckBox = null;
            _parseDescriptionCheckBox = null;
            _templatesContainer.dispose();
            _disposed = true;
        }
    }


    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
        int classMask = 0;

        if (((Boolean) _tableModel.data[ROW_CLASS][COL_PUBLIC]).booleanValue())
        {
            classMask += Modifier.PUBLIC;
        }

        if (((Boolean) _tableModel.data[ROW_CLASS][COL_PROTECTED]).booleanValue())
        {
            classMask += Modifier.PROTECTED;
        }

        if (((Boolean) _tableModel.data[ROW_CLASS][COL_DEFAULT]).booleanValue())
        {
            classMask += Modifier.FINAL;
        }

        if (((Boolean) _tableModel.data[ROW_CLASS][COL_PRIVATE]).booleanValue())
        {
            classMask += Modifier.PRIVATE;
        }

        int ctorMask = 0;

        if (((Boolean) _tableModel.data[ROW_CTOR][COL_PUBLIC]).booleanValue())
        {
            ctorMask += Modifier.PUBLIC;
        }

        if (((Boolean) _tableModel.data[ROW_CTOR][COL_PROTECTED]).booleanValue())
        {
            ctorMask += Modifier.PROTECTED;
        }

        if (((Boolean) _tableModel.data[ROW_CTOR][COL_DEFAULT]).booleanValue())
        {
            ctorMask += Modifier.FINAL;
        }

        if (((Boolean) _tableModel.data[ROW_CTOR][COL_PRIVATE]).booleanValue())
        {
            ctorMask += Modifier.PRIVATE;
        }

        int methodMask = 0;

        if (((Boolean) _tableModel.data[ROW_METHOD][COL_PUBLIC]).booleanValue())
        {
            methodMask += Modifier.PUBLIC;
        }

        if (((Boolean) _tableModel.data[ROW_METHOD][COL_PROTECTED]).booleanValue())
        {
            methodMask += Modifier.PROTECTED;
        }

        if (((Boolean) _tableModel.data[ROW_METHOD][COL_DEFAULT]).booleanValue())
        {
            methodMask += Modifier.FINAL;
        }

        if (((Boolean) _tableModel.data[ROW_METHOD][COL_PRIVATE]).booleanValue())
        {
            methodMask += Modifier.PRIVATE;
        }

        int variableMask = 0;

        if (((Boolean) _tableModel.data[ROW_VARIABLE][COL_PUBLIC]).booleanValue())
        {
            variableMask += Modifier.PUBLIC;
        }

        if (((Boolean) _tableModel.data[ROW_VARIABLE][COL_PROTECTED]).booleanValue())
        {
            variableMask += Modifier.PROTECTED;
        }

        if (((Boolean) _tableModel.data[ROW_VARIABLE][COL_DEFAULT]).booleanValue())
        {
            variableMask += Modifier.FINAL;
        }

        if (((Boolean) _tableModel.data[ROW_VARIABLE][COL_PRIVATE]).booleanValue())
        {
            variableMask += Modifier.PRIVATE;
        }

        this.settings.putInt(ConventionKeys.COMMENT_JAVADOC_CTOR_MASK, ctorMask);
        this.settings.putInt(ConventionKeys.COMMENT_JAVADOC_METHOD_MASK, methodMask);
        this.settings.putInt(ConventionKeys.COMMENT_JAVADOC_CLASS_MASK, classMask);
        this.settings.putInt(ConventionKeys.COMMENT_JAVADOC_VARIABLE_MASK, variableMask);
        this.settings.putBoolean(
            ConventionKeys.COMMENT_JAVADOC_FIELDS_SHORT,
            _singleLineFieldCommentsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.BRACE_ADD_COMMENT,
            _braceCommentsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.COMMENT_JAVADOC_PARSE, _parseCheckBox.isSelected());
        this.settings.putBoolean(ConventionKeys.COMMENT_JAVADOC_PARSE_DESCRIPTION, _parseDescriptionCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS, _checkTagsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS_THROWS,
            _checkThrowsTagsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.DONT_COMMENT_JAVADOC_WHEN_ML,
            _checkDontJavadocIfMlBox.isSelected());
        
        this.settings.putBoolean(
            ConventionKeys.COMMENT_JAVADOC_INNER_CLASS, _createInnerCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.COMMENT_JAVADOC_TAGS_STANDARD,
            encodeTags(_standardTagsList.getValues()));
        this.settings.put(
            ConventionKeys.COMMENT_JAVADOC_TAGS_INLINE,
            encodeTags(_inlineTagsList.getValues()));

        _templatesContainer.updateSettings();
    }


    /**
     * {@inheritDoc}
     */
    public void validateSettings()
      throws ValidationException
    {
        _templatesContainer.validateSettings();
    }


    private JPanel createGeneralPane()
    {
        JPanel generalPanel = new JPanel();
        GridBagLayout generalLayout = new GridBagLayout();
        generalPanel.setLayout(generalLayout);
        generalPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_GENERAL" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagConstraints c = new GridBagConstraints();

        _parseCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_PARSE_COMMENTS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_PARSE,
                    ConventionDefaults.COMMENT_JAVADOC_PARSE));
        _parseCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.RELATIVE, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        generalLayout.setConstraints(_parseCheckBox, c);
        generalPanel.add(_parseCheckBox);
        _checkDontJavadocIfMlBox =
            new JCheckBox("Dont addd if ML",
                this.settings.getBoolean(
                    ConventionKeys.DONT_COMMENT_JAVADOC_WHEN_ML,
                    ConventionDefaults.DONT_COMMENT_JAVADOC_WHEN_ML));
        _checkDontJavadocIfMlBox.setToolTipText(this.bundle.getString("DONT_COMMENT_JAVADOC_WHEN_ML" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        generalLayout.setConstraints(_checkDontJavadocIfMlBox, c);
        generalPanel.add(_checkDontJavadocIfMlBox);

        _checkTagsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_CORRECT_TAGS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS,
                    ConventionDefaults.COMMENT_JAVADOC_CHECK_TAGS));
        _checkTagsCheckBox.addActionListener(this.trigger);

        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        generalLayout.setConstraints(_checkTagsCheckBox, c);
        generalPanel.add(_checkTagsCheckBox);

        _checkThrowsTagsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_CORRECT_THROWS_TAGS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS_THROWS,
                    ConventionDefaults.COMMENT_JAVADOC_CHECK_TAGS_THROWS));
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        generalLayout.setConstraints(_checkThrowsTagsCheckBox, c);
        generalPanel.add(_checkThrowsTagsCheckBox);
        

        _parseCheckBox.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    _parseDescriptionCheckBox.setEnabled(_parseCheckBox.isSelected());
                    _checkTagsCheckBox.setEnabled(_parseCheckBox.isSelected());
                    _checkThrowsTagsCheckBox.setEnabled(_parseCheckBox.isSelected());
                    _singleLineFieldCommentsCheckBox.setEnabled(_parseCheckBox.isSelected());
                }
            });

        _parseDescriptionCheckBox =
            new JCheckBox(
                "Parse javadoc description",
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_PARSE,
                    ConventionDefaults.COMMENT_JAVADOC_PARSE));
        _parseDescriptionCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        generalLayout.setConstraints(_checkTagsCheckBox, c);
        generalPanel.add(_parseDescriptionCheckBox);

        _tableModel = new DataModel();

        JTable table = new JTable(_tableModel);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        //initializeColumnSizes(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(
            UIManager.getColor("Table.background" /* NOI18N */));

        int height = SwingHelper.getTableHeight(table);
        scrollPane.setPreferredSize(new Dimension(300, height + 17));

        JPanel createPanel = new JPanel();
        GridBagLayout createLayout = new GridBagLayout();
        createPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_GENERATION" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        createPanel.setLayout(createLayout);

        c.insets.top = 5;
        SwingHelper.setConstraints(
            c, 0, 0, 8, 8, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        createLayout.setConstraints(scrollPane, c);
        createPanel.add(scrollPane);

        _createInnerCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INCLUDE_INNER" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_INNER_CLASS,
                    ConventionDefaults.COMMENT_JAVADOC_INNER_CLASS));
        _createInnerCheckBox.addActionListener(this.trigger);
        c.insets.bottom = 0;
        SwingHelper.setConstraints(
            c, 0, 9, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        createLayout.setConstraints(_createInnerCheckBox, c);
        createPanel.add(_createInnerCheckBox);

        JPanel miscPanel = new JPanel();
        miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.Y_AXIS));
        miscPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_MISC" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        _singleLineFieldCommentsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_FIELD_COMMENTS_IN_SINGLE_LINE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_FIELDS_SHORT,
                    ConventionDefaults.COMMENT_JAVADOC_FIELDS_SHORT));
        _singleLineFieldCommentsCheckBox.addActionListener(this.trigger);
        miscPanel.add(_singleLineFieldCommentsCheckBox);

        _braceCommentsCheckBox =
            new JCheckBox(
                "Add comments after closing braces",
                this.settings.getBoolean(
                    ConventionKeys.BRACE_ADD_COMMENT,
                    ConventionDefaults.BRACE_ADD_COMMENT));
        _braceCommentsCheckBox.addActionListener(this.trigger);
        miscPanel.add(_braceCommentsCheckBox);
        
         _checkTagsCheckBox.setEnabled(_parseCheckBox.isSelected());
         _singleLineFieldCommentsCheckBox.setEnabled(_parseCheckBox.isSelected());
         _parseDescriptionCheckBox.setEnabled(_parseCheckBox.isSelected());

        GridBagLayout layout = new GridBagLayout();
        JPanel panel = new JPanel();
        panel.setLayout(layout);

        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(generalPanel, c);
        panel.add(generalPanel);

        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(createPanel, c);
        panel.add(createPanel);

        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(miscPanel, c);
        panel.add(miscPanel);

        _tableModel.addTableModelListener(
            new TableModelListener()
            {
                public void tableChanged(TableModelEvent ev)
                {
                    trigger.actionPerformed(null);
                }
            });

        return panel;
    }


    /**
     * Returns the custom tags pane.
     *
     * @return the custom tags pane.
     */
    private JPanel createTagsPane()
    {
        GridBagLayout tagsPanelLayout = new GridBagLayout();
        JPanel tagsPanel = new JPanel();
        tagsPanel.setLayout(tagsPanelLayout);

        JPanel standardTagsPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        GridBagLayout standardPanelLayout = new GridBagLayout();
        standardTagsPanel.setLayout(standardPanelLayout);
        standardTagsPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_STANDARD_TAGS" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        String standardTagsString =
            this.settings.get(
                ConventionKeys.COMMENT_JAVADOC_TAGS_STANDARD,
                ConventionDefaults.COMMENT_JAVADOC_TAGS_STANDARD);
        Collection standardTags = decodeTags(standardTagsString);
        _standardTagsList =
            new AddRemoveList(
                this.bundle.getString("TLE_ADD_NEW_STANDARD" /* NOI18N */),
                this.bundle.getString("LBL_ENTER_NAME" /* NOI18N */), standardTags);

        ListDataHandler dataHandler = new ListDataHandler();
        _standardTagsList.getModel().addListDataListener(dataHandler);

        JScrollPane standardTagsScrollPane = new JScrollPane(_standardTagsList);
        c.insets.top = 0;
        c.insets.bottom = 0;
        c.insets.left = 0;
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 0, 0, 10, 5, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        standardPanelLayout.setConstraints(standardTagsScrollPane, c);
        standardTagsPanel.add(standardTagsScrollPane);

        JButton standardTagsAddButton = _standardTagsList.getAddButton();
        c.insets.left = 5;
        c.insets.bottom = 2;
        SwingHelper.setConstraints(
            c, 11, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        standardPanelLayout.setConstraints(standardTagsAddButton, c);
        standardTagsPanel.add(standardTagsAddButton);

        JButton standardTagsRemoveButton = _standardTagsList.getRemoveButton();
        SwingHelper.setConstraints(
            c, 11, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        standardPanelLayout.setConstraints(standardTagsRemoveButton, c);
        standardTagsPanel.add(standardTagsRemoveButton);

        JPanel inlineTagsPanel = new JPanel();
        GridBagLayout inlinePanelLayout = new GridBagLayout();
        inlineTagsPanel.setLayout(inlinePanelLayout);
        inlineTagsPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_INLINE_TAGS" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        String inlineTagsString =
            this.settings.get(
                ConventionKeys.COMMENT_JAVADOC_TAGS_INLINE,
                ConventionDefaults.COMMENT_JAVADOC_TAGS_INLINE);
        Collection inlineTags = decodeTags(inlineTagsString);
        _inlineTagsList =
            new AddRemoveList(
                this.bundle.getString("TLE_ADD_NEW_INLINE" /* NOI18N */),
                this.bundle.getString("LBL_ENTER_NAME" /* NOI18N */), inlineTags);
        _inlineTagsList.getModel().addListDataListener(dataHandler);
        c.insets.top = 0;
        c.insets.bottom = 0;
        c.insets.left = 0;
        c.insets.right = 0;

        JScrollPane inlineTagsScrollPane = new JScrollPane(_inlineTagsList);
        SwingHelper.setConstraints(
            c, 0, 0, 10, 5, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        inlinePanelLayout.setConstraints(inlineTagsScrollPane, c);
        inlineTagsPanel.add(inlineTagsScrollPane);

        JButton inlineTagsAddButton = _inlineTagsList.getAddButton();
        c.insets.left = 5;
        c.insets.bottom = 2;
        SwingHelper.setConstraints(
            c, 11, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        inlinePanelLayout.setConstraints(inlineTagsAddButton, c);
        inlineTagsPanel.add(inlineTagsAddButton);

        JButton inlineTagsRemoveButton = _inlineTagsList.getRemoveButton();
        SwingHelper.setConstraints(
            c, 11, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        inlinePanelLayout.setConstraints(inlineTagsRemoveButton, c);
        inlineTagsPanel.add(inlineTagsRemoveButton);

        c.insets.top = 10;
        c.insets.bottom = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        tagsPanelLayout.setConstraints(standardTagsPanel, c);
        tagsPanel.add(standardTagsPanel);

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        tagsPanelLayout.setConstraints(inlineTagsPanel, c);
        tagsPanel.add(inlineTagsPanel);

        return tagsPanel;
    }


    /**
     * Creates a panel that allows the user to specify the different Javadoc templates.
     *
     * @return the templates panel.
     *
     * @since 1.0b8
     */
    private JPanel createTemplatesPane()
    {
        final JPanel templatesPanel = new JPanel();
        templatesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagLayout templatesLayout = new GridBagLayout();
        templatesPanel.setLayout(templatesLayout);

        GridBagConstraints c = new GridBagConstraints();
        String[] items = { TPL_CLASS, TPL_INTERFACE, TPL_CTOR, TPL_METHOD, TPL_FIELD };
        ComboBoxPanel chooseTemplateComboBoxPanel =
            new ComboBoxPanel(
                this.bundle.getString("LBL_SHOW_TEMPLATE_FOR" /* NOI18N */), items,
                TPL_CLASS);
        final JComboBox chooseTemplateComboBox =
            chooseTemplateComboBoxPanel.getComboBox();
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        templatesLayout.setConstraints(chooseTemplateComboBoxPanel, c);
        templatesPanel.add(chooseTemplateComboBoxPanel);

        _templatesContainer = new TemplateContainer(TPL_METHOD);
        c.insets.top = 5;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        templatesLayout.setConstraints(_templatesContainer, c);
        templatesPanel.add(_templatesContainer);
        chooseTemplateComboBox.addItemListener(
            new ItemListener()
            {
                int index = 3; // index of the currently displayed item

                public void itemStateChanged(ItemEvent ev)
                {
                    switch (ev.getStateChange())
                    {
                        case ItemEvent.DESELECTED :

                            try
                            {
                                validateSettings();
                            }
                            catch (ValidationException ex)
                            {
                                // revert the selection
                                chooseTemplateComboBox.setSelectedIndex(this.index);

                                return;
                            }

                            String name =
                                (String) chooseTemplateComboBox.getSelectedItem();

                            if ((name != null) && (name.length() > 0))
                            {
                                if (!name.equals(_templatesContainer.getCurrent()))
                                {
                                    _templatesContainer.switchPanels(name);
                                    invalidate();
                                    repaint();
                                }
                            }

                            index = chooseTemplateComboBox.getSelectedIndex();

                            break;
                    }
                }
            });

        chooseTemplateComboBox.setSelectedIndex(3);

        return templatesPanel;
    }


    /**
     * Decodes the given encoded tags string.
     *
     * @param tags encoded tags string.
     *
     * @return collection of the tags.
     *
     * @since 1.0b7
     */
    private Collection decodeTags(String tags)
    {
        List result = new ArrayList();

        for (StringTokenizer i = new StringTokenizer(tags, DELIMETER);
            i.hasMoreElements();)
        {
            result.add(i.nextToken());
        }

        return result;
    }


    /**
     * Encodes the given tag list as a string.
     *
     * @param tags tags to encode.
     *
     * @return encode (string delimeted) tag list.
     *
     * @since 1.0b7
     */
    private String encodeTags(Object[] tags)
    {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < tags.length; i++)
        {
            buf.append(tags[i]);
            buf.append(DELIMETER);
        }

        if (buf.length() > 0)
        {
            buf.deleteCharAt(buf.length() - 1);
        }

        return buf.toString();
    }


    private void initialize()
    {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add(createGeneralPane(), this.bundle.getString("TAB_GENERAL" /* NOI18N */));
        tabs.add(
            createTemplatesPane(), this.bundle.getString("TAB_TEMPLATES" /* NOI18N */));
        tabs.add(createTagsPane(), this.bundle.getString("TAB_CUSTOM_TAGS" /* NOI18N */));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(tabs, BorderLayout.CENTER);
    }


    /**
     * This method picks good column sizes.
     *
     * @param table table to initialize
     */
// TODO    private void initializeColumnSizes(JTable table)
//    {
//        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
//
//        for (int i = 1; i < 5; i++)
//        {
//            TableColumn column = table.getColumnModel().getColumn(i);
//            Component comp =
//                headerRenderer.getTableCellRendererComponent(
//                    null, column.getHeaderValue(), false, false, 0, 0);
//            int headerWidth = comp.getPreferredSize().width;
//            column.setPreferredWidth(headerWidth);
//        }
//    }

    //~ Inner Interfaces -----------------------------------------------------------------

    private static interface TemplatePanel
    {
        public void updateSettings();


        public void validateSettings()
          throws ValidationException;
    }

    //~ Inner Classes --------------------------------------------------------------------

    private class CtorTemplatePanel
        extends JPanel
        implements TemplatePanel
    {
        Convention settings = Convention.getInstance();
        JTextArea bottomTextArea;
        JTextArea exceptionTextArea;
        JTextArea parameterTextArea;
        JTextArea topTextArea;
        ResourceBundle bundle;

        public CtorTemplatePanel()
        {
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);

            this.bundle =
                ResourceBundle.getBundle("de.hunsicker.jalopy.swing.Bundle" /* NOI18N */);

            GridBagConstraints c = new GridBagConstraints();
            JLabel topLabel = new JLabel(bundle.getString("LBL_TOP" /* NOI18N */));
            SwingHelper.setConstraints(
                c, 0, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
            layout.setConstraints(topLabel, c);
            add(topLabel);
            this.topTextArea = new JTextArea(getTopTemplate());

            JScrollPane topScrollPane = new JScrollPane(this.topTextArea);
            SwingHelper.setConstraints(
                c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.2,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
            layout.setConstraints(topScrollPane, c);
            add(topScrollPane);

            JLabel paramLabel =
                new JLabel(bundle.getString("LBL_PARAMETER" /* NOI18N */));
            c.insets.top = 3;
            SwingHelper.setConstraints(
                c, 0, 2, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
            layout.setConstraints(paramLabel, c);
            add(paramLabel);

            this.parameterTextArea = new JTextArea(getParameterTemplate());

            JScrollPane parameterScrollPane = new JScrollPane(this.parameterTextArea);
            c.insets.top = 0;
            SwingHelper.setConstraints(
                c, 0, 3, GridBagConstraints.REMAINDER, 1, 1.0, 0.1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
            layout.setConstraints(parameterScrollPane, c);
            add(parameterScrollPane);

            JLabel exceptionLabel =
                new JLabel(bundle.getString("LBL_EXCEPTION" /* NOI18N */));
            c.insets.top = 3;
            SwingHelper.setConstraints(
                c, 0, 4, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0,
                0);
            layout.setConstraints(exceptionLabel, c);
            add(exceptionLabel);

            this.exceptionTextArea = new JTextArea(getExceptionTemplate());
            c.insets.top = 0;
            SwingHelper.setConstraints(
                c, 0, 5, GridBagConstraints.REMAINDER, 1, 1.0, 0.1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);

            JScrollPane exceptionScrollPane = new JScrollPane(this.exceptionTextArea);
            layout.setConstraints(exceptionScrollPane, c);
            add(exceptionScrollPane);

            JLabel bottomLabel = new JLabel(bundle.getString("LBL_BOTTOM" /* NOI18N */));
            c.insets.top = 3;
            SwingHelper.setConstraints(
                c, 0, 8, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0,
                0);
            layout.setConstraints(bottomLabel, c);
            add(bottomLabel);

            this.bottomTextArea = new JTextArea(getBottomTemplate());
            c.insets.top = 0;
            SwingHelper.setConstraints(
                c, 0, 9, GridBagConstraints.REMAINDER, 1, 1.0, 0.1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);

            JScrollPane bottomScrollPane = new JScrollPane(this.bottomTextArea);
            layout.setConstraints(bottomScrollPane, c);
            add(bottomScrollPane);
        }

        public void updateSettings()
        {
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_TOP,
                StringHelper.replace(
                    this.topTextArea.getText(), LINE_SEPARATOR, DELIMETER));
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM,
                StringHelper.replace(
                    this.parameterTextArea.getText(), LINE_SEPARATOR, DELIMETER));
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION,
                StringHelper.replace(
                    this.exceptionTextArea.getText(), LINE_SEPARATOR, DELIMETER));
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM,
                StringHelper.replace(
                    this.bottomTextArea.getText(), LINE_SEPARATOR, DELIMETER));
        }


        public void validateSettings()
          throws ValidationException
        {
            String topText = this.topTextArea.getText();

            if (!_topTextPattern.matcher(topText).matches())
            {
                Object[] args = { _topTextPattern.pattern() };

                JOptionPane.showMessageDialog(
                    SwingUtilities.windowForComponent(this),
                    MessageFormat.format(
                        this.bundle.getString("MSG_INVALID_TOP_FRAGMENT" /* NOI18N */),
                        args),
                    this.bundle.getString("TLE_INVALID_TOP_FRAGMENT" /* NOI18N */),
                    JOptionPane.ERROR_MESSAGE);

                throw new ValidationException();
            }

            String parameterText = this.parameterTextArea.getText();

            if (!_paramPattern.matcher(parameterText).matches())
            {
                Object[] args = { _paramPattern.pattern() };
                JOptionPane.showMessageDialog(
                    SwingUtilities.windowForComponent(this),
                    MessageFormat.format(
                        this.bundle.getString(
                            "MSG_INVALID_PARAMETER_FRAGMENT" /* NOI18N */), args),
                    this.bundle.getString("TLE_INVALID_PARAMETER_FRAGMENT" /* NOI18N */),
                    JOptionPane.ERROR_MESSAGE);

                throw new ValidationException();
            }

            String exceptionText = this.exceptionTextArea.getText();

            if (!_exceptionPattern.matcher(exceptionText).matches())
            {
                Object[] args = { _exceptionPattern.pattern() };
                JOptionPane.showMessageDialog(
                    SwingUtilities.windowForComponent(this),
                    MessageFormat.format(
                        this.bundle.getString(
                            "MSG_INVALID_EXCEPTION_FRAGMENT" /* NOI18N */), args),
                    this.bundle.getString("TLE_INVALID_EXCEPTION_FRAGMENT" /* NOI18N */),
                    JOptionPane.ERROR_MESSAGE);

                throw new ValidationException();
            }

            String bottomText = this.bottomTextArea.getText();

            if (!_bottomTextPattern.matcher(bottomText).matches())
            {
                Object[] args = { _bottomTextPattern.pattern() };

                JOptionPane.showMessageDialog(
                    SwingUtilities.windowForComponent(this),
                    MessageFormat.format(
                        this.bundle.getString("MSG_INVALID_BOTTOM_FRAGMENT" /* NOI18N */),
                        args),
                    this.bundle.getString("TLE_INVALID_BOTTOM_FRAGMENT" /* NOI18N */),
                    JOptionPane.ERROR_MESSAGE);

                throw new ValidationException();
            }
        }


        protected String getBottomTemplate()
        {
            return StringHelper.replace(
                this.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM), DELIMETER,
                LINE_SEPARATOR);
        }


        protected String getExceptionTemplate()
        {
            return StringHelper.replace(
                this.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION), DELIMETER,
                LINE_SEPARATOR);
        }


        protected String getParameterTemplate()
        {
            return StringHelper.replace(
                this.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM), DELIMETER,
                LINE_SEPARATOR);
        }


        protected String getTopTemplate()
        {
            return StringHelper.replace(
                this.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_TOP,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CTOR_TOP), DELIMETER,
                LINE_SEPARATOR);
        }
    }


    private class DataModel
        extends AbstractTableModel
    {
        final Convention settings = Convention.getInstance();
        final String[] columnNames =
        {
            "                      " /* NOI18N */, "public" /* NOI18N */,
            "protected" /* NOI18N */, "default" /* NOI18N */, "private" /* NOI18N */
        };
        final Object[][] data =
        {
            {
                JavadocSettingsPage.this.bundle.getString(
                    "LBL_CLASSES_INTERFACES" /* NOI18N */),
                new Boolean(
                    Modifier.isPublic(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_CLASS_MASK,
                            ConventionDefaults.COMMENT_JAVADOC_CLASS_MASK))),
                new Boolean(
                    Modifier.isProtected(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_CLASS_MASK, 0))),
                new Boolean(
                    Modifier.isFinal(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_CLASS_MASK, 0))),
                new Boolean(
                    Modifier.isPrivate(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_CLASS_MASK, 0)))
            },
            {
                JavadocSettingsPage.this.bundle.getString(
                    "LBL_CONSTRUCTORS" /* NOI18N */),
                new Boolean(
                    Modifier.isPublic(
                        this.settings.getInt(ConventionKeys.COMMENT_JAVADOC_CTOR_MASK, 0))),
                new Boolean(
                    Modifier.isProtected(
                        this.settings.getInt(ConventionKeys.COMMENT_JAVADOC_CTOR_MASK, 0))),
                new Boolean(
                    Modifier.isFinal(
                        this.settings.getInt(ConventionKeys.COMMENT_JAVADOC_CTOR_MASK, 0))),
                new Boolean(
                    Modifier.isPrivate(
                        this.settings.getInt(ConventionKeys.COMMENT_JAVADOC_CTOR_MASK, 0)))
            },
            {
                JavadocSettingsPage.this.bundle.getString("LBL_METHODS" /* NOI18N */),
                new Boolean(
                    Modifier.isPublic(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_METHOD_MASK, 0))),
                new Boolean(
                    Modifier.isProtected(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_METHOD_MASK, 0))),
                new Boolean(
                    Modifier.isFinal(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_METHOD_MASK, 0))),
                new Boolean(
                    Modifier.isPrivate(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_METHOD_MASK, 0)))
            },
            {
                JavadocSettingsPage.this.bundle.getString("LBL_VARIABLES" /* NOI18N */),
                new Boolean(
                    Modifier.isPublic(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_VARIABLE_MASK, 0))),
                new Boolean(
                    Modifier.isProtected(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_VARIABLE_MASK, 0))),
                new Boolean(
                    Modifier.isFinal(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_VARIABLE_MASK, 0))),
                new Boolean(
                    Modifier.isPrivate(
                        this.settings.getInt(
                            ConventionKeys.COMMENT_JAVADOC_VARIABLE_MASK, 0)))
            }
        };

        public boolean isCellEditable(
            int row,
            int col)
        {
            if (col < 1)
            {
                return false;
            }
            return true;
        }


        public Class getColumnClass(int c)
        {
            return getValueAt(0, c).getClass();
        }


        public int getColumnCount()
        {
            return columnNames.length;
        }


        public String getColumnName(int col)
        {
            return columnNames[col];
        }


        public int getRowCount()
        {
            return this.data.length;
        }


        public void setValueAt(
            Object value,
            int    row,
            int    col)
        {
            if (this.data[0][col] instanceof Integer)
            {
                try
                {
                    this.data[row][col] = new Integer((String) value);
                    fireTableCellUpdated(row, col);
                }
                catch (NumberFormatException e)
                {
                    ;
                }
            }
            else
            {
                this.data[row][col] = value;
                fireTableCellUpdated(row, col);
            }
        }


        public Object getValueAt(
            int row,
            int col)
        {
            return this.data[row][col];
        }
    }


    private class ListDataHandler
        implements ListDataListener
    {
        public void contentsChanged(ListDataEvent e)
        {
        }


        public void intervalAdded(ListDataEvent ev)
        {
            DefaultListModel model = (DefaultListModel) ev.getSource();
            String name = (String) model.get(ev.getIndex0());

            if (!_tagNamePattern.matcher(name).matches())
            {
                Object[] args = { name, _tagNamePattern.pattern() };
                JOptionPane.showMessageDialog(
                    SwingUtilities.windowForComponent(JavadocSettingsPage.this),
                    MessageFormat.format(
                        JavadocSettingsPage.this.bundle.getString(
                            "MSG_INVALID_TAG_NAME" /* NOI18N */), args),
                    JavadocSettingsPage.this.bundle.getString(
                        "TLE_INVALID_TAG_NAME" /* NOI18N */), JOptionPane.ERROR_MESSAGE);
                throw new IllegalArgumentException();
            }
        }


        public void intervalRemoved(ListDataEvent e)
        {
        }
    }


    private class MethodTemplatePanel
        extends CtorTemplatePanel
    {
        JTextArea returnTextArea;

        public MethodTemplatePanel()
        {
            GridBagConstraints c = new GridBagConstraints();
            JLabel returnLabel =
                new JLabel(this.bundle.getString("LBL_RETURN" /* NOI18N */));

            GridBagLayout layout = (GridBagLayout) getLayout();
            c.insets.top = 3;
            SwingHelper.setConstraints(
                c, 0, 6, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0,
                0);
            layout.setConstraints(returnLabel, c);
            add(returnLabel);

            this.returnTextArea =
                new JTextArea(
                    StringHelper.replace(
                        this.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN),
                        DELIMETER, LINE_SEPARATOR));
            c.insets.top = 0;
            SwingHelper.setConstraints(
                c, 0, 7, GridBagConstraints.REMAINDER, 1, 1.0, 0.1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);

            JScrollPane returnScrollPane = new JScrollPane(this.returnTextArea);
            layout.setConstraints(returnScrollPane, c);
            add(returnScrollPane);
        }

        public void updateSettings()
        {
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_TOP,
                StringHelper.replace(
                    this.topTextArea.getText(), LINE_SEPARATOR, DELIMETER));
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM,
                StringHelper.replace(
                    this.parameterTextArea.getText(), LINE_SEPARATOR, DELIMETER));
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION,
                StringHelper.replace(
                    this.exceptionTextArea.getText(), LINE_SEPARATOR, DELIMETER));
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN,
                StringHelper.replace(
                    this.returnTextArea.getText(), LINE_SEPARATOR, DELIMETER));
            this.settings.put(
                ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM,
                StringHelper.replace(
                    this.bottomTextArea.getText(), LINE_SEPARATOR, DELIMETER));
        }


        public void validateSettings()
          throws ValidationException
        {
            super.validateSettings();

            String returnText = this.returnTextArea.getText();

            if (!_returnPattern.matcher(returnText).matches())
            {
                Object[] args = { _returnPattern.pattern() };
                JOptionPane.showMessageDialog(
                    SwingUtilities.windowForComponent(this),
                    MessageFormat.format(
                        this.bundle.getString("MSG_INVALID_RETURN_FRAGMENT" /* NOI18N */),
                        args),
                    this.bundle.getString("TLE_INVALID_RETURN_FRAGMENT" /* NOI18N */),
                    JOptionPane.ERROR_MESSAGE);

                throw new ValidationException();
            }
        }


        protected String getBottomTemplate()
        {
            return StringHelper.replace(
                this.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM), DELIMETER,
                LINE_SEPARATOR);
        }


        protected String getExceptionTemplate()
        {
            return StringHelper.replace(
                this.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION),
                DELIMETER, LINE_SEPARATOR);
        }


        protected String getParameterTemplate()
        {
            return StringHelper.replace(
                this.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM), DELIMETER,
                LINE_SEPARATOR);
        }


        protected String getTopTemplate()
        {
            return StringHelper.replace(
                this.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_TOP,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_TOP), DELIMETER,
                LINE_SEPARATOR);
        }
    }


    private class SimpleTemplatePanel
        extends JPanel
        implements TemplatePanel
    {
        JTextArea textArea;
        String name;

        public SimpleTemplatePanel(
            String name,
            String text)
        {
            this.name = name;
            setLayout(new BorderLayout());
            this.textArea =
                new JTextArea(StringHelper.replace(text, DELIMETER, LINE_SEPARATOR));
            add(new JScrollPane(this.textArea), BorderLayout.CENTER);
        }

        public String getText()
        {
            return this.textArea.getText();
        }


        public void updateSettings()
        {
            if (TPL_CLASS.equals(this.name))
            {
                JavadocSettingsPage.this.settings.put(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CLASS,
                    StringHelper.replace(
                        this.textArea.getText(), LINE_SEPARATOR, DELIMETER));
            }
            else if (TPL_INTERFACE.equals(name))
            {
                JavadocSettingsPage.this.settings.put(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_INTERFACE,
                    StringHelper.replace(
                        this.textArea.getText(), LINE_SEPARATOR, DELIMETER));
            }
            else if (TPL_FIELD.equals(name))
            {
                JavadocSettingsPage.this.settings.put(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_VARIABLE,
                    StringHelper.replace(
                        this.textArea.getText(), LINE_SEPARATOR, DELIMETER));
            }
        }


        public void validateSettings()
          throws ValidationException
        {
            String text = this.textArea.getText();

            if (!_templatePattern.matcher(text).matches())
            {
                Object[] args = { _templatePattern.pattern() };
                JOptionPane.showMessageDialog(
                    SwingUtilities.windowForComponent(SimpleTemplatePanel.this),
                    MessageFormat.format(
                        JavadocSettingsPage.this.bundle.getString(
                            "MSG_INVALID_TEMPLATE" /* NOI18N */), args),
                    JavadocSettingsPage.this.bundle.getString(
                        "TLE_INVALID_TEMPLATE" /* NOI18N */), JOptionPane.ERROR_MESSAGE);

                throw new ValidationException();
            }
        }
    }


    /**
     * The container that displays the different template panels.
     *
     * @since 1.0b8
     */
    private class TemplateContainer
        extends JPanel
    {
        Map panels = new HashMap(); // Map of <String>:<JPanel>
        String name;
        boolean destroyed;

        public TemplateContainer(String name)
        {
            setLayout(new BorderLayout());
            add(getTemplatePanel(name), BorderLayout.CENTER);
            this.name = name;
        }

        /**
         * Returns the name of the currently displayed panel.
         *
         * @return the name of the currently displayed panel.
         */
        public String getCurrent()
        {
            return this.name;
        }


        public void dispose()
        {
            if (!_disposed)
            {
                this.panels.clear();
                _disposed = true;
            }
        }


        /**
         * Removes the current panel from the container and add the given panel.
         *
         * @param newName name of the panel.
         */
        public void switchPanels(String newName)
        {
            remove(0);

            JPanel panel = getTemplatePanel(newName);
            add(panel, BorderLayout.CENTER);
            this.name = newName;
        }


        public void updateSettings()
        {
            for (Iterator i = this.panels.values().iterator(); i.hasNext();)
            {
                TemplatePanel panel = (TemplatePanel) i.next();
                panel.updateSettings();
            }
        }


        public void validateSettings()
          throws ValidationException
        {
            for (Iterator i = this.panels.values().iterator(); i.hasNext();)
            {
                TemplatePanel panel = (TemplatePanel) i.next();
                panel.validateSettings();
            }
        }


        /**
         * Returns the template panel for the given name.
         *
         * @param newName name of the template panel.
         *
         * @return the template panel for the given name.
         *
         * @throws IllegalArgumentException DOCUMENT ME!
         */
        private JPanel getTemplatePanel(String newName)
        {
            if (this.panels.containsKey(newName))
            {
                return (JPanel) this.panels.get(newName);
            }

            if (TPL_CLASS.equals(newName))
            {
                SimpleTemplatePanel panel =
                    new SimpleTemplatePanel(
                        newName,
                        JavadocSettingsPage.this.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CLASS,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CLASS));
                this.panels.put(newName, panel);

                return panel;
            }
            else if (TPL_INTERFACE.equals(newName))
            {
                SimpleTemplatePanel panel =
                    new SimpleTemplatePanel(
                        newName,
                        JavadocSettingsPage.this.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_INTERFACE,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_INTERFACE));
                this.panels.put(newName, panel);

                return panel;
            }
            else if (TPL_CTOR.equals(newName))
            {
                CtorTemplatePanel panel = new CtorTemplatePanel();
                this.panels.put(newName, panel);

                return panel;
            }
            else if (TPL_METHOD.equals(newName))
            {
                MethodTemplatePanel panel = new MethodTemplatePanel();
                this.panels.put(newName, panel);

                return panel;
            }
            else if (TPL_FIELD.equals(newName))
            {
                SimpleTemplatePanel panel =
                    new SimpleTemplatePanel(
                        newName,
                        JavadocSettingsPage.this.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_VARIABLE,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_VARIABLE));
                this.panels.put(newName, panel);

                return panel;
            }

            // should never happen
            throw new IllegalArgumentException("unknown template name -- " + newName);
        }
    }
}
