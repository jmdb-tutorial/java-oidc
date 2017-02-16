package jmdb.oidc.platform.jmdb.oidc.platform.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemProcess {
    private static final Logger LOG = LoggerFactory.getLogger(SystemProcess.class);

    public SystemProcess() {
    }

    public void writeProcessIdToFile(String filename) {
        FileOutputStream out = null;

        try {
            File e = new File(System.getProperty("user.home"), filename);
            if(e.exists()) {
                e.delete();
            }

            e.deleteOnExit();
            e.createNewFile();
            out = new FileOutputStream(e);
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            if(!processName.contains("@")) {
                throw new RuntimeException(String.format("Process name [%s] is not parsable for the processId!", new Object[]{processName}));
            }

            String[] parts = processName.split("@");
            String processId = parts[0];
            String hostName = parts[1];
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
            writer.println(processId);
            writer.flush();
            LOG.info(String.format("Writing PID file for process with PID [%s] on [%s] (file is at [%s])", new Object[]{processId, hostName, e.getAbsolutePath()}));
        } catch (Exception var12) {
            throw new RuntimeException(String.format("Could not write process Id file [~/%s]", new Object[]{filename}), var12);
        } finally {
            IoStreamHandling.closeQuietly(out);
        }

    }
}
