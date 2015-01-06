package org.psliwa.idea.composerJson.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PatternItem implements Comparable<PatternItem>, Cloneable {
    @NotNull
    private String pattern = "";

    public PatternItem(String pattern) {
        setPattern(pattern);
    }

    @NotNull
    public final String getPattern() {
        return pattern;
    }

    public final void setPattern(@Nullable String pattern) {
        this.pattern = pattern == null ? "" : pattern;
    }

    @Override
    public int compareTo(@NotNull PatternItem o) {
        return this.pattern.compareTo(o.pattern);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatternItem that = (PatternItem) o;

        return pattern.equals(that.pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    @Override
    public PatternItem clone() {
        try {
            return (PatternItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}