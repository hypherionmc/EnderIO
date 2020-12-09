package crazypants.enderio.base.filter.gui;

import com.enderio.core.client.gui.button.ColorButton;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.common.util.DyeColor;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IRedstoneConduit;
import crazypants.enderio.base.filter.IFilter;
import crazypants.enderio.base.filter.redstone.SubtractionOutputSignalFilter;
import crazypants.enderio.base.lang.Lang;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubtractionFilterGui extends AbstractFilterGui {

    private static final int ID_COLOR = FilterGuiUtil.nextButtonId();

    private final @Nonnull SubtractionOutputSignalFilter filter;

    private int xOffset;
    private int yOffset;
    private boolean isInstalled = false;

    private final @Nonnull List<ColorButton> colorButtons;
    // Dummy button to display when the filter is not installed
    private final GuiButton dummyInputB = new GuiButton(ID_COLOR + 2, 0, 0, 16, 16, "?");

    public SubtractionFilterGui(@Nonnull InventoryPlayer playerInv, @Nonnull ContainerFilter filterContainer, TileEntity te, @Nonnull IFilter filterIn, EnumFacing side) {
        super(playerInv, filterContainer, te, filterIn);

        filter = (SubtractionOutputSignalFilter) filterIn;

        // Retrieve the "Output" color of the conduit the filter is installed in
        // This locks the filter "input" color to the output color of the conduit
        if (te instanceof IConduitBundle) {
           IRedstoneConduit conduit = ((IConduitBundle)te).getConduit(IRedstoneConduit.class);
           if (conduit != null && conduit.getSignalFilter(side, true) instanceof SubtractionOutputSignalFilter) {
               filter.setBaseSignalColor(conduit.getOutputSignalColor(side));
               isInstalled = true;
           }
        }

        xOffset = 13;
        yOffset = 34;

        colorButtons = new ArrayList<ColorButton>(2);

        int x = xOffset;
        int y = yOffset;

        dummyInputB.enabled = false;
        dummyInputB.visible = !isInstalled;
        addButton(dummyInputB);

        ColorButton inputB = new ColorButton(this, ID_COLOR, x, y);
        inputB.setToolTipHeading(Lang.GUI_REDSTONE_FILTER_SIGNAL_COLOR.get());
        DyeColor color = filter.getBaseSignalColor();
        inputB.setColorIndex(color.ordinal());
        inputB.setEnabled(false);
        inputB.setIsVisible(isInstalled);
        colorButtons.add(inputB);

        ColorButton subtractColorB = new ColorButton(this, ID_COLOR + 1, x, y + 20);
        subtractColorB.setToolTipHeading(Lang.GUI_REDSTONE_FILTER_SIGNAL_COLOR.get());
        DyeColor subtractColor = filter.getSubtractSignalColor();
        subtractColorB.setColorIndex(subtractColor.ordinal());
        subtractColorB.setEnabled(true);
        colorButtons.add(subtractColorB);
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == ID_COLOR + 1) {
            filter.setSubtractSignalColor(DyeColor.fromIndex(colorButtons.get(1).getColorIndex()));
        }
        sendFilterChange();
    }

    @Override
    public void updateButtons() {
        super.updateButtons();
        for (int i = 0; i < 2; i++) {
            colorButtons.get(i).onGuiInit();
        }
    }

    @Override
    public void renderCustomOptions(int top, float par1, int par2, int par3) {
        FontRenderer fr = getFontRenderer();
        fr.drawString(Lang.GUI_REDSTONE_FILTER_INPUT_SIGNAL.get(), getGuiLeft() + xOffset + 20, getGuiTop() + yOffset + 4,
                ColorUtil.getRGB(Color.darkGray));
        fr.drawString(Lang.GUI_REDSTONE_FILTER_INPUT_SUBTRACT_SIGNAL.get(), getGuiLeft() + xOffset + 20, getGuiTop() + yOffset + 4 + 20,
                ColorUtil.getRGB(Color.darkGray));
        dummyInputB.x = getGuiLeft() + xOffset;
        dummyInputB.y = getGuiTop() + yOffset;
        dummyInputB.drawButton(mc, par2, par3, par1);
        super.renderCustomOptions(top, par1, par2, par3);
    }

    @Nonnull
    @Override
    protected String getUnlocalisedNameForHeading() {
        return Lang.GUI_REDSTONE_FILTER_SUBTRACT.get();
    }

}
