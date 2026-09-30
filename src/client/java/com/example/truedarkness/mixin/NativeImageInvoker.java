package com.example.truedarkness.mixin;

import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * getColor/setColor on NativeImage are private in this build, so plain Java
 * code (even in another mixin class) cannot call them directly. This
 * invoker interface lets Mixin generate the bridge for us.
 */
@Mixin(NativeImage.class)
public interface NativeImageInvoker {
    @Invoker("getColor")
    int truedarkness$getColor(int x, int y);

    @Invoker("setColor")
    void truedarkness$setColor(int x, int y, int color);
}
