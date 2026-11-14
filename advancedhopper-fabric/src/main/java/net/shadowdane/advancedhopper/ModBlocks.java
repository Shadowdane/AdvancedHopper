package net.shadowdane.advancedhopper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;

import static net.shadowdane.advancedhopper.FabricEntryPoint.LOG_PREFIX;

public class ModBlocks {
        public static final Block ADVANCED_HOPPER = registerBlock("advanced_hopper");

        private static Block registerBlock(String name) {
                // Shared identifier for the block and its inventory item.
                Identifier id = Identifier.fromNamespaceAndPath(
                                FabricEntryPoint.MOD_ID, name);

                ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);

                ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

                // Register new hopper block and copy hopper properties
                Block block = new HopperBlock(
                                BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER)
                                                .setId(blockKey));

                Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

                // Register a new blockitem
                BlockItem blockItem = new BlockItem(
                                block,
                                new Item.Properties()
                                                .setId(itemKey)
                                                .useBlockDescriptionPrefix());

                Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

                return block;
        }

        public static void initialize() {
                // Calling this method triggers the static registration above.
                ((FabricBlockEntityType) BlockEntityTypes.HOPPER).addValidBlock(ADVANCED_HOPPER);

                FabricEntryPoint.LOGGER.info(LOG_PREFIX + "Blocks registered.");
        }
}