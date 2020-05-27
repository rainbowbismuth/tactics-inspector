package rainbowbismuth.fft.gui;

import imgui.ImGui;
import imgui.ImInt;
import imgui.enums.*;
import rainbowbismuth.fft.PSMemory;
import rainbowbismuth.fft.TacticsInspector;
import rainbowbismuth.fft.WindowsMemoryViewer;
import rainbowbismuth.fft.enums.Status;
import rainbowbismuth.fft.view.StatusArray;
import rainbowbismuth.fft.view.StatusCTArray;
import rainbowbismuth.fft.view.UnitData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InspectorUI {
    private final PSMemory psMemory;
    private final TacticsInspector inspector;
    private final Map<Long, ImInt> inputInts = new HashMap<>();
    private final List<UnitData.Field> statusFields = List.of(UnitData.Field.MOVE, UnitData.Field.JUMP, UnitData.Field.SP);

    public InspectorUI() throws Exception {
        psMemory = WindowsMemoryViewer.createEPSXE15Viewer(null, "ePSXe - Enhanced PSX emulator");
        inspector = new TacticsInspector();
    }

    void init() {
        ImGui.styleColorsLight();
        ImGui.getStyle().setFrameRounding(6.0f);
    }

    void render(final int winWidth, final int winHeight) throws Exception {
        psMemory.read(inspector.getRAM());
        ImGui.setNextWindowSize(winWidth, winHeight);
        ImGui.setNextWindowPos(0.0f, 0.0f);
        renderUnitSelectionWindow();
        inspector.flush(psMemory);
    }

    void renderUnitSelectionWindow() throws Exception {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.begin("Unit Data Viewer",
                ImGuiWindowFlags.NoCollapse
                        | ImGuiWindowFlags.NoMove
                        | ImGuiWindowFlags.NoTitleBar
                        | ImGuiWindowFlags.NoDecoration);
        ImGui.popStyleVar();
        if (ImGui.beginTabBar("Units")) {
            final List<UnitData> unitData = inspector.getUnitData();
            for (int i = 0; i < UnitData.NUM; i++) {
                final UnitData unit = unitData.get(i);
//                final MiscUnitData misc = miscUnitData.get(i);
                if (unit.isInvalid()) {
                    continue;
                }
                if (ImGui.beginTabItem(String.format("%s##%d", unit.getName(), unit.getIndex()))) {
                    renderUnitTab(unit); //, misc);
                    ImGui.endTabItem();
                }
            }
            ImGui.endTabBar();
        }
        ImGui.end();
    }

//    private void renderMisc(final MiscUnitData misc) throws PSMemoryWriteException {
//        ImGui.text(String.format("Previous 0x%08X", misc.getPreviousPtr()));
//        ImGui.text(String.format("ID %d", misc.getId()));
//        ImGui.text(String.format("Unit Data Ptr 0x%08X", misc.getUnitDataPtr()));
//
//        ImGui.setNextItemWidth(150.0f);
//        final Integer newVRAMSlot = inputInt("Spritesheet VRAM Slot", misc.getSpritesheetVramSlot(), 1);
//        if (newVRAMSlot != null) {
//            misc.setSpritesheetVramSlot(inspector, newVRAMSlot);
//        }
//        ImGui.setNextItemWidth(150.0f);
//        final Integer newSpriteSheetID = inputInt("Spritesheet ID", misc.getSpritesheetId(), 1);
//        if (newSpriteSheetID != null) {
//            misc.setSpritesheetId(inspector, newSpriteSheetID);
//        }
//        ImGui.setNextItemWidth(150.0f);
//        final Integer newStoredPalette = inputInt("Stored Palette", misc.getStoredPalette(), 1);
//        if (newStoredPalette != null) {
//            misc.setStoredPalette(inspector, newStoredPalette);
//        }
//        ImGui.setNextItemWidth(300);
//        final Integer newVRAMSpritesheetID = inputInt("VRAM Spritesheet ID", misc.getVramSpritesheetId(), 1);
//        if (newVRAMSpritesheetID != null) {
//            misc.setVramSpritesheetId(inspector, newVRAMSpritesheetID);
//        }
//        ImGui.setNextItemWidth(300.0f);
//        final Integer newVRAMPaletteID = inputInt("VRAM Palette ID", misc.getVramPaletteId(), 1);
//        if (newVRAMPaletteID != null) {
//            misc.setVramPaletteId(inspector, newVRAMPaletteID);
//        }
//        ImGui.setNextItemWidth(150.0f);
//        final Integer newModifiedPalette = inputInt("Modified Palette", misc.getModifiedPalette(), 1);
//        if (newModifiedPalette != null) {
//            misc.setModifiedPalette(inspector, newModifiedPalette);
//        }
//    }

    void renderVitalBar(final String name, final int min, final int max, final float red, final float green, final float blue) {
        ImGui.text(name);
        ImGui.sameLine();
        ImGui.pushStyleColor(ImGuiCol.PlotHistogram, red, green, blue, 1.0f);
        final float fraction = (float) Math.min(min / (float) max, 1.0);
        ImGui.progressBar(fraction, 250.0f, ImGui.getTextLineHeight(), "");
        ImGui.popStyleColor();
        ImGui.sameLine();
        ImGui.text(String.format("%03d / %03d", min, max));
    }

    //    void renderUnitTab(final BattleUnit unit, final MiscUnitData misc) throws Exception {
    void renderUnitTab(final UnitData unit) throws Exception {
        if (ImGui.collapsingHeader("Vitals", ImGuiTreeNodeFlags.DefaultOpen)) {
            renderVitals(unit);
        }
        if (ImGui.collapsingHeader("Status")) {
            renderStatus(unit);
        }
        if (ImGui.collapsingHeader("Conditions")) {
            renderConditions(unit);
        }
        if (ImGui.collapsingHeader("Attack")) {
            renderAttack(unit);
        }
//        if (ImGui.collapsingHeader("Misc")) {
//            renderMisc(misc);
//        }
    }

    private Integer inputInt(String label, int value, int step) {
        final ImInt imInt = inputInts.computeIfAbsent(
                ImGui.getID(String.format("ImInt-%s", label)),
                key -> new ImInt(value));
        if (value != imInt.get()) {
            imInt.set(value);
        }
        if (ImGui.inputInt(label, imInt, step, step, ImGuiInputTextFlags.EnterReturnsTrue)) {
            return imInt.get();
        }
        return null;
    }

    private void renderStatus(final UnitData unit) {
        unitDataFieldControl(unit, UnitData.Field.X_COORD);
        unitDataFieldControl(unit, UnitData.Field.Y_COORD);

        unitDataFieldControl(unit, UnitData.Field.BRAVE);
        ImGui.sameLine();
        unitDataFieldControl(unit, UnitData.Field.FAITH);

        for (final UnitData.Field field : statusFields) {
            unitDataFieldControl(unit, field);
        }
    }

    private void unitDataFieldControl(UnitData unit, UnitData.Field field) {
        ImGui.setNextItemWidth(150.0f);
        final Integer newVal = inputInt(field.getDisplayName(), (int) unit.read(field), 1);
        if (newVal != null) {
            unit.write(field, newVal);
        }
    }

    private void renderAttack(final UnitData unit) {
//        ImGui.columns(2);
//        for (int i = 0; i < AttackTypeFlag.VALUES.length; i++) {
//            if (i != 0 && i % 4 == 0) ImGui.nextColumn();
//            final AttackTypeFlag flag = AttackTypeFlag.VALUES[i];
//            if (ImGui.checkbox(flag.getDisplayName(), unit.hasAttackTypeFlag(flag))) {
//                unit.setAttackTypeFlag(inspector, flag, !unit.hasAttackTypeFlag(flag));
//            }
//        }
//        ImGui.columns();

        if (ImGui.beginTabBar("Attack Tab Bar")) {
            if (ImGui.beginTabItem("Add")) {
                renderStatusArray(unit.getAttackAddStatus());
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Remove")) {
                renderStatusArray(unit.getAttackRemoveStatus());
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Inflicted")) {
                renderStatusArray(unit.getAttackInflictedStatus());
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
    }

    private void renderConditions(final UnitData unit) {
        if (ImGui.beginTabBar("Statuses Tab Bar")) {
            if (ImGui.beginTabItem("Status")) {
                renderStatusArray(unit.getStatus());
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Auto")) {
                renderStatusArray(unit.getAutoStatus());
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Immunity")) {
                renderStatusArray(unit.getStatusImmunity());
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("CT")) {
                renderStatusCTPage(unit.getStatus(), unit.getStatusCT());
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
    }

    private void renderVitals(final UnitData unit) {
        final int hp = (int) unit.read(UnitData.Field.HP);
        final int maxHp = (int) unit.read(UnitData.Field.MAX_HP);
        final int mp = (int) unit.read(UnitData.Field.MP);
        final int maxMp = (int) unit.read(UnitData.Field.MAX_MP);
        final int level = (int) unit.read(UnitData.Field.LEVEL);
        final int experience = (int) unit.read(UnitData.Field.EXPERIENCE);
        final int ct = (int) unit.read(UnitData.Field.CT);
        final int brave = (int) unit.read(UnitData.Field.BRAVE);
        final int faith = (int) unit.read(UnitData.Field.FAITH);
        final int x = (int) unit.read(UnitData.Field.X_COORD);
        final int y = (int) unit.read(UnitData.Field.Y_COORD);

        renderVitalBar("HP", hp, maxHp, (0x56 / 255.0f), (0x73 / 255.0f), (0x77 / 255.0f));
        ImGui.sameLine();
        ImGui.text(String.format("%s Lv.%02d Exp.%02d", unit.getName(), level, experience));
        renderVitalBar("MP", mp, maxMp, (0x8C / 255.0f), (0x4C / 255.0f), (0x3B / 255.0f));
        ImGui.sameLine();
        ImGui.text(unit.getJobName());
        renderVitalBar("CT", ct, 100, (0x5B / 255.0f), (0x70 / 255.0f), (0x40 / 255.0f));
        ImGui.sameLine();
        ImGui.text(String.format("Brave %2d Faith %2d", brave, faith));

        ImGui.text(String.format("Coordinates (%d, %d), facing %s, on %s.",
                x, y, unit.getFacing(),
                unit.isOnHigherElevation() ? "high elevation" : "low elevation"));
    }

    private void renderStatusArray(final StatusArray statusArray) {
        ImGui.columns(4);
        for (int j = 0; j < Status.VALUES.length; j++) {
            final Status status = Status.VALUES[j];
            if (j != 0 && j % 10 == 0) ImGui.nextColumn();
            if (ImGui.checkbox(status.getDisplayName(), statusArray.hasStatus(status))) {
                statusArray.setStatus(status, !statusArray.hasStatus(status));
            }
        }
        ImGui.columns();
    }

    private void renderStatusCTPage(final StatusArray statusArray, final StatusCTArray statusCT) {
        ImGui.columns(2);
        for (int j = 0; j < Status.VALUES.length; j++) {
            final Status status = Status.VALUES[j];
            if (status.getCTOffset() == null) {
                continue;
            }
            if (j != 0 && j % 10 == 0) ImGui.nextColumn();

            ImGui.setNextItemWidth(150.0f);
            final Integer newCT = inputInt(String.format("##%s-CT", status.name()), statusCT.getCT(status), 1);
            if (newCT != null) {
                statusCT.setCT(status, newCT);
            }
            ImGui.sameLine();
            if (ImGui.checkbox(status.getDisplayName(), statusArray.hasStatus(status))) {
                statusArray.setStatus(status, !statusArray.hasStatus(status));
            }
        }
        ImGui.columns();
    }
}