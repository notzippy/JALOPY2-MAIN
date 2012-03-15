/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.Timer;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.util.ResourceBundleFactory;


/**
 * Skeleton implementation of a settings page. A settings page provides the graphical
 * means to display/edit a given subset of the Jalopy code convention settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public abstract class AbstractSettingsPage
    extends JPanel
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The Deliminator */
    protected static final String DELIMETER = "|" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /**
     * ResourceBundle that provides the localized string resources for the graphical
     * components.
     */
    protected final ResourceBundle bundle;

    /** The code convention to display/edit. */
    protected Convention settings;

    /** Listener used to trigger an update of the preview frame. */
    final ActionListener trigger = new UpdateTrigger();

    /**
     * Our container. May be <code>null</code> if the code convention pages are directly
     * embedded into a Java appplication.
     */
    SettingsContainer _container;

    /** The category of the page. */
    private String _category;

    /** The title to display in title bar. */
    private String _title;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new AbstractSettingsPage.
     */
    public AbstractSettingsPage()
    {
        this.settings = Convention.getInstance();
        this.bundle =
            ResourceBundleFactory.getBundle(
                "de.hunsicker.jalopy.swing.Bundle" /* NOI18N */);
    }


    /**
     * Creates a new AbstractSettingsPage.
     *
     * @param container the parent container.
     */
    AbstractSettingsPage(SettingsContainer container)
    {
        this();
        _container = container;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the name of this page's category.
     *
     * @param category name of the category.
     */
    public void setCategory(String category)
    {
        _category = category;
    }


    /**
     * Returns the category.
     *
     * @return the category.
     */
    public String getCategory()
    {
        return _category;
    }


    /**
     * Sets the current code convention.
     *
     * @param convention code convention.
     */
    public void setConvention(Convention convention)
    {
        this.settings = convention;
    }


    /**
     * Returns the current code convention.
     *
     * @return the current code convention.
     */
    public Convention getConvention()
    {
        return this.settings;
    }


    /**
     * Sets the title of this page.
     *
     * @param title the title.
     */
    public void setTitle(String title)
    {
        _title = title;
    }


    /**
     * Returns the title of this page.
     *
     * @return the title of this page.
     */
    public String getTitle()
    {
        return _title;
    }


    /**
     * Updates the current code convention to reflect the current state of this page.
     *
     * @see #getConvention
     */
    public abstract void updateSettings();


    /**
     * Validates this page's settings. Pages that need their input validated should
     * override to provide the needed implementation.
     * 
     * <p>
     * In case of any violation the implementation should simply display an error message
     * and throw a <code>ValidationException</code> to inform the caller about the
     * invalid input.
     * </p>
     *
     * @throws ValidationException if the current settings are not valid.
     *
     * @since 1.0b8
     */
    public void validateSettings()
      throws ValidationException
    {
    }


    /**
     * Creates a list with the string values of the given integer values.
     *
     * @param values array with a set of integers.
     *
     * @return string values of the given array.
     *
     * @since 1.0b9
     */
    protected String[] createItemList(int[] values)
    {
        String[] items = new String[values.length];

        for (int i = 0; i < values.length; i++)
        {
            items[i] = String.valueOf(values[i]).intern();
        }

        return items;
    }


    /**
     * Returns the parent container.
     *
     * @return the parent container. Returns <code>null</code> if no container was
     *         specified upon construction of the page.
     */
    SettingsContainer getContainer()
    {
        return _container;
    }


    /**
     * Returns the file name of the preview file to use for this page. Normally the file
     * name is equivalent to the category name, but pages that use tabbed panes may want
     * to override this method to provide different files for their different panes.
     *
     * @return the file name (no path, without extension) of the preview file to use.
     *         Returns <code>null</code> if the page does not have a preview file
     *         associated.
     *
     * @since 1.0b8
     */
    protected String getPreviewFileName()
    {
        return _category;
    }

    //~ Inner Classes --------------------------------------------------------------------

    private final class UpdateTrigger
        implements ActionListener
    {
        Timer timer;

        public synchronized void actionPerformed(ActionEvent ev)
        {
            if (_container != null)
            {
                if (this.timer == null)
                {
                    this.timer =
                        new Timer(
                            20,
                            new ActionListener()
                            {
                                public void actionPerformed(ActionEvent event)
                                {
                                    UpdateTrigger.this.timer.stop();

                                    PreviewFrame preview = _container.getPreview();
                                    String filename = getPreviewFileName();
                                    String text = _container.loadPreview(filename);

                                    try
                                    {
                                        preview.getCurrentPage().validateSettings();

                                        if (preview.customFile)
                                        {
                                            preview.update();
                                        }
                                        else
                                        {
                                            preview.setText(text);
                                        }
                                    }
                                    catch (ValidationException ex)
                                    {
                                        return;
                                    }
                                    catch (Throwable ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                    this.timer.start();
                }
                else
                {
                    this.timer.restart();
                }
            }
        }
    }
}
