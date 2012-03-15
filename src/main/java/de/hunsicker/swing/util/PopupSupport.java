/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.swing.util;

import java.awt.AWTEvent;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import de.hunsicker.util.ResourceBundleFactory;


/**
 * Helper class which adds popup menu support for {@link javax.swing.text.JTextComponent
 * text components}.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class PopupSupport
{
    //~ Static variables/initializers ----------------------------------------------------

    static final Comparator COMPARATOR = new PartialStringComparator();
    static final String EMPTY_STRING = "" /* NOI18N */.intern();

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME =
        "de.hunsicker.swing.util.Bundle" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** The Copy action. */
    private Action _copyAction;

    /* The Cut action. */
    private Action _cutAction;

    /** The Delete action. */
    private Action _deleteAction;

    /** The Paste action. */
    private Action _pasteAction;

    /** The Select All action. */
    private Action _selectAllAction;

    /** The focus event interceptor. */
    private FocusInterceptor _interceptor;

    /** The popup menu to display. */
    private JPopupMenu _menu;

    /** Holds a list of all text components that have popup support. */
    private List _registeredComponents; // List of <ListenerSupport>

    /** List with the package names for which popup support should be enabled. */
    final List _supported; // List of <String>

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new PopupSuppport object. The popup menu support is initially enabled.
     * The popup support will only be added for components of the
     * <code>javax.swing</code> hierachy.
     */
    public PopupSupport()
    {
        this(true);
    }


    /**
     * Creates a new PopupSuppport object. The popup menu support is initially enabled
     * for the given hierachies or classes.
     *
     * @param supported supported package hierarchies or classes.
     */
    public PopupSupport(List supported)
    {
        this(true, supported);
    }


    /**
     * Creates a new PopupSuppport object.
     *
     * @param enable if <code>true</code> the popup menu support will be initially
     *        enabled.
     * @param supported supported package hierarchies or classes.
     */
    public PopupSupport(
        boolean    enable,
        final List supported)
    {
        if (enable)
        {
            setEnabled(true);
        }

        _supported = new ArrayList(supported);
        Collections.sort(_supported);
    }


    /**
     * Creates a new PopupSuppport object.
     *
     * @param enable if <code>true</code> the popup menu support will be initially
     *        enabled.
     */
    public PopupSupport(boolean enable)
    {
        if (enable)
        {
            setEnabled(true);
        }

        _supported = new ArrayList(3);
        _supported.add("javax.swing." /* NOI18N */);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the status of the popup menu support.
     *
     * @param enable if <code>true</code> the popup menu support will be enabled.
     */
    public void setEnabled(boolean enable)
    {
        if (enable)
        {
            if (_interceptor == null)
            {
                _interceptor = new FocusInterceptor();
                Toolkit.getDefaultToolkit().addAWTEventListener(
                    _interceptor, AWTEvent.FOCUS_EVENT_MASK);
            }
        }
        else
        {
            Toolkit.getDefaultToolkit().removeAWTEventListener(_interceptor);
            _interceptor = null;

            // if no text component have ever got the focus no listeners were
            // added, so we have to check
            if (_registeredComponents != null)
            {
                for (int i = 0, size = _registeredComponents.size(); i < size; i++)
                {
                    ListenerSupport support =
                        (ListenerSupport) _registeredComponents.get(i);
                    support.remove();
                }
            }

            _registeredComponents = null;
            _copyAction = null;
            _cutAction = null;
            _selectAllAction = null;
            _pasteAction = null;
            _deleteAction = null;
            _menu = null;
        }
    }


    /**
     * Adds a default popup menu for the given component.
     *
     * @param component component to add the popup menu to.
     */
    public void addSupport(JTextComponent component)
    {
        if (component == null)
        {
            return;
        }

        if (_registeredComponents == null)
        {
            _registeredComponents = new ArrayList(10);
        }

        if (!_registeredComponents.contains(new ListenerSupport(component)))
        {
            ListenerSupport support =
                new ListenerSupport(component, new MouseHandler(), new KeyHandler());
            _registeredComponents.add(support);
        }
        else
        {
            updateSelectAllAction(component.getDocument());
            updateDeleteAction(component);
        }
    }


    /**
     * Indicates whether the system clipboard contains text content.
     *
     * @return <code>true</code> if the system clipboard contains no text content.
     */
    protected boolean isClipboardEmpty()
    {
        Transferable data =
            Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);

        if ((data == null) || !data.isDataFlavorSupported(DataFlavor.stringFlavor))
        {
            return true;
        }

        return false;
    }


    /**
     * Returns the popup menu for the given component. The default implementation returns
     * the same default popup menu for all components.
     * 
     * <p>
     * This popup menu consists of five actions:
     * </p>
     * <pre>
     * +------------+
     * | Copy       |
     * | Cut        |
     * | Paste      |
     * | Delete     |
     * +------------+
     * | Select all |
     * +------------+
     * </pre>
     *
     * @param component component to return the popup menu for.
     *
     * @return the popup menu for the given component.
     */
    protected JPopupMenu getPopup(JTextComponent component)
    {
        if (_menu == null)
        {
            _menu = new JPopupMenu();

            Action[] actions = component.getActions();

            for (int i = 0; i < actions.length; i++)
            {
                Object value = actions[i].getValue(Action.NAME);

                if (value.equals(DefaultEditorKit.cutAction))
                {
                    _cutAction = actions[i];
                }
                else if (value.equals(DefaultEditorKit.copyAction))
                {
                    _copyAction = actions[i];
                }
                else if (value.equals(DefaultEditorKit.pasteAction))
                {
                    _pasteAction = actions[i];
                }
                else if (value.equals(DefaultEditorKit.selectAllAction))
                {
                    _selectAllAction = actions[i];
                }
            }

            if (_cutAction != null)
            {
                JMenuItem item = new JMenuItem(_cutAction);
                item.setText(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "MNU_CUT" /* NOI18N */));
                _menu.add(item);
            }

            if (_copyAction != null)
            {
                JMenuItem item = new JMenuItem(_copyAction);
                item.setText(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "MNU_COPY" /* NOI18N */));
                _menu.add(item);
            }

            if (_pasteAction != null)
            {
                JMenuItem item = new JMenuItem(_pasteAction);
                item.setText(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "MNU_PASTE" /* NOI18N */));
                _menu.add(item);
            }

            if (_deleteAction == null)
            {
                _deleteAction = new DeleteAction();
            }

            _menu.add(_deleteAction);

            if (_selectAllAction != null)
            {
                _menu.add(new JPopupMenu.Separator());

                JMenuItem item = new JMenuItem(_selectAllAction);
                item.setText(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "MNU_SELECT_ALL" /* NOI18N */));
                _menu.add(item);
            }
        }

        updateCopyCutAction(component);
        updatePasteAction(component);
        updateDeleteAction(component);
        updateSelectAllAction(component.getDocument());

        return _menu;
    }


    /**
     * Indicates whether a text selection exists.
     *
     * @param start start offset of the text's selection start.
     * @param end end offset of the text's selection end.
     *
     * @return <code>true</code> if <code>start &gt; end</code>.
     */
    protected boolean isTextSelected(
        int start,
        int end)
    {
        if (start >= end)
        {
            return false;
        }

        return true;
    }


    private void updateCopyCutAction(JTextComponent component)
    {
        int startOffset = component.getSelectionStart();
        int endOffset = component.getSelectionEnd();

        if (isTextSelected(startOffset, endOffset))
        {
            if (_copyAction != null)
            {
                _copyAction.setEnabled(true);
            }

            if ((_cutAction != null) && component.isEditable())
            {
                _cutAction.setEnabled(true);
            }
        }
        else
        {
            if (_copyAction != null)
            {
                _copyAction.setEnabled(false);
            }

            if (_cutAction != null)
            {
                _cutAction.setEnabled(false);
            }
        }
    }


    private void updateDeleteAction(JTextComponent component)
    {
        if (component.isEditable() && (component.getDocument().getLength() > 0))
        {
            if (_deleteAction != null)
            {
                _deleteAction.setEnabled(true);
            }
        }
        else
        {
            if (_deleteAction != null)
            {
                _deleteAction.setEnabled(false);
            }
        }
    }


    private void updatePasteAction(JTextComponent component)
    {
        if (_pasteAction != null)
        {
            if (component.isEditable() && !isClipboardEmpty())
            {
                _pasteAction.setEnabled(true);
            }
            else
            {
                _pasteAction.setEnabled(false);
            }
        }
    }


    /**
     * Updates the state of the select-all text action depending on the state of the
     * given document.
     *
     * @param document document of a text component.
     */
    private void updateSelectAllAction(Document document)
    {
        if (document.getLength() > 0)
        {
            if (_selectAllAction != null)
            {
                _selectAllAction.setEnabled(true);
            }
        }
        else
        {
            if (_selectAllAction != null)
            {
                _selectAllAction.setEnabled(false);
            }
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Action which - depending on the selection state of a text component - either
     * deletes the selection or the whole text.
     */
    private static final class DeleteAction
        extends TextAction
    {
        /**
         * Creates a new DeleteAction object.
         */
        public DeleteAction()
        {
            super("clear-action" /* NOI18N */);
            putValue(
                Action.NAME,
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "MNU_DELETE" /* NOI18N */));
            this.setEnabled(false);
        }

        public void actionPerformed(ActionEvent ev)
        {
            JTextComponent target = getTextComponent(ev);

            if (target == null)
            {
                return;
            }

            Caret caret = target.getCaret();
            int curPos = caret.getDot();
            int markPos = caret.getMark();

            if (curPos != markPos)
            {
                try
                {
                    int span = markPos - curPos;

                    if (span < 0)
                    {
                        span = (span * -1);
                        curPos = markPos;
                    }

                    Document document = target.getDocument();
                    document.remove(curPos, span);
                }
                catch (Exception neverOccurs)
                {
                    ;
                }
            }
            else
            {
                target.setText(EMPTY_STRING);
            }
        }
    }


    /**
     * Helper class to bundle a component and associated listeners so we are able to
     * correctly remove listenes after we're done.
     */
    private static final class ListenerSupport
    {
        JTextComponent component;
        KeyListener keyHandler;
        MouseListener mouseHandler;

        public ListenerSupport(JTextComponent component)
        {
            this.component = component;
        }


        public ListenerSupport(
            JTextComponent component,
            MouseListener  mouseListener,
            KeyListener    keyListener)
        {
            this(component);
            this.mouseHandler = mouseListener;
            this.keyHandler = keyListener;

            add();
        }

        public void add()
        {
            this.component.addMouseListener(this.mouseHandler);
            this.component.addKeyListener(this.keyHandler);
        }


        public boolean equals(Object o)
        {
            if (o instanceof JTextComponent)
            {
                return this.component.equals(o);
            }
            else if (o instanceof ListenerSupport)
            {
                return this.component.equals(((ListenerSupport) o).component);
            }

            return false;
        }


        public int hashCode()
        {
            return this.component.hashCode();
        }


        public void remove()
        {
            this.component.removeMouseListener(this.mouseHandler);
            this.component.removeKeyListener(this.keyHandler);
        }
    }


    private static final class PartialStringComparator
        implements Comparator
    {
        public int compare(
            Object o1,
            Object o2)
        {
            String s1 = (String) o1;
            String s2 = (String) o1;

            if (s2.startsWith(s1))
            {
                return 0;
            }

            return s1.compareTo(s2);
        }
    }


    /**
     * Handler which 'spies' on the AWT event dispatching thread and intercepts focus
     * events in order to add a popup menu to a text component which just gained the
     * input focus.
     */
    private class FocusInterceptor
        implements AWTEventListener
    {
        public void eventDispatched(AWTEvent ev)
        {
            if (ev.getID() == FocusEvent.FOCUS_GAINED)
            {
                if (ev.getSource() instanceof JTextComponent)
                {
                    if (
                        Collections.binarySearch(
                            _supported, ev.getSource().getClass().getName(), COMPARATOR) > -1)
                    {
                        addSupport((JTextComponent) ev.getSource());
                    }
                }
            }
        }
    }


    private final class KeyHandler
        extends KeyAdapter
    {
        public void keyPressed(KeyEvent ev)
        {
            if (ev.isShiftDown() && (ev.getKeyCode() == KeyEvent.VK_F10))
            {
                JTextComponent component = (JTextComponent) ev.getSource();

                if (component.isShowing())
                {
                    try
                    {
                        Rectangle r = component.modelToView(component.getCaretPosition());
                        getPopup(component).show(component, r.x, r.y);
                    }
                    catch (BadLocationException ignored)
                    {
                        ;
                    }
                }
            }
        }
    }


    /**
     * Handler which updates the state of the actions for mouse events.
     */
    private final class MouseHandler
        extends MouseAdapter
    {
        public void mousePressed(MouseEvent ev)
        {
            ((JComponent) ev.getSource()).requestFocus();
        }


        public void mouseReleased(MouseEvent ev)
        {
            if (ev.isPopupTrigger())
            {
                JTextComponent component = (JTextComponent) ev.getSource();
                getPopup(component).show(component, ev.getX(), ev.getY());
            }
        }
    }
}
