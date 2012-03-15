/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.ResourceBundleFactory;
import de.hunsicker.util.StringHelper;


/**
 * A simple dialog which can be used to display errors.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class ErrorDialog
    extends JDialog
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Indicates that the error message is displayed. */
    private static final int DETAILS_HIDE = 2;

    /** Indicates that the detailed error stacktrace is displayed. */
    private static final int DETAILS_SHOW = 1;

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME = "de.hunsicker.swing.Bundle" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** Holds the component with the detailed stacktrace. */
    JScrollPane _details;

    /** Is the error message or the stacktrace displayed? */
    int _status = DETAILS_HIDE;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ErrorDialog object.
     *
     * @param owner the <code>Dialog</code> from which the dialog is displayed.
     * @param title the title of the dialog.
     * @param modal if <code>true</code> the dialog should be modal.
     * @param ex error information to display.
     */
    protected ErrorDialog(
        final Dialog    owner,
        String          title,
        boolean         modal,
        final Throwable ex)
    {
        super(owner, modal);
        initialize(ex, title, owner);
    }


    /**
     * Creates a new ErrorDialog object.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed.
     * @param title the title of the dialog.
     * @param modal if <code>true</code> the dialog should be modal.
     * @param ex error information to display.
     */
    protected ErrorDialog(
        final Frame     owner,
        String          title,
        boolean         modal,
        final Throwable ex)
    {
        super(owner, modal);
        initialize(ex, title, owner);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Creates a new error dialog.
     *
     * @param owner the <code>Window</code> from which the dialog is displayed.
     * @param title the title of the dialog.
     * @param modal if <code>true</code> the dialog will be modal.
     * @param ex error information to display.
     *
     * @return a new error dialog.
     *
     * @throws IllegalArgumentException if <em>owner</em> is no instance of {@link
     *         java.awt.Frame} or {@link java.awt.Dialog}.
     */
    public static ErrorDialog create(
        Window    owner,
        String    title,
        boolean   modal,
        Throwable ex)
    {
        if (owner instanceof Frame)
        {
            return new ErrorDialog((Frame) owner, title, modal, ex);
        }
        else if (owner instanceof Dialog)
        {
            return new ErrorDialog((Dialog) owner, title, modal, ex);
        }

        throw new IllegalArgumentException("invalid owner type -- " + owner);
    }


    /**
     * Creates a new error dialog.
     *
     * @param owner the parent window of the dialog.
     * @param modal if <code>true</code> the dialog will be modal.
     * @param ex error information to display.
     *
     * @return a new error dialog.
     */
    public static ErrorDialog create(
        Window    owner,
        boolean   modal,
        Throwable ex)
    {
        return create(
            owner,
            ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                "TLE_ERROR" /* NOI18N */), modal, ex);
    }


    /**
     * Creates a new, modal error dialog.
     *
     * @param parent the parent window of the dialog.
     * @param ex error information to display.
     *
     * @return a new error dialog
     */
    public static ErrorDialog create(
        Window    parent,
        Throwable ex)
    {
        return create(
            parent,
            ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                "TLE_ERROR" /* NOI18N */), true, ex);
    }


    private void initialize(
        final Throwable ex,
        String          title,
        final Component parent)
    {
        setTitle(title);
        setResizable(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Container pane = getContentPane();
        final GridBagLayout layout = new GridBagLayout();
        pane.setLayout(layout);

        StringBuffer buf = new StringBuffer(150);

        if (ex instanceof RuntimeException)
        {
            Object[] args = { ex.getClass().getName() };
            buf.append(
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "MSG_UNEXPECTED_EXCEPTION" /* NOI18N */), args));
        }
        else
        {
            buf.append(ex.getClass().getName() + ':');
        }

        final JPanel messagePanel = new JPanel();
        GridBagLayout messageLayout = new GridBagLayout();
        messagePanel.setLayout(messageLayout);

        JLabel message = new JLabel(buf.toString());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets.right = 15;

        JLabel icon = new JLabel(UIManager.getIcon("OptionPane.errorIcon" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.RELATIVE, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, c.insets, 0, 0);
        messageLayout.setConstraints(icon, c);
        messagePanel.add(icon);
        c.insets.left = 0;
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        messageLayout.setConstraints(message, c);
        messagePanel.add(message);

        /**  */
        String[] lines =
            StringHelper.wrapStringToArray(
                (ex.getMessage() == null) ? ""
                                          : ex.getMessage(), 55, "\r\n", true,
                StringHelper.TRIM_ALL);

        if ((lines.length == 1) && (lines[0].length() == 0))
        {
            lines =
                new String[]
                {
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "MSG_NO_FURTHER_INFO" /* NOI18N */)
                };
        }

        for (int i = 0; i < lines.length; i++)
        {
            JLabel line = new JLabel(lines[i]);
            SwingHelper.setConstraints(
                c, 1, i + 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0,
                0);
            messageLayout.setConstraints(line, c);
            messagePanel.add(line);
        }

        c.insets.left = 10;
        c.insets.right = 10;
        c.insets.top = 10;
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 4, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        layout.setConstraints(messagePanel, c);
        pane.add(messagePanel);
        c.insets.top = 10;
        c.insets.left = 50;
        c.insets.bottom = 15;
        c.insets.right = 15;

        JButton okBtn =
            new JButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_OK" /* NOI18N */));
        okBtn.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    dispose();
                }
            });
        getRootPane().setDefaultButton(okBtn);
        SwingHelper.setConstraints(
            c, 3, 4, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        layout.setConstraints(okBtn, c);
        pane.add(okBtn);

        final JButton toggleBtn =
            new JButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_SHOW_DETAILS" /* NOI18N */));
        toggleBtn.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    setVisible(false);

                    switch (_status)
                    {
                        // show stacktrace, remove error message
                        case DETAILS_HIDE :
                            toggleBtn.setText(
                                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                                    "BTN_HIDE_DETAILS" /* NOI18N */));
                            _status = DETAILS_SHOW;

                            if (_details == null)
                            {
                                StringWriter stringWriter = new StringWriter();
                                PrintWriter out =
                                    new PrintWriter(new BufferedWriter(stringWriter));
                                ex.printStackTrace(out);
                                out.close();

                                JTextArea textArea =
                                    new JTextArea(stringWriter.toString(), 10, 40);
                                _details = new JScrollPane(textArea);
                                textArea.setCaretPosition(1);

                                Dimension size = new Dimension(400, 170);
                                _details.setMinimumSize(size);
                                _details.setPreferredSize(size);
                            }

                            pane.remove(messagePanel);
                            c.insets.top = 10;
                            c.insets.left = 10;
                            c.insets.bottom = 10;
                            c.insets.right = 10;
                            SwingHelper.setConstraints(
                                c, 0, 0, GridBagConstraints.REMAINDER, 4, 1.0, 1.0,
                                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                c.insets, 0, 0);
                            layout.setConstraints(_details, c);
                            pane.add(_details);

                            break;

                        // show error message, remove stacktrace
                        case DETAILS_SHOW :
                            toggleBtn.setText(
                                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                                    "BTN_SHOW_DETAILS" /* NOI18N */));
                            _status = DETAILS_HIDE;
                            pane.remove(_details);
                            c.insets.top = 10;
                            c.insets.left = 10;
                            c.insets.bottom = 10;
                            c.insets.right = 10;
                            SwingHelper.setConstraints(
                                c, 0, 0, GridBagConstraints.REMAINDER, 4, 1.0, 1.0,
                                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                c.insets, 0, 0);
                            layout.setConstraints(messagePanel, c);
                            pane.add(messagePanel);

                            break;
                    }

                    pack();
                    setLocationRelativeTo(parent);
                    setVisible(true);
                }
            });

        c.insets.top = 10;
        c.insets.left = 10;
        c.insets.bottom = 15;
        c.insets.right = 50;
        SwingHelper.setConstraints(
            c, 0, 4, GridBagConstraints.RELATIVE, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        layout.setConstraints(toggleBtn, c);
        pane.add(toggleBtn);
        pack();
        setLocationRelativeTo(parent);
    }
}
