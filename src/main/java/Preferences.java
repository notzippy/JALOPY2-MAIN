/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
import de.hunsicker.jalopy.swing.SettingsDialog;


/**
 * Shorthand for {@link de.hunsicker.jalopy.swing.SettingsDialog}.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class Preferences
{
    //~ Constructors ---------------------------------------------------------------------

    private Preferences()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Command line entry point. Delegates to {@link
     * de.hunsicker.jalopy.swing.SettingsDialog#main}.
     *
     * @param argv command line arguments. Currently no command line arguments are
     *        recognized.
     */
    public static void main(String[] argv)
    {
        SettingsDialog.main(argv);
    }
}
