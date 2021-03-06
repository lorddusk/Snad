/*
 * CommonProxy.java
 *
 * Copyright (c) 2016 TheRoBrit
 *
 * SNAD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SNAD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robrit.snad.common.proxy;

import com.robrit.snad.common.block.BlockSnad;

import com.robrit.snad.common.item.ItemBlockSnadMeta;
import com.robrit.snad.common.util.ModInformation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class CommonProxy implements IProxy {

  public static final BlockSnad blockSnad = new BlockSnad();

  @Override
  public void registerBlocks() {
    GameRegistry.registerBlock(blockSnad, ItemBlockSnadMeta.class, "snad");
  }

  @Override
  public void registerRecipes() {
    GameRegistry.addRecipe(new ItemStack(blockSnad, 1, BlockSnad.EnumType.SAND.getMetadata()),
                           new Object[]{"S", "S", 'S', new ItemStack(Blocks.sand, 1,
                                                                     BlockSand.EnumType.SAND
                                                                         .getMetadata())});

    GameRegistry.addRecipe(new ItemStack(blockSnad, 1, BlockSnad.EnumType.RED_SAND.getMetadata()),
                           new Object[]{"S", "S", 'S', new ItemStack(Blocks.sand, 1,
                                                                     BlockSand.EnumType.RED_SAND
                                                                         .getMetadata())});
  }

  @Override
  public void registerBlockModels() {
    ModelLoader
        .registerItemVariants(Item.getItemFromBlock(blockSnad), new ResourceLocation("snad:snad"),
                              new ResourceLocation("snad:red_snad"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockSnad), 0,
                                               new ModelResourceLocation(
                                                   ModInformation.MOD_ID + ":" + blockSnad
                                                       .getUnlocalizedName().substring(5),
                                                   "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockSnad), 1,
                                               new ModelResourceLocation(
                                                   ModInformation.MOD_ID + ":red_" + blockSnad
                                                       .getUnlocalizedName().substring(5),
                                                   "inventory"));
  }
}
