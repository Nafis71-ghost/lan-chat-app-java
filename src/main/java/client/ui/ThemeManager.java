package client.ui;

import java.awt.Color;

public class ThemeManager {

    private static boolean dark = false;

    public static final Color FRAME_BG_LIGHT = new Color(236, 240, 245);
    public static final Color CHAT_BG_LIGHT = new Color(248, 250, 252);
    public static final Color TEXT_LIGHT = new Color(35, 45, 55);
    public static final Color BUBBLE_OTHER_LIGHT = Color.WHITE;

    public static final Color FRAME_BG_DARK = new Color(32, 34, 37);
    public static final Color CHAT_BG_DARK = new Color(40, 42, 46);
    public static final Color TEXT_DARK = new Color(230, 230, 230);
    public static final Color BUBBLE_OTHER_DARK = new Color(54, 57, 63);

    private ThemeManager() {
    }

    public static boolean isDark() {
        return dark;
    }

    public static void toggle() {
        dark = !dark;
    }

    public static Color frameBg() {
        return dark ? FRAME_BG_DARK : FRAME_BG_LIGHT;
    }

    public static Color chatBg() {
        return dark ? CHAT_BG_DARK : CHAT_BG_LIGHT;
    }

    public static Color text() {
        return dark ? TEXT_DARK : TEXT_LIGHT;
    }

    public static Color otherBubble() {
        return dark ? BUBBLE_OTHER_DARK : BUBBLE_OTHER_LIGHT;
    }
}