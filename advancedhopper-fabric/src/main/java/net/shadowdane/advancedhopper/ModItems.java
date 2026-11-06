package net.shadowdane.advancedhopper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final Item UPGRADE_BASE = registerItem("upgrade_base");
    public static final Item STACK_UPGRADE_1 = registerItem("stack_upgrade_1");
    public static final Item STACK_UPGRADE_2 = registerItem("stack_upgrade_2");
    public static final Item STACK_UPGRADE_3 = registerItem("stack_upgrade_3");
    public static final Item STACK_UPGRADE_4 = registerItem("stack_upgrade_4");
    public static final Item STACK_UPGRADE_5 = registerItem("stack_upgrade_5");

    private static Item registerItem(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(
                FabricEntryPoint.MOD_ID, name
        );

        ResourceKey<Item> itemKey =
                ResourceKey.create(Registries.ITEM, id);

        Item item = new Item(
                new Item.Properties().setId(itemKey)
        );

        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    public static void initialize() {
        FabricEntryPoint.LOGGER.info("Advanced Hopper items registered.");
    }
}