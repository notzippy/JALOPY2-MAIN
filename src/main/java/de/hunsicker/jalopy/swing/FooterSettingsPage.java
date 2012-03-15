/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Settings page for the Jalopy printer footer settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class FooterSettingsPage
    extends HeaderSettingsPage
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new FooterSettingsPage object.
     */
    public FooterSettingsPage()
    {
        super();
    }


    /**
     * Creates a new FooterSettingsPage.
     *
     * @param container the parent container.
     */
    FooterSettingsPage(SettingsContainer container)
    {
        super(container);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    protected Convention.Key getBlankLinesAfterKey()
    {
        return ConventionKeys.BLANK_LINES_AFTER_FOOTER;
    }


    /**
     * {@inheritDoc}
     */
    protected Convention.Key getBlankLinesBeforeKey()
    {
        return ConventionKeys.BLANK_LINES_BEFORE_FOOTER;
    }


    /**
     * {@inheritDoc}
     */
    protected Convention.Key getConventionKeysKey()
    {
        return ConventionKeys.FOOTER_KEYS;
    }


    /**
     * {@inheritDoc}
     */
    protected String getDefaultAfter()
    {
        return String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_FOOTER);
    }


    /**
     * {@inheritDoc}
     */
    protected String getDeleteLabel()
    {
        return this.bundle.getString("BDR_DELETE_FOOTERS" /* NOI18N */);
    }


    /**
     * {@inheritDoc}
     */
    protected String[] getItemsAfter()
    {
        return createItemList(new int[] { 1, 2, 3, 4, 5 });
    }


    /**
     * {@inheritDoc}
     */
    protected Convention.Key getSmartModeKey()
    {
        return ConventionKeys.FOOTER_SMART_MODE_LINES;
    }


    /**
     * {@inheritDoc}
     */
    protected Convention.Key getTextKey()
    {
        return ConventionKeys.FOOTER_TEXT;
    }


    /**
     * {@inheritDoc}
     */
    protected Convention.Key getUseKey()
    {
        return ConventionKeys.FOOTER;
    }

    
    /**
     * {@inheritDoc}
     */
    protected Convention.Key getIgnoreIfExistsKey()
    {
        return ConventionKeys.FOOTER_IGNORE_IF_EXISTS;
    }


    /**
     * {@inheritDoc}
     */
    protected String getUseLabel()
    {
        return this.bundle.getString("CHK_USE_FOOTER" /* NOI18N */);
    }

    
    /**
     * {@inheritDoc}
     */
    protected String getIgnoreIfExistsLabel()
    {
        return this.bundle.getString("CHK_IGNORE_FOOTER_IF_EXISTS" /* NOI18N */);
    }
}
