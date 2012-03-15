/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.swing.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SwingUtilities;


/**
 * UI related helper functions.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public final class SwingHelper
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new SwingHelper object.
     */
    private SwingHelper()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the constraints for the given constraints object. Helper function to be able
     * to reuse a constraints object.
     *
     * @param constraints the constraints object to initialize.
     * @param gridx the initial gridx value.
     * @param gridy the initial gridy value.
     * @param gridwidth the initial gridwidth value.
     * @param gridheight the initial gridheight value.
     * @param weightx the initial weightx value.
     * @param weighty the initial weighty value.
     * @param anchor the initial anchor value.
     * @param fill the initial fill value.
     * @param insets the initial insets value.
     * @param ipadx the initial ipadx value.
     * @param ipady the initial ipady value.
     *
     * @return the initialized constraints object.
     */
    public static GridBagConstraints setConstraints(
        GridBagConstraints constraints,
        int                gridx,
        int                gridy,
        int                gridwidth,
        int                gridheight,
        double             weightx,
        double             weighty,
        int                anchor,
        int                fill,
        Insets             insets,
        int                ipadx,
        int                ipady)
    {
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.insets.top = insets.top;
        constraints.insets.bottom = insets.bottom;
        constraints.insets.left = insets.left;
        constraints.insets.right = insets.right;
        constraints.ipadx = ipadx;
        constraints.ipady = ipady;

        return constraints;
    }


    /**
     * DOCUMENT ME!
     *
     * @param item DOCUMENT ME!
     * @param text DOCUMENT ME!
     * @param useMnemonic DOCUMENT ME!
     */
    public static void setMenuText(
        AbstractButton item,
        String         text,
        boolean        useMnemonic)
    {
        int i = text.indexOf('&');

        if (i < 0)
        {
            item.setText(text);
        }
        else
        {
            item.setText(text.substring(0, i) + text.substring(i + 1));

            if (useMnemonic)
            {
                item.setMnemonic(text.charAt(i + 1));
            }
        }
    }


    /**
     * Returns the frame that contains the given component.
     *
     * @param component component.
     *
     * @return frame that contains the given component or <code>null</code> if there is
     *         no parent frame.
     */
    public static Frame getOwnerFrame(Component component)
    {
        Window window = SwingUtilities.windowForComponent(component);
        Frame mother = null;

        if (window != null)
        {
            Window owner = window.getOwner();

            if ((owner != null) && owner instanceof Frame)
            {
                mother = (Frame) owner;
            }
        }

        return mother;
    }


    /**
     * Returns the visual height of the given table.
     *
     * @param table the table.
     *
     * @return the table height.
     */
    public static int getTableHeight(JTable table)
    {
        int result = 0;
        int rowHeight = 0;

        for (int i = 0, rows = table.getRowCount(); i < rows; i++)
        {
            int height = table.getRowHeight(i);
            result += height;

            if (height > rowHeight)
            {
                rowHeight = height;
            }
        }

        return result + rowHeight + (table.getRowCount() * table.getRowMargin());
    }


    /**
     * Creates a new button with the given text.
     *
     * @param text DOCUMENT ME!
     * @param parse DOCUMENT ME!
     *
     * @return new button with the given text.
     *
     * @since 1.0b9
     */
    public static JButton createButton(
        String  text,
        boolean parse)
    {
        JButton button = new JButton();

        if (parse)
        {
            setMenuText(button, text, true);
        }
        else
        {
            button.setText(text);
        }

        return button;
    }


    /**
     * Creates a new button with the given text.
     *
     * @param text DOCUMENT ME!
     *
     * @return new button with the given text.
     *
     * @since 1.0b9
     */
    public static JButton createButton(String text)
    {
        return createButton(text, true);
    }


    /**
     * Displays the given file chooser. Utility method for avoiding of memory leak in JDK
     * 1.3 {@link javax.swing.JFileChooser#showDialog}.
     *
     * @param chooser the file chooser to display.
     * @param parent the parent window.
     * @param approveButtonText the text for the approve button.
     *
     * @return the return code of the chooser.
     */
    public static final int showJFileChooser(
        JFileChooser chooser,
        Component    parent,
        String       approveButtonText)
    {
        if (approveButtonText != null)
        {
            chooser.setApproveButtonText(approveButtonText);
            chooser.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        }

        Frame frame =
            (parent instanceof Frame) ? (Frame) parent
                                      : (Frame) javax.swing.SwingUtilities
            .getAncestorOfClass(java.awt.Frame.class, parent);
        String title = chooser.getDialogTitle();

        if (title == null)
        {
            title = chooser.getUI().getDialogTitle(chooser);
        }

        final JDialog dialog = new JDialog(frame, title, true);
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooser, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        chooser.rescanCurrentDirectory();

        final int[] retValue = new int[] { javax.swing.JFileChooser.CANCEL_OPTION };

        ActionListener l =
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    if (ev.getActionCommand() == JFileChooser.APPROVE_SELECTION)
                    {
                        retValue[0] = JFileChooser.APPROVE_OPTION;
                    }

                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };

        chooser.addActionListener(l);
        dialog.show();

        return (retValue[0]);
    }
}
