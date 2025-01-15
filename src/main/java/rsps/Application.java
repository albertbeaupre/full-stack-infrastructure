import backend.utility.Text;
import rsps.network.Server;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.logging.Logger;


private static final Logger Log = Logger.getLogger("Application");

void main() {
    MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
    MemoryUsage heap = bean.getHeapMemoryUsage();
    MemoryUsage nonHeap = bean.getNonHeapMemoryUsage();

    Server.start();

    Log.info(STR."Heap Memory Usage: \{Text.of(heap.getUsed()).formatStorageUnits()}");
    Log.info(STR."Non-Heap Memory Usage: \{Text.of(nonHeap.getUsed()).formatStorageUnits()}\n");
}
