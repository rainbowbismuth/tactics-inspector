package rainbowbismuth.fft.gui;

import imgui.ImGui;
import imgui.ImInt;
import imgui.enums.*;
import rainbowbismuth.fft.*;
import rainbowbismuth.fft.enums.Status;
import rainbowbismuth.fft.view.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InspectorUI {
    private final PSMemory psMemory;
    private final TacticsInspector inspector;
    private final Map<Long, ImInt> inputInts = new HashMap<>();
    private final List<MiscUnitData.Field> miscModifyFields = List.of(
            MiscUnitData.Field.PORTRAIT_VRAM_SLOT,
            MiscUnitData.Field.PORTRAIT_SPRITESHEET_ID,
            MiscUnitData.Field.STORED_PALETTE,
            MiscUnitData.Field.UNIT_SPRITESHEET_ID,
            MiscUnitData.Field.UNIT_PALETTE,
            MiscUnitData.Field.MODIFIED_PALETTE);
    private final ImInt memViewerBaseAddr = new ImInt(1700852);
    private final ImInt memViewerPeekAddr = new ImInt(1700852);
    private final List<String> abilityNameTable;
    private final List<String> skillSetNameTable;

    public InspectorUI() throws Exception {
        psMemory = WindowsMemoryViewer.createEPSXE15Viewer(null, "ePSXe - Enhanced PSX emulator");
        inspector = new TacticsInspector();
        psMemory.read(inspector.getRAM());
        abilityNameTable = inspector.readAbilityNameTable();
        skillSetNameTable = inspector.readSkillSetNameTable();
    }

    void init() {
        ImGui.styleColorsLight();
        ImGui.getStyle().setFrameRounding(6.0f);
    }

    void render(final int winWidth, final int winHeight) throws Exception {
        psMemory.read(inspector.getRAM());
        ImGui.setNextWindowSize(winWidth, winHeight);
        ImGui.setNextWindowPos(0.0f, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.begin("Unit Data Viewer",
                ImGuiWindowFlags.NoCollapse
                        | ImGuiWindowFlags.NoMove
                        | ImGuiWindowFlags.NoTitleBar
                        | ImGuiWindowFlags.NoDecoration);
        ImGui.popStyleVar();
        if (ImGui.beginTabBar("Main Tools")) {
            if (ImGui.beginTabItem("Unit Viewer")) {
                renderUnitSelectionWindow();
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("AI Viewer")) {
                renderAIViewer();
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Memory Viewer")) {
                renderMemoryViewer();
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
        ImGui.end();
        inspector.flush(psMemory);
    }

    void renderMemoryViewer() {
        final byte[] ram = inspector.getRAM();
        ImGui.text(String.format("0x%08x", memViewerBaseAddr.get()));
        ImGui.setNextItemWidth(300.0f);
        ImGui.sameLine();
        ImGui.inputInt("Base Address", memViewerBaseAddr, 0x10, 0x100);
        ImGui.text(String.format("0x%08x", memViewerPeekAddr.get()));
        ImGui.setNextItemWidth(300.0f);
        ImGui.sameLine();
        ImGui.inputInt("Peek Address", memViewerPeekAddr, 0x1, 0x10);

        final int peekData = Byte.toUnsignedInt(ram[memViewerPeekAddr.get()]);
        ImGui.text(String.format("      0x%02x", peekData));
        ImGui.sameLine();
        ImGui.setNextItemWidth(300.0f);
        final Integer newPeekVal = inputInt("Poke", peekData, 1);
        if (newPeekVal != null) {
            inspector.write(memViewerPeekAddr.get(), newPeekVal, WordSize.BYTE);
        }

        final int baseAddress = memViewerBaseAddr.get();
        for (int i = 0; i < 0x18; i++) {
            final int rowAddress = baseAddress + (i * 0x10);
            ImGui.text(String.format("0x%08x ", 0x8000_0000L + rowAddress));
            for (int j = 0; j < 0x10; j++) {
                ImGui.sameLine();
                final int byteAddr = rowAddress + j;
                final int data = Byte.toUnsignedInt(ram[byteAddr]);
                if (byteAddr == memViewerPeekAddr.get()) {
                    ImGui.textColored(0.9f, 0.1f, 0.1f, 1.0f, String.format("%02x", data));
                } else if (data == 0) {
                    ImGui.textDisabled("00");
                } else {
                    ImGui.text(String.format("%02x", data));
                }
                if (ImGui.isItemClicked()) {
                    memViewerPeekAddr.set(byteAddr);
                }
            }

            final StringBuilder stringBuilder = new StringBuilder(0x12);
            stringBuilder.append(' ');
            for (int j = 0; j < 0x10; j++) {
                final int byteAddr = rowAddress + j;
                final int data = Byte.toUnsignedInt(ram[byteAddr]);
                final char character = CharacterSet.INSTANCE.get(data);
                if (character == CharacterSet.UNKNOWN) {
                    stringBuilder.append(' ');
                } else {
                    stringBuilder.append(character);
                }
            }
            ImGui.sameLine();
            ImGui.textDisabled(stringBuilder.toString());
        }
    }

    void renderAIViewer() {
        final List<UnitData> units = inspector.getUnitData();
        final List<ConsideredActionData> actions = inspector.getConsideredActionData();

//        final Optional<UnitData> actor = units.stream().filter(unit -> unit.isValid() && unit.isTakingTurn()).findFirst();
//        if (!actor.isPresent()) {
//            return;
//        }
        for (final ConsideredActionData action : actions) {
            final int hitChance = (int) action.read(ConsideredActionData.Field.HIT_CHANCE);
            final int targetPriority = (int) action.read(ConsideredActionData.Field.TARGET_PRIORITY);
            final int casterUnitId = (int) action.read(ConsideredActionData.Field.CASTER_UNIT_ID);
            final int skillSetId = (int) action.read(ConsideredActionData.Field.SKILLSET_ID);
            final int abilityId = (int) action.read(ConsideredActionData.Field.ABILITY_ID);
            final int targetMapX = (int) action.read(ConsideredActionData.Field.TARGET_MAP_X);
            final int targetMapY = (int) action.read(ConsideredActionData.Field.TARGET_MAP_Y);

            // TODO: I'm guessing this is most certainly wrong.
            final float targetPriorityFloat = ((short) targetPriority - 1) / 128.0f;

//            if (casterUnitId != actor.get().getUnitId()) {
//                continue;
//            }
            if (skillSetId == 0 && abilityId == 0) {
                continue;
            }
            if (skillSetId == 0xff || abilityId == 0xffff) {
                continue;
            }
            if (skillSetId == 1 && abilityId != 0) {
                continue;
            }

            ImGui.columns(2);
            ImGui.labelText("Index", String.valueOf(action.getIndex()));
            ImGui.labelText("Hit Chance?", String.format("%d", hitChance) + "%%%");
            ImGui.labelText("Target Priority", String.format("%.4f", targetPriorityFloat));
            ImGui.labelText("Caster Unit ID", String.valueOf(casterUnitId));
            final Optional<UnitData> caster = units.stream()
                    .filter(unit -> unit.isValid() && casterUnitId == unit.getUnitId()).findFirst();
            if (caster.isPresent()) {
                ImGui.labelText("Caster Name", caster.get().getName());
            } else {
                ImGui.labelText("Caster Name", "(not found)");
            }
            // TODO: This isn't accounting for the different map layers
            final Optional<UnitData> target = units.stream()
                    .filter(unit -> unit.isValid() && targetMapX == unit.getMapX() && targetMapY == unit.getMapY()).findFirst();
            if (target.isPresent()) {
                ImGui.labelText("Target Name", target.get().getName());
            } else {
                ImGui.labelText("Target Name", "(not found)");
            }
            ImGui.nextColumn();
            ImGui.labelText("Skillset ID", String.format("0x%02x", skillSetId));
            if (skillSetId < skillSetNameTable.size()) {
                ImGui.labelText("Skillset Name", skillSetNameTable.get(skillSetId));
            } else {
                ImGui.labelText("Skillset Name", "(not found)");
            }

            ImGui.labelText("Ability ID", String.format("0x%03x", abilityId));
            if (abilityId < abilityNameTable.size()) {
                ImGui.labelText("Ability Name", abilityNameTable.get(abilityId));
            } else {
                ImGui.labelText("Ability Name", "(not found)");
            }
            ImGui.labelText("Target Position", String.format("(%d, %d)", targetMapX, targetMapY));
            ImGui.columns();
            ImGui.newLine();
        }
    }

    void renderUnitSelectionWindow() {
        if (ImGui.beginTabBar("Unit Selection")) {
            final List<UnitData> unitData = inspector.getUnitData();
            final List<MiscUnitData> miscUnitData = inspector.getMiscUnitData();
            for (int i = 0; i < UnitData.NUM; i++) {
                final UnitData unit = unitData.get(i);
                final MiscUnitData misc = miscUnitData.get(i);
//                final MiscUnitData misc = miscUnitData.get(i);
                if (unit.isInvalid()) {
                    continue;
                }
                if (ImGui.beginTabItem(String.format("%s##%d", unit.getName(), unit.getIndex()))) {
                    renderUnitTab(unit, misc);
                    ImGui.endTabItem();
                }
            }
            ImGui.endTabBar();
        }
    }

    private void miscUnitDataFieldControl(final MiscUnitData misc, final MiscUnitData.Field field) {
        ImGui.setNextItemWidth(150.0f);
        final Integer newVal = inputInt(field.getDisplayName() + "##MU", (int) misc.read(field), 1);
        if (newVal != null) {
            misc.write(field, newVal);
        }
    }

    private void renderMisc(final MiscUnitData misc) {
        ImGui.text(String.format("Previous 0x%08X", misc.read(MiscUnitData.Field.PREV)));
        ImGui.text(String.format("ID %d", misc.read(MiscUnitData.Field.ID)));
        ImGui.text(String.format("Unit Data Pointer 0x%08X", misc.read(MiscUnitData.Field.UNIT_DATA_POINTER)));
        for (final MiscUnitData.Field field : miscModifyFields) {
            miscUnitDataFieldControl(misc, field);
        }
        if (ImGui.beginTabBar("Misc Tab Bar")) {
            if (ImGui.beginTabItem("Add")) {
                renderStatusArray(misc.getStatusToAdd());
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Remove")) {
                renderStatusArray(misc.getStatusToRemove());
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
    }

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

    void renderUnitTab(final UnitData unit, final MiscUnitData misc) {
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
        if (ImGui.collapsingHeader("Misc")) {
            renderMisc(misc);
        }
        if (ImGui.collapsingHeader("AI Status Data")) {
            final AIStatusData aiStatusData = unit.getExtendedAIStatusData();
            if (aiStatusData != null) {
                renderAIStatusData(aiStatusData);
            }
        }
    }

    private void renderAIStatusData(final AIStatusData aiData) {
        final AIStatusData.Field[] fields = AIStatusData.Field.values();
        final int half = fields.length / 2;
        ImGui.columns(2);
        for (int i = 0; i < fields.length; i++) {
            if (i != 0 && i % half == 0) ImGui.nextColumn();
            aiStatusDataControl(aiData, fields[i]);
        }
        ImGui.columns();
        if (ImGui.beginTabBar("Unit AI Extended Status Tab Bar")) {
            if (ImGui.beginTabItem("Status")) {
                renderStatusArray(aiData.getStatus());
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("CT")) {
                renderStatusCTPage(aiData.getStatus(), aiData.getStatusCT());
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Inflicted")) {
                renderStatusArray(aiData.getInflictedStatus());
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
    }

    private void aiStatusDataControl(final AIStatusData aiData, final AIStatusData.Field field) {
        ImGui.setNextItemWidth(150.0f);
        final Integer newVal = inputInt(field.getDisplayName() + "##UAES", (int) aiData.read(field), 1);
        if (newVal != null) {
            aiData.write(field, newVal);
        }
    }

    private Integer inputInt(final String label, final int value, final int step) {
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
        final UnitData.Field[] fields = UnitData.Field.values();
        final int half = fields.length / 2;
        ImGui.columns(2);
        for (int i = 0; i < fields.length; i++) {
            if (i != 0 && i % half == 0) ImGui.nextColumn();
            unitDataFieldControl(unit, fields[i]);
        }
        ImGui.columns();
//        unitDataFieldControl(unit, UnitData.Field.X_COORD);
//        unitDataFieldControl(unit, UnitData.Field.Y_COORD);
//
//        unitDataFieldControl(unit, UnitData.Field.BRAVE);
//        ImGui.sameLine();
//        unitDataFieldControl(unit, UnitData.Field.FAITH);
//
//        for (final UnitData.Field field : statusFields) {
//            unitDataFieldControl(unit, field);
//        }
    }

    private void unitDataFieldControl(final UnitData unit, final UnitData.Field field) {
        ImGui.setNextItemWidth(150.0f);
        final Integer newVal = inputInt(field.getDisplayName() + "##UD", (int) unit.read(field), 1);
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