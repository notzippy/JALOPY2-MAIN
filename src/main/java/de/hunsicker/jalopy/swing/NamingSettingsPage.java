/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.ResourceBundleFactory;

/**
 * Settings page for the Jalopy Code Inspector naming settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class NamingSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    static final String EMPTY_STRING = "" /* NOI18N */.intern();

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME =
        "de.hunsicker.jalopy.swing.Bundle" /* NOI18N */;

    JList _patternList;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new NamingSettingsPage object.
     */
    public NamingSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new NamingSettingsPage.
     *
     * @param container the parent container.
     */
    NamingSettingsPage(SettingsContainer container)
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
        ListModel model = _patternList.getModel();

        for (int i = 0, size = model.getSize(); i < size; i++)
        {
            PatternListEntry entry = (PatternListEntry) model.getElementAt(i);
            this.settings.put(entry.key, entry.pattern);
        }
    }


    RegexpDialog create(Window owner)
    {
        if (owner instanceof Frame)
        {
            return new RegexpDialog((Frame) owner);
        }

        return new RegexpDialog((Dialog) owner);
    }


    private void initialize()
    {
        PatternListEntry[] entries =
        {
            new PatternListEntry(
                this.bundle.getString("LBL_PACKAGES" /* NOI18N */), null,
                ConventionKeys.REGEXP_PACKAGE, ConventionDefaults.REGEXP_PACKAGE),
            new PatternListEntry(
                this.bundle.getString("LBL_CLASSES" /* NOI18N */), null,
                ConventionKeys.REGEXP_CLASS, ConventionDefaults.REGEXP_CLASS),
            new PatternListEntry(
                this.bundle.getString("LBL_CLASSES" /* NOI18N */), "abstract" /* NOI18N */,
                ConventionKeys.REGEXP_CLASS_ABSTRACT,
                ConventionDefaults.REGEXP_CLASS_ABSTRACT),
            new PatternListEntry(
                this.bundle.getString("LBL_INTERFACES" /* NOI18N */), null,
                ConventionKeys.REGEXP_INTERFACE, ConventionDefaults.REGEXP_INTERFACE),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */), "public" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_PUBLIC, ConventionDefaults.REGEXP_FIELD),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */), "protected" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_PROTECTED, ConventionDefaults.REGEXP_FIELD),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */), "default" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_DEFAULT, ConventionDefaults.REGEXP_FIELD),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */), "private" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_PRIVATE, ConventionDefaults.REGEXP_FIELD),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */),
                "public static" /* NOI18N */, ConventionKeys.REGEXP_FIELD_PUBLIC_STATIC,
                ConventionDefaults.REGEXP_FIELD),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */),
                "protected static" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_PROTECTED_STATIC,
                ConventionDefaults.REGEXP_FIELD),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */),
                "default static" /* NOI18N */, ConventionKeys.REGEXP_FIELD_DEFAULT_STATIC,
                ConventionDefaults.REGEXP_FIELD),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */),
                "private static" /* NOI18N */, ConventionKeys.REGEXP_FIELD_PRIVATE_STATIC,
                ConventionDefaults.REGEXP_FIELD),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */),
                "public static final" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_PUBLIC_STATIC_FINAL,
                ConventionDefaults.REGEXP_FIELD_STATIC_FINAL),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */),
                "protected static final" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_PROTECTED_STATIC_FINAL,
                ConventionDefaults.REGEXP_FIELD_STATIC_FINAL),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */),
                "default static final" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_DEFAULT_STATIC_FINAL,
                ConventionDefaults.REGEXP_FIELD_STATIC_FINAL),
            new PatternListEntry(
                this.bundle.getString("LBL_FIELDS" /* NOI18N */),
                "private static final" /* NOI18N */,
                ConventionKeys.REGEXP_FIELD_PRIVATE_STATIC_FINAL,
                ConventionDefaults.REGEXP_FIELD_STATIC_FINAL),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */), "public" /* NOI18N */,
                ConventionKeys.REGEXP_METHOD_PUBLIC, ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "protected" /* NOI18N */, ConventionKeys.REGEXP_METHOD_PROTECTED,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */), "default" /* NOI18N */,
                ConventionKeys.REGEXP_METHOD_DEFAULT, ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */), "private" /* NOI18N */,
                ConventionKeys.REGEXP_METHOD_PRIVATE, ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "public static" /* NOI18N */, ConventionKeys.REGEXP_METHOD_PUBLIC_STATIC,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "protected static" /* NOI18N */,
                ConventionKeys.REGEXP_METHOD_PROTECTED_STATIC,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "default static" /* NOI18N */, ConventionKeys.REGEXP_METHOD_DEFAULT_STATIC,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "private static" /* NOI18N */, ConventionKeys.REGEXP_METHOD_PRIVATE_STATIC,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "public static final" /* NOI18N */,
                ConventionKeys.REGEXP_METHOD_PUBLIC_STATIC_FINAL,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "protected static final" /* NOI18N */,
                ConventionKeys.REGEXP_METHOD_PROTECTED_STATIC_FINAL,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "default static final" /* NOI18N */,
                ConventionKeys.REGEXP_METHOD_DEFAULT_STATIC_FINAL,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_METHODS" /* NOI18N */),
                "private static final" /* NOI18N */,
                ConventionKeys.REGEXP_METHOD_PRIVATE_STATIC_FINAL,
                ConventionDefaults.REGEXP_METHOD),
            new PatternListEntry(
                this.bundle.getString("LBL_PARAMETER" /* NOI18N */), null,
                ConventionKeys.REGEXP_PARAM, ConventionDefaults.REGEXP_PARAM),
            new PatternListEntry(
                this.bundle.getString("LBL_PARAMETER" /* NOI18N */), "final" /* NOI18N */,
                ConventionKeys.REGEXP_PARAM_FINAL, ConventionDefaults.REGEXP_PARAM),
            new PatternListEntry(
                this.bundle.getString("LBL_LOCAL_VARIABLE" /* NOI18N */), null,
                ConventionKeys.REGEXP_LOCAL_VARIABLE,
                ConventionDefaults.REGEXP_LOCAL_VARIABLE),
            new PatternListEntry(
                this.bundle.getString("LBL_LABELS" /* NOI18N */), null,
                ConventionKeys.REGEXP_LABEL, ConventionDefaults.REGEXP_LABEL)
        };

        _patternList = new JList(entries);
        _patternList.setFont(new Font("Courier" /* NOI18N */, Font.PLAIN, 11));

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();

        JScrollPane scrollPane = new JScrollPane(_patternList);
        c.insets.bottom = 10;
        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        layout.setConstraints(scrollPane, c);
        add(scrollPane);

        final JButton changeButton =
            SwingHelper.createButton(this.bundle.getString("BTN_CHANGE" /* NOI18N */));
        changeButton.setEnabled(false);
        changeButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    Window owner =
                        SwingUtilities.windowForComponent(NamingSettingsPage.this);
                    RegexpDialog dialog = create(owner);
                    dialog.setVisible(true);
                    dialog.dispose();
                }
            });

        c.insets.right = 20;
        c.insets.top = 0;
        c.insets.bottom = 20;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        layout.setConstraints(changeButton, c);
        add(changeButton);

        _patternList.addMouseListener(
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent ev)
                {
                    if (ev.getClickCount() > 1)
                    {
                        Window owner =
                            SwingUtilities.windowForComponent(NamingSettingsPage.this);
                        RegexpDialog dialog = create(owner);
                        dialog.setVisible(true);
                        dialog.dispose();
                    }
                }
            });

        _patternList.addListSelectionListener(
            new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent ev)
                {
                    // ignore extra messages
                    if (ev.getValueIsAdjusting())
                    {
                        return;
                    }

                    changeButton.setEnabled(!_patternList.isSelectionEmpty());
                }
            });
    }

    //~ Inner Classes --------------------------------------------------------------------

    private final class PatternListEntry
    {
        Convention.Key key;
        String modifiers;
        String name;
        String pattern;

        public PatternListEntry(
            String         name,
            String         modifiers,
            Convention.Key key,
            String         pattern)
        {
            this.name = name;
            this.modifiers = modifiers;
            this.key = key;
            this.pattern = NamingSettingsPage.this.settings.get(key, pattern);
        }

        public String toString()
        {
            StringBuffer buf = new StringBuffer(120);

            buf.append(this.name);
            buf.append(' ');

            if (this.modifiers != null)
            {
                buf.append('<');
                buf.append(this.modifiers);
                buf.append('>');
                buf.append(' ');
            }

            if (buf.length() < 36)
            {
                for (int i = 0, size = 36 - buf.length(); i < size; i++)
                {
                    buf.append(' ');
                }
            }

            buf.append(' ');
            buf.append(this.pattern);

            return buf.toString();
        }
    }


    private final class RegexpDialog
        extends JDialog
    {
        JLabel messageLabel;

        RegexpDialog(Frame owner)
        {
            super(owner);
            initialize();
        }


        RegexpDialog(Dialog owner)
        {
            super(owner);
            initialize();
        }

        void setPattern(String pattern)
        {
            PatternListEntry entry = (PatternListEntry) _patternList.getSelectedValue();
            entry.pattern = pattern;

            _patternList.setSelectedValue(entry, false);
        }


        private String getPattern()
        {
            PatternListEntry entry = (PatternListEntry) _patternList.getSelectedValue();

            return entry.pattern;
        }


        private void initialize()
        {
            setTitle(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "TLE_REGEXP_TESTER" /* NOI18N */));
            setModal(true);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            final Container pane = getContentPane();
            final GridBagLayout layout = new GridBagLayout();
            pane.setLayout(layout);

            GridBagConstraints c = new GridBagConstraints();

            JLabel patternLabel =
                new JLabel(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_PATTERN" /* NOI18N */));
            c.insets.top = 10;
            c.insets.left = 15;
            c.insets.right = 10;
            SwingHelper.setConstraints(
                c, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                c.insets, 0, 0);
            layout.setConstraints(patternLabel, c);
            pane.add(patternLabel);
            c.insets.left = 0;
            c.insets.right = 15;

            final JTextField patternTextField = new JTextField(getPattern(), 25);
            SwingHelper.setConstraints(
                c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0,
                0);
            layout.setConstraints(patternTextField, c);
            pane.add(patternTextField);

            JLabel testLabel =
                new JLabel(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_STRING" /* NOI18N */));
            c.insets.top = 0;
            c.insets.bottom = 10;
            c.insets.left = 15;
            c.insets.right = 10;
            SwingHelper.setConstraints(
                c, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                c.insets, 0, 0);
            layout.setConstraints(testLabel, c);
            pane.add(testLabel);
            c.insets.left = 0;
            c.insets.right = 15;

            final JTextField testTextField = new JTextField(EMPTY_STRING, 25);
            SwingHelper.setConstraints(
                c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0,
                0);
            layout.setConstraints(testTextField, c);
            pane.add(testTextField);

            messageLabel = new JLabel(" " /* NOI18N */);
            messageLabel.setFont(new Font("Courier" /* NOI18N */, Font.BOLD, 14));
            c.insets.top = 15;
            c.insets.bottom = 15;
            c.insets.left = 20;
            c.insets.right = 20;
            SwingHelper.setConstraints(
                c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
            layout.setConstraints(messageLabel, c);
            pane.add(messageLabel);

            final JButton testButton =
                SwingHelper.createButton(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "BTN_TEST" /* NOI18N */));
            c.insets.top = 0;
            c.insets.bottom = 10;
            c.insets.left = 15;
            c.insets.right = 50;
            SwingHelper.setConstraints(
                c, 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                c.insets, 0, 0);
            layout.setConstraints(testButton, c);
            pane.add(testButton);
            testButton.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent ev)
                    {
                        if (!test(patternTextField.getText(), testTextField.getText()))
                        {
                            testTextField.requestFocus();
                        }
                    }
                });

            JButton applyButton =
                SwingHelper.createButton(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "BTN_APPLY" /* NOI18N */));
            c.insets.left = 10;
            c.insets.right = 5;
            SwingHelper.setConstraints(
                c, 4, 3, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                c.insets, 0, 0);
            layout.setConstraints(applyButton, c);
            pane.add(applyButton);

            applyButton.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent ev)
                    {
                        if (test(patternTextField.getText(), testTextField.getText()))
                        {
                            setPattern(patternTextField.getText());
                            dispose();
                        }
                    }
                });

            JButton cancelButton =
                SwingHelper.createButton(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "BTN_CANCEL" /* NOI18N */));
            c.insets.left = 0;
            c.insets.right = 15;
            SwingHelper.setConstraints(
                c, 5, 3, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, c.insets, 0, 0);
            layout.setConstraints(cancelButton, c);
            pane.add(cancelButton);

            cancelButton.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent ev)
                    {
                        dispose();
                    }
                });

            pack();
            setLocationRelativeTo(getParent());
        }


        /**
         * Performs regular expression testing for the given pattern/string.
         *
         * @param pattern DOCUMENT ME!
         * @param string DOCUMENT ME!
         *
         * @return <code>true</code> if the given pattern matches the given string.
         */
        boolean test(
            String pattern,
            String string)
        {
            Pattern regexp = null;

            try
            {
                regexp = Pattern.compile(pattern);
            }
            catch (PatternSyntaxException ex)
            {
                this.messageLabel.setForeground(Color.red);
                this.messageLabel.setText(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_INVALID_PATTERN" /* NOI18N */));

                return false;
            }

            Matcher matcher = regexp.matcher(string);

            if (matcher.matches())
            {
                this.messageLabel.setForeground(Color.blue);
                this.messageLabel.setText(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_PATTERN_MATCHES" /* NOI18N */));

                return true;
            }
            this.messageLabel.setForeground(Color.red);
            this.messageLabel.setText(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "LBL_PATTERN_DOES_NOT_MATCH" /* NOI18N */));

            return false;
        }
    }
}
