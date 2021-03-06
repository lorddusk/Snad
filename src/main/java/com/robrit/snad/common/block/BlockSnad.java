/*
 * BlockSnad.java
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

package com.robrit.snad.common.block;

import com.robrit.snad.common.item.IMetaBlockSnad;
import com.robrit.snad.common.util.ConfigurationData;

import net.minecraft.block.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
//import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public class BlockSnad extends BlockFalling implements IMetaBlockSnad {

  public static final PropertyEnum<BlockSnad.EnumType>
      VARIANT =
      PropertyEnum.<BlockSnad.EnumType>create("variant", BlockSnad.EnumType.class);

  public BlockSnad() {
    super(Material.sand);
    this.setTickRandomly(true);
    this.setHardness(0.5F);
    this.setStepSound(net.minecraft.block.Block.soundTypeSand);
    this.setCreativeTab(CreativeTabs.tabMisc);
    this.setUnlocalizedName("snad");
    this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.SAND));
  }

  @Override
  public int damageDropped(IBlockState state) {
    return getMetaFromState(state);
  }

  @Override
  public MapColor getMapColor(IBlockState state) {
    return ((BlockSnad.EnumType) state.getValue(VARIANT)).getMapColor();
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return this.getDefaultState()
        .withProperty(VARIANT, meta == 0 ? EnumType.SAND : EnumType.RED_SAND);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return ((BlockSnad.EnumType) state.getValue(VARIANT)).getMetadata();
  }

  @Override
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[]{VARIANT});
  }

  @Override
  public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
    for (BlockSnad.EnumType blocksand$enumtype : BlockSnad.EnumType.values()) {
      list.add(new ItemStack(itemIn, 1, blocksand$enumtype.getMetadata()));
    }
  }

  @Override
  public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos,
                                EntityPlayer player) {
    return new ItemStack(Item.getItemFromBlock(this), 1,
                         this.getMetaFromState(world.getBlockState(pos)));
  }

  @Override
  public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
    if (!world.isRemote) {
      this.checkFallable(world, pos);
    }

    net.minecraft.block.Block blockAbove = world.getBlockState(pos.up()).getBlock();

    if (blockAbove == null) {
      return;
    }

    if (blockAbove instanceof BlockReed || blockAbove instanceof BlockCactus) {
      boolean isSameBlockType = true;
      int height = 1;

      while (isSameBlockType) {
        if (world.getBlockState(pos.up(height)).getBlock() != null) {
          net.minecraft.block.Block nextPlantBlock = world.getBlockState(pos.up(height)).getBlock();
          if (nextPlantBlock.getClass() == blockAbove.getClass()) {
            for (int growthAttempts = 0; growthAttempts < ConfigurationData.SPEED_INCREASE_VALUE;
                 growthAttempts++) {
              nextPlantBlock
                  .updateTick(world, pos.up(height), world.getBlockState(pos.up(height)), rand);
            }
            height++;
          } else {
            isSameBlockType = false;
          }
        } else {
          isSameBlockType = false;
        }
      }
    } else if (blockAbove instanceof IPlantable) {
      blockAbove.updateTick(world, pos.up(), world.getBlockState(pos.up()), rand);
    }

  }

  private void checkFallable(World worldIn, BlockPos pos) {
    if (canFallInto(worldIn, pos.down()) && pos.getY() >= 0) {
      int i = 32;

      if (!fallInstantly && worldIn.isAreaLoaded(pos.add(-i, -i, -i), pos.add(i, i, i))) {
        if (!worldIn.isRemote) {
          EntityFallingBlock
              entityfallingblock =
              new EntityFallingBlock(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY(),
                                     (double) pos.getZ() + 0.5D, worldIn.getBlockState(pos));
          this.onStartFalling(entityfallingblock);
          worldIn.spawnEntityInWorld(entityfallingblock);
        }
      } else {
        worldIn.setBlockToAir(pos);
        BlockPos blockpos;

        for (blockpos = pos.down(); canFallInto(worldIn, blockpos) && blockpos.getY() > 0;
             blockpos = blockpos.down()) {
          ;
        }

        if (blockpos.getY() > 0) {
          worldIn.setBlockState(blockpos.up(), this.getDefaultState());
        }
      }
    }
  }

  @Override
  public boolean canSustainPlant(IBlockAccess world, BlockPos pos, EnumFacing direction,
                                 IPlantable plantable) {
    net.minecraft.block.Block
        plant =
        plantable.getPlant(world, new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ())).getBlock();
    EnumPlantType
        plantType =
        plantable.getPlantType(world, new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));

    switch (plantType) {
      case Desert: {
        return true;
      }
      case Water: {
        return world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock()
                   .getMaterial() == Material.water
               && world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ()))
                  == getDefaultState();
      }
      case Beach: {
        return (world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ())).getBlock()
                    .getMaterial() == Material.water ||
                world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ())).getBlock()
                    .getMaterial() == Material.water ||
                world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1)).getBlock()
                    .getMaterial() == Material.water ||
                world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1)).getBlock()
                    .getMaterial() == Material.water);
      }
    }

    return false;
  }

  @Override
  public String getSpecialName(ItemStack stack) {
    return stack.getItemDamage() == 0 ? "default" : "red";
  }

  public static enum EnumType implements IStringSerializable {
    SAND(0, "snad", "default", MapColor.sandColor),
    RED_SAND(1, "red_snad", "red", MapColor.adobeColor);

    private static final BlockSnad.EnumType[] META_LOOKUP = new BlockSnad.EnumType[values().length];
    private final int meta;
    private final String name;
    private final MapColor mapColor;
    private final String unlocalizedName;

    private EnumType(int meta, String name, String unlocalizedName, MapColor mapColor) {
      this.meta = meta;
      this.name = name;
      this.mapColor = mapColor;
      this.unlocalizedName = unlocalizedName;
    }

    public int getMetadata() {
      return this.meta;
    }

    public String toString() {
      return this.name;
    }

    public MapColor getMapColor() {
      return this.mapColor;
    }

    public static BlockSnad.EnumType byMetadata(int meta) {
      if (meta < 0 || meta >= META_LOOKUP.length) {
        meta = 0;
      }

      return META_LOOKUP[meta];
    }

    public String getName() {
      return this.name;
    }

    public String getUnlocalizedName() {
      return this.unlocalizedName;
    }

    static {
      for (BlockSnad.EnumType blocksand$enumtype : values()) {
        META_LOOKUP[blocksand$enumtype.getMetadata()] = blocksand$enumtype;
      }
    }
  }
}
