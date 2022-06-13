package com.jelly.farmhelper.mixins;

import com.jelly.farmhelper.config.interfaces.MiscConfig;
import com.jelly.farmhelper.features.Resync;
import com.jelly.farmhelper.macros.CropMacro;
import com.jelly.farmhelper.macros.MacroHandler;
import com.jelly.farmhelper.macros.SugarcaneMacro;
import com.jelly.farmhelper.utils.Clock;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockReed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ PlayerControllerMP.class })
public class MixinPlayerControllerMP {
    private final Clock checkTimer = new Clock();

    @Inject(method={"clickBlock"}, at={@At(value="HEAD")}, cancellable=true)
    public void clickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        if (MiscConfig.resync && MacroHandler.isMacroing && MacroHandler.currentMacro != null && MacroHandler.currentMacro.enabled && loc != null && Minecraft.getMinecraft().theWorld.getBlockState(loc) != null &&
                (Minecraft.getMinecraft().theWorld.getBlockState(loc).getBlock() instanceof BlockBush || Minecraft.getMinecraft().theWorld.getBlockState(loc).getBlock() instanceof BlockReed)) {
            if (MacroHandler.currentMacro instanceof SugarcaneMacro || MacroHandler.currentMacro instanceof CropMacro) {
                if (checkTimer.passed()) {
                    Resync.update(loc);
                    checkTimer.schedule(5000);
                }
            }
        }
    }

//    @Inject(method={"clickBlock"}, at={@At(value="HEAD")}, cancellable=true)
//    private void clickBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
//        // LogUtils.debugFullLog("clickBlockHook - " + pos + Minecraft.getMinecraft().theWorld.getBlockState(pos));
//    }

//    @Inject(method={"onPlayerDamageBlock"}, at={ @At(value="HEAD") }, cancellable=true)
//    private void onPlayerDamageBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
//
//    }
//
//    @Inject(method={"resetBlockRemoving"}, at={ @At(value="HEAD") }, cancellable=true)
//    private void resetBlock(CallbackInfo info) {
//
//    }
//
//    @Inject(method={"getBlockReachDistance"}, at={@At(value="RETURN")}, cancellable=true)
//    private void getReachDistanceHook(CallbackInfoReturnable<Float> distance) {
//
//    }
}