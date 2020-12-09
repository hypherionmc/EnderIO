package crazypants.enderio.base.filter.redstone;

import com.enderio.core.common.util.DyeColor;
import crazypants.enderio.base.conduit.redstone.signals.BundledSignal;
import crazypants.enderio.base.conduit.redstone.signals.CombinedSignal;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.Random;

public class SubtractionOutputSignalFilter implements IOutputSignalFilter {

    private @Nonnull DyeColor baseSignalColor = DyeColor.BLACK;
    private @Nonnull DyeColor subtractSignalColor = DyeColor.RED;


    @Nonnull
    @Override
    public CombinedSignal apply(@Nonnull DyeColor color, @Nonnull BundledSignal bundledSignal) {
        baseSignalColor = color;
        int subtractedStrength = bundledSignal.getSignal(color).getStrength() - bundledSignal.getSignal(subtractSignalColor).getStrength();
        return new CombinedSignal(Math.max(subtractedStrength, 0));
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbtRoot) {
        baseSignalColor = DyeColor.fromIndex(nbtRoot.getInteger("inputColor"));
        subtractSignalColor = DyeColor.fromIndex(nbtRoot.getInteger("subtractColor"));
    }

    @Override
    public void writeToNBT(@Nonnull NBTTagCompound nbtRoot) {
        nbtRoot.setInteger("inputColor", baseSignalColor.ordinal());
        nbtRoot.setInteger("subtractColor", subtractSignalColor.ordinal());
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    public DyeColor getSubtractSignalColor() {
        return subtractSignalColor;
    }

    public void setSubtractSignalColor(@Nonnull DyeColor color) {
        this.subtractSignalColor = color;
    }

    @Nonnull
    public DyeColor getBaseSignalColor() {
        return baseSignalColor;
    }

    public void setBaseSignalColor(@Nonnull DyeColor baseSignalColor) {
        this.baseSignalColor = baseSignalColor;
    }
}
