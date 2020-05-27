package rainbowbismuth.fft.example;

import rainbowbismuth.fft.TacticsInspector;
import rainbowbismuth.fft.WindowsMemoryViewer;
import rainbowbismuth.fft.view.UnitData;

import java.util.List;

public class StdoutPSXE15 {
    public static void main(final String[] args) throws Exception {
        final WindowsMemoryViewer memory = WindowsMemoryViewer
                .createEPSXE15Viewer(null, "ePSXe - Enhanced PSX emulator");

        final TacticsInspector inspector = new TacticsInspector();
        memory.read(inspector.getRAM());

        final List<UnitData> unitData = inspector.getUnitData();
        for (final UnitData unit : unitData) {
            if (unit.isInvalid()) {
                continue;
            }
            System.out.println(String.format("Unit %d (%s, %s)", unit.getIndex(), unit.getName(), unit.getJobName()));
            for (final UnitData.Field field : UnitData.Field.values()) {
                System.out.println(String.format("%20s: %d", field.getDisplayName(), unit.read(field)));
            }
            System.out.println();
        }
    }
}
