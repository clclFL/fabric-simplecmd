package net.pineclone.simplecmd;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.ActionResult;
import net.pineclone.simplecmd.cmds.CommandRegistryHandler;
import net.pineclone.simplecmd.cmds.tpa.status.PreTeleport;
import net.pineclone.simplecmd.cmds.tpa.status.TPAManager;
import net.pineclone.simplecmd.event.PlayerHurtCallBack;
import net.pineclone.simplecmd.utils.Ticker;
import net.pineclone.simplecmd.utils.TomlUtils;
import net.pineclone.simplecmd.utils.Utils;

import java.io.IOException;

public class SimpleCmdMod implements ModInitializer {

    public static final String MOD_ID = "simplecmd";

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {

        //initialize the file.
        try {
            TomlUtils.ensureToml();
        } catch (IOException e) {
            Utils.LOGGER.debug("Cannot correctly initial the config file.");
        }

        Ticker.register();
        TPAManager.register();
        CommandRegistryHandler.register();

/*        PlayerHurtCallBack.EVENT.register((player ,damageSource, amount) -> {
            System.out.println(player.getDisplayName().getString());
            return ActionResult.PASS;
        });*/
    }

}
