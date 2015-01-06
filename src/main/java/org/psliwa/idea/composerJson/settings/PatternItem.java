package org.psliwa.idea.composerJson.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class PatternItem implements Comparable<PatternItem>, Cloneable {
    @NotNull
    private String pattern = "";

    private Pattern regexPattern;

    public PatternItem(String pattern) {
        setPattern(pattern);
    }

    @NotNull
    public String getPattern() {
        return pattern;
    }

    public void setPattern(@Nullable String pattern) {
        this.pattern = pattern == null ? "" : pattern;
    }

    public boolean matches(String text) {
        try {
            return getRegexPattern().matcher(text).matches();
        } catch(PatternSyntaxException e) {
            return false;
        }
    }

    private Pattern getRegexPattern() {
        if(regexPattern == null) {
            int index, previousIndex = 0;
            StringBuilder product = new StringBuilder("^");

            while((index = pattern.indexOf('*', previousIndex)) >= 0) {
                product.append(Pattern.quote(pattern.substring(previousIndex, index)))
                    .append(".*");
                previousIndex = index + 1;
            }

            product.append(Pattern.quote(pattern.substring(previousIndex)));
            product.append("$");

            regexPattern = Pattern.compile(product.toString());
        }

        return regexPattern;
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