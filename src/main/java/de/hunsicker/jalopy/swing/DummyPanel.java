/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

/**
 * An empty panel that can act as a placeholder.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 */
final class DummyPanel
    extends AbstractSettingsPage
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new DummyPanel object.
     */
    public DummyPanel()
    {
    }


    /**
     * Creates a new DummyPanel object.
     *
     * @param container the parent container.
     */
    public DummyPanel(SettingsContainer container)
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
    }
}
