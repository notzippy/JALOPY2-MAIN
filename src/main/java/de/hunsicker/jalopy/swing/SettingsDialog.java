/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.swing.ErrorDialog;
import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.ResourceBundleFactory;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;


/**
 * The Jalopy settings dialog provides a graphical user interface to manage, display and
 * interactively test and edit the available code convention and asorted configuration
 * settings.
 * 
 * <p>
 * The dialog can be used from other Java applications as usual, in which case it acts
 * like any other JDialog (i.e. as a secondary window). But it may be also invoked
 * directly from the command line, magically serving as the main application window.
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class SettingsDialog
    extends JDialog
{
    //~ Static variables/initializers ----------------------------------------------------

    /**
     * Command line option that indicates that the settings dialog was called from within
     * Eclipse. Used to prevent the call to System.exit() upon closure.
     */
    public static final String ARG_ECLIPSE = "-eclipse" /* NOI18N */;

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME =
        "de.hunsicker.jalopy.swing.Bundle" /* NOI18N */;

    /** A frame that displays the contents of a Java source file. */
    static PreviewFrame _previewFrame;

    //~ Instance variables ---------------------------------------------------------------

    /** Button to save the state, don't closes the dialog. */
    private JButton _applyButton;

    /** Button to discard changes and close the dialog. */
    private JButton _cancelButton;

    /** Button to close the dialog, the options will be saved. */
    private JButton _okButton;

    /** The container that contains the tree and page views. */
    SettingsContainer _preferencesContainer;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new SettingsDialog object.
     *
     * @param owner the frame from which the dialog is displayed.
     */
    protected SettingsDialog(Frame owner)
    {
        super(
            owner,
            ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                "TLE_JALOPY_SETTINGS" /* NOI18N */));
    }


    /**
     * Creates a new SettingsDialog object.
     *
     * @param owner the dialog from which the dialog is displayed.
     */
    protected SettingsDialog(Dialog owner)
    {
        super(
            owner,
            ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                "TLE_JALOPY_SETTINGS" /* NOI18N */));
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Creates a new SettingsDialog object.
     *
     * @param owner the window from which the dialog is displayed.
     * @param title the string to display in the title bar.
     *
     * @return a new SettingsDialog object.
     *
     * @since 1.0b9
     */
    public static SettingsDialog create(
        Window owner,
        String title)
    {
        SettingsDialog dialog = create(owner);
        dialog.setTitle(title);

        return dialog;
    }


    /**
     * Creates a new SettingsDialog object.
     *
     * @param owner the window from which the dialog is displayed.
     *
     * @return a new SettingsDialog object.
     *
     * @since 1.0b9
     */
    public static SettingsDialog create(Window owner)
    {
        SettingsDialog dialog = null;

        if (owner instanceof Frame)
        {
            dialog = new SettingsDialog((Frame) owner);
        }
        else if (owner instanceof Dialog)
        {
            dialog = new SettingsDialog((Dialog) owner);
        }

        dialog.initialize();

        return dialog;
    }


    /**
     * {@inheritDoc}      Overriden to dispatch the call to the top-level container if
     * invoked from the command line.
     */
    public int getHeight()
    {
        Container c = getParent();

        if (c instanceof SettingsFrame)
        {
            return c.getHeight();
        }
        return super.getHeight();
    }


    /**
     * {@inheritDoc} Overriden to dispatch the call to the top-level container if invoked
     * from the command line.
     */
    public void setLocation(
        int x,
        int y)
    {
        Container c = getParent();

        if (c instanceof SettingsFrame)
        {
            c.setLocation(x, y);
        }
        else
        {
            super.setLocation(x, y);
        }
    }


    /**
     * {@inheritDoc} Overriden to dispatch the call to the top-level container if invoked
     * from the command line.
     */
    public Point getLocation()
    {
        Container c = getParent();

        if (c instanceof SettingsFrame)
        {
            return c.getLocation();
        }
        return super.getLocation();
    }


    /**
     * {@inheritDoc} Overriden to dispatch the call to the top-level container if invoked
     * from the command line.
     */
    public int getWidth()
    {
        Container c = getParent();

        if (c instanceof SettingsFrame)
        {
            return c.getWidth();
        }
        return super.getWidth();
    }


    /**
     * {@inheritDoc} Overriden to dispatch the call to the top-level container if invoked
     * from the command line.
     */
    public int getX()
    {
        Container c = getParent();

        if (c instanceof SettingsFrame)
        {
            return c.getX();
        }
        return super.getX();
    }


    /**
     * {@inheritDoc} Overriden to dispatch the call to the top-level container if invoked
     * from the command line.
     */
    public int getY()
    {
        Container c = getParent();

        if (c instanceof SettingsFrame)
        {
            return c.getY();
        }
        return super.getY();
    }


    /**
     * Displays the settings dialog. The dialog then uses a {@link javax.swing.JFrame} as
     * its top-level container.
     *
     * @param argv command line arguments. <p>
     */
    public static void main(String[] argv)
    {
        if ((argv.length == 0) || (!argv[0].equals(SettingsDialog.ARG_ECLIPSE)))
        {
            Loggers.initialize(
                new ConsoleAppender(
                    new PatternLayout("[%p] %m\n" /* NOI18N */), "System.out" /* NOI18N */));

            //Loggers.initialize(new NullAppender());
        }

        for (int i = 0; i < argv.length; i++)
        {
            if (argv[i].equals("-ui" /* NOI18N */))
            {
                initializeLookAndFeel(argv[i + 1]);
                i++;
            }
        }

        final SettingsFrame frame =
            new SettingsFrame(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "TLE_JALOPY_SETTINGS" /* NOI18N */), argv);
        final SettingsDialog dialog = SettingsDialog.create(frame);
        frame.dialog = dialog;
        frame.getContentPane().add(dialog.getContentPane());
        frame.addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent ev)
                {
                    dialog._preferencesContainer.dispose();

                    if (frame.isExitOnClose)
                    {
                        System.exit(0);
                    }
                    else
                    {
                        frame.dispose();
                        SettingsDialog._previewFrame.dispose();
                    }
                }


                public void windowOpened(WindowEvent ev)
                {
                    dialog._preferencesContainer.displayPreview();
                }
            });
        frame.pack();

        // center on screen
        Toolkit kit = dialog.getToolkit();
        frame.setLocation(
            (kit.getScreenSize().width - frame.getWidth()) / 2,
            (kit.getScreenSize().height - frame.getHeight()) / 2);
        frame.setVisible(true);
    }


    /**
     * {@inheritDoc} Overriden to dispatch the call to the top-level container if invoked
     * from the command line.
     */
    public void show()
    {
        if (_previewFrame != null)
        {
            if (!_previewFrame.isVisible())
            {
                _preferencesContainer.displayPreview();
            }
            else
            {
                _previewFrame.toFront();
            }
        }

        super.show();
    }


    /**
     * {@inheritDoc} Overriden to dispatch the call to the top-level container if invoked
     * from the command line.
     */
    public void toFront()
    {
        Container c = getParent();

        if (c instanceof SettingsFrame)
        {
            ((SettingsFrame) c).toFront();
            c.requestFocus();
        }
        else
        {
            super.toFront();
        }
    }


    /**
     * Initializes settings for the Look&amp;Feel.
     *
     * @param clazz the classname of the Look&amp;Feel class to initialize.
     */
    private static void initializeLookAndFeel(String clazz)
    {
        if (clazz != null)
        {
            try
            {
                UIManager.setLookAndFeel(clazz);
            }
            catch (Throwable ex)
            {
                Object[] args = { clazz };
                System.err.println(
                    MessageFormat.format(
                        ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                            "MSG_NOT_FIND_LAF" /* NOI18N */), args));
            }
        }

        if (UIManager.getLookAndFeel().getID().equals("Metal" /* NOI18N */))
        {
            int fontSize = 12;
            String fontName = "Tahoma" /* NOI18N */;

            Font dialogPlain = new Font(fontName, Font.PLAIN, fontSize);
            Font serifPlain = new Font("Serif" /* NOI18N */, Font.PLAIN, fontSize);
            Font sansSerifPlain =
                new Font("SansSerif" /* NOI18N */, Font.PLAIN, fontSize);
            Font monospacedPlain =
                new Font("Monospaced" /* NOI18N */, Font.PLAIN, fontSize);

            UIManager.put("Border.font" /* NOI18N */, dialogPlain);
            UIManager.put("InternalFrameTitlePane.font" /* NOI18N */, dialogPlain);
            UIManager.put("OptionPane.font" /* NOI18N */, dialogPlain);
            UIManager.put("DesktopIcon.font" /* NOI18N */, dialogPlain);
            UIManager.put("Button.font" /* NOI18N */, dialogPlain);
            UIManager.put("ToggleButton.font" /* NOI18N */, dialogPlain);
            UIManager.put("RadioButton.font" /* NOI18N */, dialogPlain);
            UIManager.put("CheckBox.font" /* NOI18N */, dialogPlain);
            UIManager.put("ColorChooser.font" /* NOI18N */, dialogPlain);
            UIManager.put("ComboBox.font" /* NOI18N */, dialogPlain);
            UIManager.put("Label.font" /* NOI18N */, dialogPlain);
            UIManager.put("List.font" /* NOI18N */, dialogPlain);
            UIManager.put("MenuBar.font" /* NOI18N */, dialogPlain);
            UIManager.put("MenuItem.font" /* NOI18N */, dialogPlain);
            UIManager.put("RadioButtonMenuItem.font" /* NOI18N */, dialogPlain);
            UIManager.put("CheckBoxMenuItem.font" /* NOI18N */, dialogPlain);
            UIManager.put("Menu.font" /* NOI18N */, dialogPlain);
            UIManager.put("PopupMenu.font" /* NOI18N */, dialogPlain);
            UIManager.put("OptionPane.font" /* NOI18N */, dialogPlain);
            UIManager.put("Panel.font" /* NOI18N */, dialogPlain);
            UIManager.put("ProgressBar.font" /* NOI18N */, dialogPlain);
            UIManager.put("ScrollPane.font" /* NOI18N */, dialogPlain);
            UIManager.put("Viewport.font" /* NOI18N */, dialogPlain);
            UIManager.put("TabbedPane.font" /* NOI18N */, dialogPlain);
            UIManager.put("Table.font" /* NOI18N */, dialogPlain);
            UIManager.put("TableHeader.font" /* NOI18N */, dialogPlain);
            UIManager.put("TextField.font" /* NOI18N */, sansSerifPlain);
            UIManager.put("PasswordField.font" /* NOI18N */, monospacedPlain);
            UIManager.put("TextArea.font" /* NOI18N */, monospacedPlain);
            UIManager.put("TextPane.font" /* NOI18N */, serifPlain);
            UIManager.put("EditorPane.font" /* NOI18N */, serifPlain);
            UIManager.put("TitledBorder.font" /* NOI18N */, dialogPlain);
            UIManager.put("ToolBar.font" /* NOI18N */, dialogPlain);
            UIManager.put("ToolTip.font" /* NOI18N */, sansSerifPlain);
            UIManager.put("Tree.font" /* NOI18N */, dialogPlain);
        }
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        _okButton =
            SwingHelper.createButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_OK" /* NOI18N */), false);
        _okButton.addActionListener(new OkActionHandler());

        Container container = getParent();

        // we've been invoked from the command line
        if (container instanceof SettingsFrame)
        {
            ((JFrame) container).getRootPane().setDefaultButton(_okButton);
            _previewFrame = PreviewFrame.create(getOwner());
            _preferencesContainer = new SettingsContainer(_previewFrame);
        }
        else
        {
            getRootPane().setDefaultButton(_okButton);
            _previewFrame = PreviewFrame.create(getOwner(), this);
            _preferencesContainer = new SettingsContainer(_previewFrame);
        }

        GridBagLayout layout = new GridBagLayout();
        Container contentPane = getContentPane();
        contentPane.setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, 10, 10, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        layout.setConstraints(_preferencesContainer, c);
        contentPane.add(_preferencesContainer);

        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 7, 11, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        layout.setConstraints(_okButton, c);
        contentPane.add(_okButton);

        _applyButton =
            SwingHelper.createButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_APPLY" /* NOI18N */), false);
        _applyButton.addActionListener(new ApplyActionHandler());
        c.insets.left = 0;
        SwingHelper.setConstraints(
            c, 8, 11, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        layout.setConstraints(_applyButton, c);
        contentPane.add(_applyButton);

        _cancelButton =
            SwingHelper.createButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_CANCEL" /* NOI18N */), false);
        _cancelButton.addActionListener(new CancelActionHandler());
        c.insets.right = 20;
        SwingHelper.setConstraints(
            c, 9, 11, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        layout.setConstraints(_cancelButton, c);
        contentPane.add(_cancelButton);

        addWindowListener(
            new WindowAdapter()
            {
                public void windowOpened(WindowEvent ev)
                {
                    _preferencesContainer.displayPreview();
                }


                public void windowClosing(WindowEvent ev)
                {
                    _preferencesContainer.dispose();
                    _previewFrame.dispose();
                }
            });
    }


    /**
     * Stores the settings.
     *
     * @return <code>true</code> if the settings were stored sucessfully.
     */
    boolean updateSettings()
    {
        try
        {
            _preferencesContainer.updateSettings();
        }
        catch (ValidationException ex)
        {
            // we displayed a message box to inform the user
            return false;
        }
        catch (Throwable ex)
        {
            final Window owner = SwingUtilities.windowForComponent(this);

            ErrorDialog dialog = ErrorDialog.create(owner, ex);
            dialog.setVisible(true);
            dialog.dispose();

            return false;
        }

        try
        {
            // save the code convention
            Convention.getInstance().flush();
        }
        catch (Throwable ex)
        {
            ErrorDialog dialog = ErrorDialog.create(this, ex);
            dialog.setVisible(true);
            dialog.dispose();

            return false;
        }

        return true;
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * The sole purpose of this class lies in the fact that we want to tell whether the
     * dialog was invoked from within a Plug-in or from the command line.
     */
    private static final class SettingsFrame
        extends JFrame
    {
        SettingsDialog dialog;

        /**
         * Indicates whether we should exit the application upon user confirmation or
         * cancelation.
         */
        boolean isExitOnClose = true;

        public SettingsFrame(
            String   title,
            String[] argv)
        {
            super(title);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setIconImage(
                new ImageIcon(
                    getClass().getResource("resources/Preferences16.gif" /* NOI18N */))
                .getImage());

            if (argv.length == 1)
            {
                if (argv[0].equals(SettingsDialog.ARG_ECLIPSE))
                {
                    this.isExitOnClose = false;
                }
            }
        }
    }


    /**
     * Handler for the Apply button. Stores the code convention.
     */
    private class ApplyActionHandler
        implements ActionListener
    {
        public void actionPerformed(ActionEvent ev)
        {
            updateSettings();
        }
    }


    /**
     * Handler for the Cancel button. Closes the dialog.
     */
    private final class CancelActionHandler
        implements ActionListener
    {
        public void actionPerformed(ActionEvent ev)
        {
            _previewFrame.dispose();
            _preferencesContainer.dispose();
            dispose();

            Container c = getParent();

            // was the dialog invoked from the command line?
            if (c instanceof SettingsFrame)
            {
                SettingsFrame f = (SettingsFrame) c;

                if (f.isExitOnClose)
                {
                    System.exit(0);
                }
                else
                {
                    f.dispose();
                }
            }
        }
    }


    /**
     * Handler for the OK button. Stores the code convention and closes the dialog.
     */
    private final class OkActionHandler
        implements ActionListener
    {
        public void actionPerformed(ActionEvent ev)
        {
            if (updateSettings())
            {
                _previewFrame.dispose();
                _preferencesContainer.dispose();
                dispose();

                Container c = getParent();

                // was the dialog invoked from the command line?
                if (c instanceof SettingsFrame)
                {
                    SettingsFrame f = (SettingsFrame) c;

                    if (f.isExitOnClose)
                    {
                        System.exit(0);
                    }
                    else
                    {
                        f.dispose();
                    }
                }
            }
        }
    }
}
