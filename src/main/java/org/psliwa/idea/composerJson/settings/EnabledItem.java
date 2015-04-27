package org.psliwa.idea.composerJson.settings;

import org.jetbrains.annotations.NotNull;

public class EnabledItem {
    private String name;
    private boolean enabled;

    public EnabledItem(@NotNull String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnabledItem that = (EnabledItem) o;

        if (enabled != that.enabled) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
