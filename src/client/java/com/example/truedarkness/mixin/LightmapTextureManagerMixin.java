package com.example.truedarkness.mixin;

import com.example.truedarkness.TrueDarknessClient;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * As of Minecraft 1.21.5, LightmapTextureManager.update(float) no longer
 * touches any CPU-side pixel buffer at all. The entire 16x16 lightmap
 * texture is painted by the GPU in a single shader render pass
 * (RenderPipelines.BILT_SCREEN_LIGHTMAP), driven entirely by a handful of
 * float uniforms handed to RenderPass.setUniform(String, float[]):
 * AmbientLightFactor, SkyFactor, BlockFactor, SkyLightColor,
 * NightVisionFactor, DarknessScale, DarkenWorldFactor and BrightnessFactor.
 * There is no per-pixel NativeImage to darken after the fact anymore, so
 * intercepting a writeToTexture call (what earlier Minecraft versions
 * needed) can never work here - the previous attempts at that all silently
 * failed to bind because that call site no longer exists in this method.
 *
 * The two uniforms that actually control how dark night/caves look are:
 *  - "SkyFactor": the base sky-light brightness for this frame (0..1).
 *    We crush this with the same gamma curve used before.
 *  - "BrightnessFactor": literally `max(0, videoSettingsGamma - darknessEffect)`.
 *    This is the in-game Video Settings "Brightness" slider baked straight
 *    into the lightmap shader. Whatever DARKNESS_GAMMA is set to, if this
 *    stays untouched the player's own Brightness slider can wash the
 *    darkening right back out. We zero it so the slider can no longer
 *    brighten the lightmap at all.
 * Both are intercepted via a single @Redirect on every
 * RenderPass.setUniform(String, float[]) call inside update(), branching
 * on the uniform name.
 *
 * As a second, independent layer we also darken the sky-light brightness
 * curve {@link LightmapTextureManager#getBrightness(DimensionType, int)}
 * itself, used elsewhere for light-level calculations outside this shader
 * pass.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    private static boolean truedarkness$loggedUniformPath = false;

    // ---- Layer 1: darken the sky-light brightness curve directly ----

    @Inject(
            method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void truedarkness$crushSkyBrightness(
            DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (truedarkness$hasNightVision()) {
            return;
        }
        float original = cir.getReturnValue();
        float clamped = Math.max(0f, Math.min(1f, original));
        float crushed = (float) Math.pow(clamped, TrueDarknessClient.DARKNESS_GAMMA);
        cir.setReturnValue(crushed);
    }

    // ---- Layer 2: crush the actual lightmap-shader uniforms ----

    @Redirect(
            method = "update",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;[F)V"
            )
    )
    private void truedarkness$modifyLightmapUniform(RenderPass renderPass, String name, float[] values) {
        if (!truedarkness$loggedUniformPath) {
            truedarkness$loggedUniformPath = true;
            TrueDarknessClient.LOGGER.info("True Darkness: lightmap shader uniform path is active");
        }

        if (!truedarkness$hasNightVision()) {
            if ("SkyFactor".equals(name) && values.length >= 1) {
                float clamped = Math.max(0f, Math.min(1f, values[0]));
                values = new float[]{(float) Math.pow(clamped, TrueDarknessClient.DARKNESS_GAMMA)};
            } else if ("BrightnessFactor".equals(name)) {
                // Neutralise the Video Settings "Brightness" slider entirely
                // for the lightmap - it would otherwise wash the darkening
                // back out regardless of DARKNESS_GAMMA.
                values = new float[]{0f};
            }
        }

        renderPass.setUniform(name, values);
    }

    // ---- shared helpers ----

    private static boolean truedarkness$hasNightVision() {
        if (!TrueDarknessClient.RESPECT_NIGHT_VISION) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null
                && client.player != null
                && client.player.hasStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
net.minecraft.client.render.Camera