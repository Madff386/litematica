package fi.dy.masa.litematica.config;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.selection.CornerSelectionMode;
import fi.dy.masa.litematica.selection.SelectionManager;
import fi.dy.masa.litematica.tool.ToolMode;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.PositionUtils.Corner;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.ToolUtils;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;

public class HotkeyCallbackToolActions implements IHotkeyCallback
{
    private final Minecraft mc;

    public HotkeyCallbackToolActions(Minecraft mc)
    {
        this.mc = mc;
    }

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key)
    {
        if (this.mc.player == null || this.mc.world == null)
        {
            return false;
        }

        ToolMode mode = DataManager.getToolMode();
        boolean toolEnabled = Configs.Visuals.ENABLE_RENDERING.getBooleanValue() && Configs.Generic.TOOL_ITEM_ENABLED.getBooleanValue();
        boolean hasTool = EntityUtils.hasToolItem(this.mc.player);
        boolean isToolPrimary = key == Hotkeys.TOOL_PLACE_CORNER_1.getKeybind();
        boolean isToolSecondary = key == Hotkeys.TOOL_PLACE_CORNER_2.getKeybind();
        boolean isToolSelect = key == Hotkeys.TOOL_SELECT_ELEMENTS.getKeybind();

        if (toolEnabled && isToolSelect)
        {
            if (mode.getUsesBlockPrimary() && Hotkeys.TOOL_SELECT_MODIFIER_BLOCK_1.getKeybind().isKeybindHeld())
            {
                ToolUtils.setToolModeBlockState(mode, true, this.mc);
                return true;
            }
            else if (mode.getUsesBlockSecondary() && Hotkeys.TOOL_SELECT_MODIFIER_BLOCK_2.getKeybind().isKeybindHeld())
            {
                ToolUtils.setToolModeBlockState(mode, false, this.mc);
                return true;
            }
        }

        if (toolEnabled && hasTool)
        {
            int maxDistance = 200;
            boolean projectMode =  DataManager.getSchematicProjectsManager().hasProjectOpen();

            if (isToolPrimary || isToolSecondary)
            {
                if (mode.getUsesAreaSelection() || projectMode)
                {
                    SelectionManager sm = DataManager.getSelectionManager();
                    boolean grabModifier = Hotkeys.SELECTION_GRAB_MODIFIER.getKeybind().isKeybindHeld();
                    boolean moveEverything = grabModifier;

                    if (grabModifier && mode == ToolMode.MOVE)
                    {
                        Entity entity = fi.dy.masa.malilib.util.EntityUtils.getCameraEntity();
                        BlockPos pos = RayTraceUtils.getTargetedPosition(this.mc.world, entity, maxDistance, false);

                        if (pos != null)
                        {
                            ToolUtils.moveCurrentlySelectedWorldRegionTo(pos, mc);
                        }
                    }
                    else if (Configs.Generic.SELECTION_CORNERS_MODE.getOptionListValue() == CornerSelectionMode.CORNERS)
                    {
                        Corner corner = isToolPrimary ? Corner.CORNER_1 : Corner.CORNER_2;
                        sm.setPositionOfCurrentSelectionToRayTrace(this.mc, corner, moveEverything, maxDistance);
                    }
                    else if (Configs.Generic.SELECTION_CORNERS_MODE.getOptionListValue() == CornerSelectionMode.EXPAND)
                    {
                        sm.handleCuboidModeMouseClick(this.mc, maxDistance, isToolSecondary, moveEverything);
                    }
                }
                else if (mode.getUsesSchematic())
                {
                    DataManager.getSchematicPlacementManager().setPositionOfCurrentSelectionToRayTrace(this.mc, maxDistance);
                }

                return true;
            }
            else if (isToolSelect)
            {
                Entity entity = fi.dy.masa.malilib.util.EntityUtils.getCameraEntity();

                if (mode.getUsesAreaSelection() || projectMode)
                {
                    SelectionManager sm = DataManager.getSelectionManager();

                    if (Hotkeys.SELECTION_GRAB_MODIFIER.getKeybind().isKeybindHeld())
                    {
                        if (sm.hasGrabbedElement())
                        {
                            sm.releaseGrabbedElement();
                        }
                        else
                        {
                            sm.grabElement(this.mc, maxDistance);
                        }
                    }
                    else
                    {
                        sm.changeSelection(this.mc.world, entity, maxDistance);
                    }
                }
                else if (mode.getUsesSchematic())
                {
                    DataManager.getSchematicPlacementManager().changeSelection(this.mc.world, entity, maxDistance);
                }

                return true;
            }
        }

        return false;
    }
}
