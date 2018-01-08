package org.psliwa.idea.composerJson.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class TextItem implements Comparable<TextItem>, Cloneable {
    private String text = "";

    public TextItem(String text) {
        setText(text);
    }

    @NotNull
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text == null ? "" : text;
    }

    @Override
    public int compareTo(@NotNull TextItem o) {
        return text.compareTo(o.text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextItem textItem = (TextItem) o;

        return Objects.equals(text, textItem.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    public TextItem clone() {
        try {
            return (TextItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
