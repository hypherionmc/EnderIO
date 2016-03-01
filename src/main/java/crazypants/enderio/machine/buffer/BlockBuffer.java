package crazypants.enderio.machine.buffer;

import crazypants.enderio.EnderIO;
import crazypants.enderio.GuiHandler;
import crazypants.enderio.ModObject;
import crazypants.enderio.machine.AbstractMachineBlock;
import crazypants.enderio.machine.MachineRecipeInput;
import crazypants.enderio.machine.MachineRecipeRegistry;
import crazypants.enderio.machine.painter.BasicPainterTemplate;
import crazypants.enderio.machine.painter.PainterUtil;
import crazypants.enderio.network.PacketHandler;
import crazypants.util.IFacade;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class BlockBuffer extends AbstractMachineBlock<TileBuffer> implements IFacade {

  public static BlockBuffer create() {
    PacketHandler.INSTANCE.registerMessage(PacketBufferIO.class, PacketBufferIO.class, PacketHandler.nextID(), Side.SERVER);
    BlockBuffer res = new BlockBuffer();
    res.init();
    return res;
  }

//  private static final String[] textureNames = new String[] { "blockBufferItem", "blockBufferPower", "blockBufferOmni", "blockBufferCreative" };
  

  private BlockBuffer() {
    super(ModObject.blockBuffer, TileBuffer.class, BlockItemBuffer.class);
  }

  @Override
  protected void init() {
    super.init();
    MachineRecipeRegistry.instance.registerRecipe(ModObject.blockPainter.unlocalisedName, new PainterTemplate());
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof TileBuffer) {
      return new ContainerBuffer(player.inventory, (TileBuffer) te);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof TileBuffer) {
      return new GuiBuffer(player.inventory, (TileBuffer) te);
    }
    return null;
  }

  @Override
  protected int getGuiId() {
    return GuiHandler.GUI_ID_BUFFER;
  }

//  @Override
//  @SideOnly(Side.CLIENT)
//  public IIcon getIcon(IBlockAccess world, int x, int y, int z, int blockSide) {
//    TileEntity te = world.getTileEntity(x, y, z);
//    if(te instanceof TileBuffer) {
//      TileBuffer tef = (TileBuffer) te;
//      if(tef.getSourceBlock() != null) {
//        return tef.getSourceBlock().getIcon(blockSide, tef.getSourceBlockMetadata());
//      } else if(blockSide > 1) {
//        return textures[world.getBlockMetadata(x, y, z)];
//      }
//    }
//    return super.getIcon(world, x, y, z, blockSide);
//  }

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
    if(entity instanceof EntityPlayer) {
      TileEntity te = world.getTileEntity(pos);
      if(te instanceof TileBuffer) {
        TileBuffer ta = (TileBuffer) te;
        if(stack.getTagCompound() != null) {
          ta.readCommon(stack.getTagCompound());
        }
        world.markBlockForUpdate(pos);
      }
    }
  }

  @Override
  public int damageDropped(IBlockState bs) {
    return getMetaFromState(bs);
  }

  public ItemStack createItemStackForSourceBlock(ItemStack machine, Block block, int sourceMeta) {
    PainterUtil.setSourceBlock(machine, block, sourceMeta);
    return machine;
  }

  public final class PainterTemplate extends BasicPainterTemplate {

    public PainterTemplate() {
      super(BlockBuffer.this);
    }

    @Override
    public ResultStack[] getCompletedResult(float chance, MachineRecipeInput... inputs) {
      ItemStack paintSource = MachineRecipeInput.getInputForSlot(1, inputs);
      if(paintSource == null) {
        return new ResultStack[0];
      }
      ItemStack target = MachineRecipeInput.getInputForSlot(0, inputs);
      target = target.copy();
      target.stackSize = 1;
      return new ResultStack[] { new ResultStack(createItemStackForSourceBlock(target, Block.getBlockFromItem(paintSource.getItem()),
          paintSource.getItemDamage())) };
    }
  }

  @Override
  public IBlockState getFacade(IBlockAccess world, BlockPos pos, EnumFacing side) {
    TileBuffer te = getTileEntity(world, pos);
    if(te == null){ 
      return null;
    }
    return te.getSourceBlock();
  }

}
