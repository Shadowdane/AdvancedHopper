package net.shadowdane.advancedhopper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

public class ModItems {
    public static final Item UPGRADE_BASE = registerItem("upgrade_base");
    public static final Item STACK_UPGRADE_1 = registerItem("stack_upgrade_1");
    public static final Item STACK_UPGRADE_2 = registerItem("stack_upgrade_2");
    public static final Item STACK_UPGRADE_3 = registerItem("stack_upgrade_3");
    public static final Item STACK_UPGRADE_4 = registerItem("stack_upgrade_4");
    public static final Item STACK_UPGRADE_5 = registerItem("stack_upgrade_5");

    private static Item registerItem(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(FabricEntryPoint.MOD_ID, name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item item = new Item(new Item.Properties().setId(itemKey));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

public static void initialize() {
        ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(FabricEntryPoint.MOD_ID, "main")
        );

        CreativeModeTab tab = FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.advancedhopper.main"))
                .icon(() -> new ItemStack(ModBlocks.ADVANCED_HOPPER.asItem()))
                .displayItems((parameters, output) -> {
                    output.accept(ModBlocks.ADVANCED_HOPPER.asItem());
                    output.accept(UPGRADE_BASE);
                    output.accept(STACK_UPGRADE_1);
                    output.accept(STACK_UPGRADE_2);
                    output.accept(STACK_UPGRADE_3);
                    output.accept(STACK_UPGRADE_4);
                    output.accept(STACK_UPGRADE_5);
                })
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey, tab);
        FabricEntryPoint.LOGGER.info("Advanced Hopper items registered.");
    }
}