/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.swing;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;


/**
 * Extends the standard button group to allow empty groups. Again creating a set of
 * buttons with the same <em>EmptyButtonGroup</em> object means that turning 'on' one of
 * those buttons turns 'off' all other buttons in the group. The difference between
 * <em>EmptyButtonGroup</em> and <em>ButtonGroup</em> lies in the fact that the
 * currently selected button can be deselected which results - in an empty group.
 * 
 * <p>
 * Note that the original documentation for <em>ButtonGroup</em> (as of JDK 1.3) is wrong
 * in that the initial state of the group does depend on the state of the added buttons
 * (They claim 'Initially, all buttons in the group are unselected').
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public class EmptyButtonGroup
    extends ButtonGroup
{
    //~ Instance variables ---------------------------------------------------------------

    /**
     * The currently selected button. May be <code>null</code> if no button is selected.
     */
    protected ButtonModel selection;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new EmptyButtonGroup object.
     */
    public EmptyButtonGroup()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the selected state of the given button model.
     *
     * @param model model that has its state changed.
     * @param select if <code>true</code> selects the given model.
     */
    public void setSelected(
        ButtonModel model,
        boolean     select)
    {
        if (select)
        {
            ButtonModel oldSelection = this.selection;

            // select the button
            this.selection = model;

            // and change the old selection if any
            if (oldSelection != null)
            {
                oldSelection.setSelected(false);
            }
        }
        else
        {
            // unselect the button if find a selected one
            // (now the group is empty again)
            if (this.selection == model)
            {
                this.selection = null;
            }
        }
    }


    /**
     * Indicates whether the given button model is selected.
     *
     * @param model model to check.
     *
     * @return <code>true</code> if the given model is selected.
     */
    public boolean isSelected(ButtonModel model)
    {
        return (model == this.selection);
    }


    /**
     * Returns the selected button model.
     *
     * @return the selected button model.
     */
    public ButtonModel getSelection()
    {
        return this.selection;
    }


    /**
     * Adds the given button to the group. If the button is selected and the group not
     * already contains aselected button, the button will retain its selection.
     *
     * @param button button to add.
     */
    public void add(AbstractButton button)
    {
        if (button == null)
        {
            return;
        }

        this.buttons.add(button);

        if ((this.selection == null) && button.isSelected())
        {
            this.selection = button.getModel();
        }

        button.getModel().setGroup(this);
    }
}
