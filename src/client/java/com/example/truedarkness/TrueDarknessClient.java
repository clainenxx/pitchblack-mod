package com.example.truedarkness;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * True Darkness
 *
 * Makes unlit places - night time and dark caves/rooms alike - much darker
 * than vanilla. Anywhere the game's own lightmap would normally show a dim
 * grey glow, this mod crushes it much closer to black instead.
 *
 * This is done purely by reshaping the in-game lightmap texture (see
 * {@link com.example.truedarkness.mixin.LightmapTextureManagerMixin}), which
 * only affects how *blocks and entities* are lit. The sky dome - sun, moon
 * and stars - is rendered completely separately from that texture, so it is
 * never touched and stays exactly as bright/visible as vanilla at night.
 *
 * Areas lit by torches, glowstone, lava, etc. still light up normally; it is
 * only the ambient/unlit light levels that get crushed.
 */
public class TrueDarknessClient implements ClientModInitializer {
    public static final String MOD_ID = "truedarkness";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * How aggressively low light levels are crushed towards black.
     * 1.0 = vanilla brightness curve (no change).
     * Values above 1.0 darken low/mid light levels while leaving full
     * brightness (broad daylight) basically untouched. Higher = darker night
     * and darker unlit interiors/caves.
     */
    public static final float DARKNESS_GAMMA = 10.0f;

    /**
     * If true, the darkening is skipped entirely while the local player has
     * Night Vision, so that effect still works exactly like vanilla.
     */
    public static final boolean RESPECT_NIGHT_VISION = true;

    @Override
    public void onInitializeClient() {
        LOGGER.info("True Darkness initialized: nights and unlit areas are now much darker. "
                + "Sun, moon and stars are untouched.");
    }
}
