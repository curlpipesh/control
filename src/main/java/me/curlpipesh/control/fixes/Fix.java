package me.curlpipesh.control.fixes;

import me.curlpipesh.control.Control;

/**
 * A generic fix that can be applied when the plugin is loaded. Not intended
 * for use from an external plugin.
 *
 * @author audrey
 * @since 12/5/15.
 */
@FunctionalInterface
public interface Fix {
    /**
     * Applies the fix, using the given plugin instance.
     *
     * @param control The plugin instance to use
     */
    void fix(Control control);
}
