package crazypants.enderio.base.filter.redstone.items;

import com.enderio.core.common.TileEntityBase;
import crazypants.enderio.api.IModObject;
import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.filter.FilterRegistry;
import crazypants.enderio.base.filter.IFilterContainer;
import crazypants.enderio.base.filter.gui.ContainerFilter;
import crazypants.enderio.base.filter.gui.SubtractionFilterGui;
import crazypants.enderio.base.filter.redstone.IOutputSignalFilter;
import crazypants.enderio.base.filter.redstone.SubtractionOutputSignalFilter;
import crazypants.enderio.util.NbtValue;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemSubtractionOutputFilter extends Item implements IItemOutputSignalFilterUpgrade {

    public static ItemSubtractionOutputFilter create(@Nonnull IModObject modObject, @Nullable Block block) {
        return new ItemSubtractionOutputFilter(modObject);
    }

    public ItemSubtractionOutputFilter(@Nonnull IModObject modObject) {
        setCreativeTab(EnderIOTab.tabEnderIOItems);
        modObject.apply(this);
        setHasSubtypes(false);
        setMaxDamage(0);
        setMaxStackSize(64);
    }

    @Override
    public IOutputSignalFilter createFilterFromStack(@Nonnull ItemStack stack) {
        SubtractionOutputSignalFilter filter = new SubtractionOutputSignalFilter();
        if (NbtValue.FILTER.hasTag(stack)) {
            filter.readFromNBT(NbtValue.FILTER.getTag(stack));
        }
        return filter;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public GuiScreen getClientGuiElement(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing facing, int param1) {
        Container container = player.openContainer;
        if (container instanceof IFilterContainer) {
            return new SubtractionFilterGui(player.inventory, new ContainerFilter(player, (TileEntityBase) world.getTileEntity(pos), facing, param1),
                    world.getTileEntity(pos), ((IFilterContainer<SubtractionOutputSignalFilter>) container).getFilter(param1), facing);
        } else {
            return new SubtractionFilterGui(player.inventory, new ContainerFilter(player, null, facing, param1), null,
                    FilterRegistry.getFilterForUpgrade(player.getHeldItem(EnumHand.values()[param1])), facing);
        }
    }
}
