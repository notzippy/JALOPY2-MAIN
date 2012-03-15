/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


/**
 * ComboBoxEditor which only allows whole numbers as user input.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
class NumberComboBoxEditor
    extends BasicComboBoxEditor
{
    //~ Static variables/initializers ----------------------------------------------------

    static final String EMPTY_STRING = "" /* NOI18N */.intern();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new NumberComboBoxEditor.
     *
     * @param value the initial value to display.
     * @param columns number of columns to use.
     */
    public NumberComboBoxEditor(
        final int value,
        int       columns)
    {
        final NumberTextField textField = new NumberTextField(value, columns);
        textField.addFocusListener(
            new FocusAdapter()
            {
                public void focusLost(FocusEvent ev)
                {
                    String test = textField.getText();

                    // if the field contains no valid number, we use a default value
                    if ((test == null) || EMPTY_STRING.equals(test.trim()))
                    {
                        textField.setText(String.valueOf(value));
                    }
                }
            });
        this.editor = textField;
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * TextField used to display and edit the user value.
     */
    private static class NumberTextField
        extends JTextField
    {
        final int initialValue;
        NumberFormat integerFormatter;

        /**
         * Creates a new NumberTextField object.
         *
         * @param value
         * @param columns
         */
        public NumberTextField(
            int value,
            int columns)
        {
            super(columns);

            NumberDocument document = (NumberDocument) getDocument();
            this.initialValue = value;
            document.initialValue = value;
            this.integerFormatter = NumberFormat.getNumberInstance();
            this.integerFormatter.setParseIntegerOnly(true);
            setValue(value);
        }

        public void setValue(int value)
        {
            setText(integerFormatter.format(value));
        }


        public int getValue()
        {
            try
            {
                return integerFormatter.parse(getText()).intValue();
            }
            catch (ParseException ex)
            {
                return this.initialValue;
            }
        }


        protected Document createDefaultModel()
        {
            return new NumberDocument();
        }

        private static class NumberDocument
            extends PlainDocument
        {
            int initialValue;

            public NumberDocument()
            {
            }

            public void insertString(
                int          offs,
                String       str,
                AttributeSet atts)
              throws BadLocationException
            {
                char[] source = str.toCharArray();
                char[] result = new char[source.length];
                int j = 0;

                for (int i = 0; i < result.length; i++)
                {
                    if (Character.isDigit(source[i]))
                    {
                        result[j++] = source[i];
                    }
                    else if (offs == 0)
                    {
                        super.insertString(offs, String.valueOf(this.initialValue), atts);

                        return;
                    }
                }

                super.insertString(offs, new String(result, 0, j), atts);
            }
        }
    }
}
