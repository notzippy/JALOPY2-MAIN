/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy Code Inspector general settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class CodeInspectorSettingsPage
    extends AbstractSettingsPage
{
    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _enableCheckBox;
    private JCheckBox _tip15CheckBox;
    private JCheckBox _tipAdhereNamingConventionCheckBox;
    private JCheckBox _tipAvoidThreadGroupsCheckBox;
    private JCheckBox _tipDeclareListCommentCheckBox;
    private JCheckBox _tipDontIgnoreExceptionCheckBox;
    private JCheckBox _tipDontSubstituteCheckBox;
    private JCheckBox _tipEmptyFinallyCheckBox;
    private JCheckBox _tipInterfaceTypeCheckBox;
    private JCheckBox _tipNeverExceptionCheckBox;
    private JCheckBox _tipNeverThrowableCheckBox;
    private JCheckBox _tipObeyEqualsCheckBox;
    private JCheckBox _tipOverrideHashCodeCheckBox;
    private JCheckBox _tipOverrideStringCheckBox;
    private JCheckBox _tipReferByInterfaceCheckBox;
    private JCheckBox _tipReplaceStructCheckBox;
    private JCheckBox _tipStringLiteralI18nCheckBox;
    private JCheckBox _tipWaitOutsideLoopCheckBox;
    private JCheckBox _tipWrongListCommentCheckBox;
    private JCheckBox _tipZeroArrayCheckBox;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new CodeInspectorSettingsPage object.
     */
    public CodeInspectorSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new CodeInspectorSettingsPage.
     *
     * @param container the parent container.
     */
    CodeInspectorSettingsPage(SettingsContainer container)
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
        this.settings.putBoolean(ConventionKeys.INSPECTOR, _enableCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_STRING_LITERAL_I18N,
            _tipStringLiteralI18nCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_NEVER_THROW_EXCEPTION,
            _tipNeverExceptionCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_NEVER_THROW_THROWABLE,
            _tipNeverThrowableCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_DONT_IGNORE_EXCEPTION,
            _tipDontIgnoreExceptionCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_NEVER_WAIT_OUTSIDE_LOOP,
            _tipWaitOutsideLoopCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_AVOID_THREAD_GROUPS,
            _tipAvoidThreadGroupsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_DECLARE_COLLECTION_COMMENT,
            _tipDeclareListCommentCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_WRONG_COLLECTION_COMMENT,
            _tipWrongListCommentCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_EMPTY_FINALLY, _tipEmptyFinallyCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_DONT_SUBSTITUTE_OBJECT_EQUALS,
            _tipDontSubstituteCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_OBEY_CONTRACT_EQUALS, _tipObeyEqualsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_OVERRIDE_HASHCODE,
            _tipOverrideHashCodeCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_OVERRIDE_TO_STRING, _tipOverrideStringCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_INTERFACE_ONLY_FOR_TYPE,
            _tipInterfaceTypeCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_REPLACE_STRUCTURE_WITH_CLASS,
            _tipReplaceStructCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_RETURN_ZERO_ARRAY, _tipZeroArrayCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_REFER_BY_INTERFACE,
            _tipReferByInterfaceCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.TIP_ADHERE_TO_NAMING_CONVENTION,
            _tipAdhereNamingConventionCheckBox.isSelected());
    }


    private JPanel createGeneralPane()
    {
        JPanel generalPanel = new JPanel();

        generalPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        generalPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_GENERAL" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _enableCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_ENABLE_INSPECTOR" /* NOI18N */),
                this.settings.getBoolean(ConventionKeys.INSPECTOR, false));
        generalPanel.add(_enableCheckBox);

        return generalPanel;
    }


    private JPanel createTipsPane()
    {
        JPanel tipsPanel = new JPanel();
        GridLayout tipsPanelLayout = new GridLayout(3, 10);
        tipsPanel.setLayout(tipsPanelLayout);
        tipsPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_TIPS" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _tipObeyEqualsCheckBox =
            new JCheckBox(
                "1" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_OBEY_CONTRACT_EQUALS, false));
        _tipObeyEqualsCheckBox.setToolTipText(
            this.bundle.getString("TIP_OBEY_CONTRACT_EQUALS" /* NOI18N */));
        tipsPanel.add(_tipObeyEqualsCheckBox);

        _tipDontSubstituteCheckBox =
            new JCheckBox(
                "2" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.TIP_DONT_SUBSTITUTE_OBJECT_EQUALS, false));
        _tipDontSubstituteCheckBox.setToolTipText(
            this.bundle.getString("TIP_DONT_SUBSTITUTE_OBJECT_EQUALS" /* NOI18N */));
        tipsPanel.add(_tipDontSubstituteCheckBox);

        _tipOverrideHashCodeCheckBox =
            new JCheckBox(
                "3" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_OVERRIDE_HASHCODE, false));
        _tipOverrideHashCodeCheckBox.setToolTipText(
            this.bundle.getString("TIP_OVERRIDE_HASHCODE" /* NOI18N */));
        tipsPanel.add(_tipOverrideHashCodeCheckBox);

        _tipOverrideStringCheckBox =
            new JCheckBox(
                "4" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_OVERRIDE_TO_STRING, false));
        _tipOverrideStringCheckBox.setToolTipText(
            this.bundle.getString("TIP_OVERRIDE_TO_STRING" /* NOI18N */));
        tipsPanel.add(_tipOverrideStringCheckBox);

        _tipInterfaceTypeCheckBox =
            new JCheckBox(
                "5" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.TIP_INTERFACE_ONLY_FOR_TYPE, false));
        _tipInterfaceTypeCheckBox.setToolTipText(
            this.bundle.getString("TIP_INTERFACE_ONLY_FOR_TYPE" /* NOI18N */));
        tipsPanel.add(_tipInterfaceTypeCheckBox);

        _tipReplaceStructCheckBox =
            new JCheckBox(
                "6" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.TIP_REPLACE_STRUCTURE_WITH_CLASS, false));
        _tipReplaceStructCheckBox.setToolTipText(
            this.bundle.getString("TIP_REPLACE_STRUCTURE_WITH_CLASS" /* NOI18N */));
        tipsPanel.add(_tipReplaceStructCheckBox);

        _tipZeroArrayCheckBox =
            new JCheckBox(
                "7" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_RETURN_ZERO_ARRAY, false));
        _tipZeroArrayCheckBox.setToolTipText(
            this.bundle.getString("TIP_RETURN_ZERO_ARRAY" /* NOI18N */));
        tipsPanel.add(_tipZeroArrayCheckBox);

        _tipAdhereNamingConventionCheckBox =
            new JCheckBox(
                "8" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.TIP_ADHERE_TO_NAMING_CONVENTION, false));
        _tipAdhereNamingConventionCheckBox.setToolTipText(
            this.bundle.getString("TIP_ADHERE_TO_NAMING_CONVENTION" /* NOI18N */));
        tipsPanel.add(_tipAdhereNamingConventionCheckBox);

        _tipReferByInterfaceCheckBox =
            new JCheckBox(
                "9" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_REFER_BY_INTERFACE, false));
        _tipReferByInterfaceCheckBox.setToolTipText(
            this.bundle.getString("TIP_REFER_BY_INTERFACE" /* NOI18N */));
        tipsPanel.add(_tipReferByInterfaceCheckBox);

        _tipNeverExceptionCheckBox =
            new JCheckBox(
                "10" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_NEVER_THROW_EXCEPTION, false));
        _tipNeverExceptionCheckBox.setToolTipText(
            this.bundle.getString("TIP_NEVER_THROW_EXCEPTION" /* NOI18N */));
        tipsPanel.add(_tipNeverExceptionCheckBox);

        _tipNeverThrowableCheckBox =
            new JCheckBox(
                "11" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_NEVER_THROW_THROWABLE, false));
        _tipNeverThrowableCheckBox.setToolTipText(
            this.bundle.getString("TIP_NEVER_THROW_THROWABLE" /* NOI18N */));
        tipsPanel.add(_tipNeverThrowableCheckBox);

        _tipDontIgnoreExceptionCheckBox =
            new JCheckBox(
                "12" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_DONT_IGNORE_EXCEPTION, false));
        _tipDontIgnoreExceptionCheckBox.setToolTipText(
            this.bundle.getString("TIP_DONT_IGNORE_EXCEPTION" /* NOI18N */));
        tipsPanel.add(_tipDontIgnoreExceptionCheckBox);

        _tipWaitOutsideLoopCheckBox =
            new JCheckBox(
                "13" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.TIP_NEVER_WAIT_OUTSIDE_LOOP, false));
        _tipWaitOutsideLoopCheckBox.setToolTipText(
            this.bundle.getString("TIP_NEVER_WAIT_OUTSIDE_LOOP" /* NOI18N */));
        tipsPanel.add(_tipWaitOutsideLoopCheckBox);

        _tipAvoidThreadGroupsCheckBox =
            new JCheckBox(
                "14" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_AVOID_THREAD_GROUPS, false));
        _tipAvoidThreadGroupsCheckBox.setToolTipText(
            this.bundle.getString("TIP_AVOID_THREAD_GROUPS" /* NOI18N */));
        tipsPanel.add(_tipAvoidThreadGroupsCheckBox);

        _tipDeclareListCommentCheckBox =
            new JCheckBox(
                "15" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.TIP_DECLARE_COLLECTION_COMMENT, false));
        _tipDeclareListCommentCheckBox.setToolTipText(
            this.bundle.getString("TIP_DECLARE_COLLECTION_COMMENT" /* NOI18N */));
        tipsPanel.add(_tipDeclareListCommentCheckBox);

        _tipWrongListCommentCheckBox =
            new JCheckBox(
                "16" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.TIP_WRONG_COLLECTION_COMMENT, false));
        _tipWrongListCommentCheckBox.setToolTipText(
            this.bundle.getString("TIP_WRONG_COLLECTION_COMMENT" /* NOI18N */));
        tipsPanel.add(_tipWrongListCommentCheckBox);

        _tipEmptyFinallyCheckBox =
            new JCheckBox(
                "17" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_EMPTY_FINALLY, false));
        _tipEmptyFinallyCheckBox.setToolTipText(
            this.bundle.getString("TIP_EMPTY_FINALLY" /* NOI18N */));
        tipsPanel.add(_tipEmptyFinallyCheckBox);

        _tip15CheckBox = new JCheckBox("18" /* NOI18N */, false);
        _tip15CheckBox.setToolTipText(
            this.bundle.getString("TIP_AVOID_VARIABLE_SHADOWING" /* NOI18N */));
        tipsPanel.add(_tip15CheckBox);

        _tipStringLiteralI18nCheckBox =
            new JCheckBox(
                "19" /* NOI18N */,
                this.settings.getBoolean(ConventionKeys.TIP_STRING_LITERAL_I18N, false));
        _tipStringLiteralI18nCheckBox.setToolTipText(
            this.bundle.getString("TIP_STRING_LITERAL_I18N" /* NOI18N */));
        tipsPanel.add(_tipStringLiteralI18nCheckBox);

        return tipsPanel;
    }


    private void initialize()
    {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();

        JPanel generalPanel = createGeneralPane();
        c.insets.bottom = 10;
        c.insets.top = 10;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(generalPanel, c);
        add(generalPanel);

        JPanel tipsPanel = createTipsPane();
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(tipsPanel, c);
        add(tipsPanel);
    }
}
