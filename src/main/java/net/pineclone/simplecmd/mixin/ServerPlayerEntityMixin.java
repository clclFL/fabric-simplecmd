package net.pineclone.simplecmd.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.pineclone.simplecmd.event.PlayerChangeDimensionCallBack;
import net.pineclone.simplecmd.event.PlayerLogoutCallBack;
import net.pineclone.simplecmd.event.PlayerSpawnCallBack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "worldChanged" , at=@At("TAIL"), cancellable = true)
    public void worldChanged(ServerWorld origin, CallbackInfo info) {
        ActionResult result = PlayerChangeDimensionCallBack.EVENT.invoker().interact((ServerPlayerEntity) (Object)this , origin);

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }

    @Inject(method = "onDisconnect" , at=@At("HEAD"), cancellable = true)
    public void onDisconnect(CallbackInfo info) {
        ActionResult result = PlayerLogoutCallBack.EVENT.invoker().interact((ServerPlayerEntity) (Object)this);

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }

    @Inject(method = "onSpawn" , at=@At("TAIL"), cancellable = true)
    public void onSpawn(CallbackInfo info) {
        ActionResult result = PlayerSpawnCallBack.EVENT.invoker().interact((ServerPlayerEntity) (Object)this);

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }

}
