package net.pineclone.simplecmd.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.pineclone.simplecmd.event.PlayerDeathCallBack;
import net.pineclone.simplecmd.event.PlayerHurtCallBack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract boolean shouldDamagePlayer(PlayerEntity player);

    @Inject(method = "applyDamage" , at=@At("TAIL"), cancellable = true)
    public void applyDamage(DamageSource source, float amount, CallbackInfo info) {
        ActionResult result = PlayerHurtCallBack.EVENT.invoker().interact((PlayerEntity)(Object)this , source , amount);

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }

    @Inject(method = "onDeath" , at=@At("TAIL"), cancellable = true)
    public void onDeath(DamageSource source, CallbackInfo info) {
        ActionResult result = PlayerDeathCallBack.EVENT.invoker().interact((PlayerEntity)(Object)this , source);

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}
