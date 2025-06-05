package sh.sit.endanchor.fabric;

import net.fabricmc.api.ModInitializer;
import sh.sit.endanchor.SitsEndAnchor;

public class FabricSitsEndAnchor implements ModInitializer {
    @Override
    public void onInitialize() {
        SitsEndAnchor.onInitialize();
    }
}
