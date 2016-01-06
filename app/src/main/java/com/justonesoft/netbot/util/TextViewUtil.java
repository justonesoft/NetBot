package com.justonesoft.netbot.util;

import android.widget.TextView;

/**
 * Created by bmunteanu on 4/28/2015.
 *
 * Utility methods to manage a read-only text view
 */
public class TextViewUtil {

    /**
     * Private constructor since will only be static methods
     */
    private TextViewUtil() {}

    public static TextView prefixWithText(final TextView textView, CharSequence textToPrefix, boolean addNewLine) {
        if (textView == null) {
            return null;
        }

        CharSequence currentText = textView.getText();
        if (currentText == null || currentText.length() == 0) {
            // just set the text;
            textView.setText(textToPrefix);
        }
        else {
            final StringBuilder internalSB = new StringBuilder(currentText);
            if (addNewLine) {
                internalSB.insert(0, '\n');
            }
            internalSB.insert(0, textToPrefix);
            textView.setText(internalSB.subSequence(0, internalSB.length()));
        }

        return textView;
    }
}
