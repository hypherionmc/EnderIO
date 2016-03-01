package crazypants.enderio.machine.tank;

import java.util.List;

import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.client.handlers.SpecialTooltipHandler;

import crazypants.enderio.EnderIO;
import crazypants.enderio.GuiHandler;
import crazypants.enderio.ModObject;
import crazypants.enderio.machine.AbstractMachineBlock;
import crazypants.enderio.machine.MachineRenderMapper;
import crazypants.enderio.machine.power.PowerDisplayUtil;
import crazypants.enderio.machine.soul.SoulBinderRenderMapper;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.render.IRenderMapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTank extends AbstractMachineBlock<TileTank> implements IAdvancedTooltipProvider {

  @SideOnly(Side.CLIENT)
  private static MachineRenderMapper RENDER_MAPPER;
  
  public static BlockTank create() {
    PacketHandler.INSTANCE.registerMessage(PacketTankFluid.class, PacketTankFluid.class, PacketHandler.nextID(), Side.CLIENT);
    PacketHandler.INSTANCE.registerMessage(PacketTankVoidMode.class, PacketTankVoidMode.class, PacketHandler.nextID(), Side.SERVER);
    BlockTank res = new BlockTank();
    res.init();
    return res;
  }

  protected BlockTank() {
    super(ModObject.blockTank, TileTank.class, BlockItemTank.class);
    setStepSound(Block.soundTypeGlass);
    setLightOpacity(0);
  }
  
  @Override
  public int damageDropped(IBlockState st) {
    return getMetaFromState(st);
  }

  @Override
  public TileEntity createTileEntity(World world, IBlockState bs) {
    return new TileTank(getMetaFromState(bs));
  }
  
  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    return true;
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(!(te instanceof TileTank)) {
      return null;
    }
    return new ContainerTank(player.inventory, (TileTank) te);
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(!(te instanceof TileTank)) {
      return null;
    }
    return new GuiTank(player.inventory, (TileTank) te);
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  protected int getGuiId() {
    return GuiHandler.GUI_ID_TANK;
  }

  @Override
  public int getLightValue(IBlockAccess world, BlockPos pos) {
    TileEntity tank = world.getTileEntity(pos);
    if(tank instanceof TileTank) {
      FluidStack stack = ((TileTank) tank).tank.getFluid();
      return stack == null || stack.amount <= 0 ? 0 : stack.getFluid().getLuminosity(stack);
    }
    return super.getLightValue(world, pos);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addCommonEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
  }

  @Override
  public float getExplosionResistance(World world, BlockPos pos, Entity par1Entity, Explosion explosion) {
    IBlockState bs = world.getBlockState(pos);
    int meta = getMetaFromState(bs);
    meta = MathHelper.clamp_int(meta, 0, 1);
    if(meta == 1) {
      return 2000;
    } else {
      return super.getExplosionResistance(par1Entity);
    }
  }

  @Override
  public boolean hasComparatorInputOverride() {
    return true;
  }

  @Override
  public int getComparatorInputOverride(World w, BlockPos pos) {
    TileEntity te = w.getTileEntity(pos);
    if (te instanceof TileTank) {
      return ((TileTank) te).getComparatorOutput();
    }
    return 0;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addBasicEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
    if(itemstack.getTagCompound()!= null && itemstack.getTagCompound().hasKey("tankContents")) {
      FluidStack fl = FluidStack.loadFluidStackFromNBT((NBTTagCompound) itemstack.getTagCompound().getTag("tankContents"));
      if(fl != null && fl.getFluid() != null) {
        String str = fl.amount + " " + EnderIO.lang.localize("fluid.millibucket.abr") + " " + PowerDisplayUtil.ofStr() + " " + fl.getFluid().getName();
        list.add(str);
      }
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addDetailedEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
    SpecialTooltipHandler.addDetailedTooltipFromResources(list, itemstack);
    if(itemstack.getItemDamage() == 1) {
      list.add(EnumChatFormatting.ITALIC + EnderIO.lang.localize("blastResistant"));
    }
  }

  @Override
  public String getUnlocalizedNameForTooltip(ItemStack stack) {
    return stack.getUnlocalizedName();
  }

  @Override
  public void getWailaInfo(List<String> tooltip, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if (te instanceof TileTank) {
      TileTank tank = (TileTank) te;
      FluidStack stored = tank.tank.getFluid();
      String fluid = stored == null ? EnderIO.lang.localize("tooltip.none") : stored.getFluid().getLocalizedName(stored);
      int amount = stored == null ? 0 : stored.amount;

      tooltip.add(String.format("%s%s : %s (%d %s)", EnumChatFormatting.WHITE, EnderIO.lang.localize("tooltip.fluidStored"), fluid, amount, EnderIO.lang.localize("fluid.millibucket.abr")));
    }
  }
  
  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper getRenderMapper(IBlockState state, IBlockAccess world, BlockPos pos) {
    if (RENDER_MAPPER == null) {
      RENDER_MAPPER = new SoulBinderRenderMapper();
    }
    return RENDER_MAPPER;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper getRenderMapper(ItemStack stack) {
    if (RENDER_MAPPER == null) {
      RENDER_MAPPER = new SoulBinderRenderMapper();
    }
    return RENDER_MAPPER;
  }
}
