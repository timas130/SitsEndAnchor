package sh.sit.endanchor.neoforge;

import net.neoforged.fml.common.Mod;
import sh.sit.endanchor.SitsEndAnchor;

@Mod(SitsEndAnchor.MOD_ID)
public final class NeoForgeSitsEndAnchor {
    public NeoForgeSitsEndAnchor() {
        SitsEndAnchor.onInitialize();
    }
}
