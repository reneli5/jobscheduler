package com.rene.testing;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileWatcherOperationJob extends JobSchedulerJobAdapter {

    String[] paths;

    int index;

    public boolean spooler_open() throws IOException {

        spooler_log.info("***************************** watching 1************************");

        Path path = FileSystems.getDefault().getPath("/tmp/watched");

        JadeFileWatchingUtility jadeFileWatchingUtility = new JadeFileWatchingUtility(path, true);

        Thread thread = new Thread(jadeFileWatchingUtility);
        thread.start();

        try {
            Thread.sleep(70000);
            getOrder().remove_from_job_chain();

            Order order = new Order(spooler.com_invoker());
            order.set_id("I have update this order Id. Can you see it");

            getOrder().job_chain().add_or_replace_order(order);
            signalSuccess();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        spooler_job.start_when_directory_changed("/tmp/watched", "(txt|exe|com)?$");
//
//        try {
//            spooler_log.info("***************************** waiting for file events ************************");
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        spooler_log.info("***************************** watching 2************************");
//
//        spooler_log.info("changed_directories=" + spooler_task.changed_directories());
//        spooler_log.info("trigger_files=" + spooler_task.trigger_files());
//
//
//        paths = spooler_task.trigger_files().split(";");
//        index = 0;

        return true;

    }

    public boolean spooler_process() {

        File file = new File(paths[index++]);
        spooler_log.info("Processing file ***********************" + file);
        file.delete();
        return index < paths.length;

    }

}
