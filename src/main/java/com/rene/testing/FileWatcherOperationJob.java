package com.rene.testing;

import org.apache.log4j.Logger;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

public class FileWatcherOperationJob extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = Logger.getLogger(FileWatcherOperationJob.class);
    private LocalDateTime startDate = LocalDateTime.now();

    private boolean runWatcher() throws IOException {

        spooler_log.info("***************************** watching 1************************");

        Path path = FileSystems.getDefault().getPath("/tmp/watched");

        JadeFileWatchingUtility jadeFileWatchingUtility = new JadeFileWatchingUtility(path, true, this);

        Thread thread = new Thread(jadeFileWatchingUtility);
        thread.start();

        return true;

    }

    public void triggerJobChain(String fileName) {

        Variable_set variable_set = spooler.create_variable_set();
        variable_set.set_value("fileName", "Confident-This-Is-gonna-work " + System.currentTimeMillis() + " " + fileName);

        Order order = spooler.create_order();
        order.set_id("Confident-This-Is-gonna-work" + System.currentTimeMillis());
        order.set_title("With title like this, you are ok");
        order.set_state_text("This is my state text");

        order.set_payload(variable_set);

        spooler.job_chain("/myWatcher/myWatcher").add_order(order);

    }


    public boolean spooler_process() throws IOException, InterruptedException {


        spooler_log.info(super.getOrderParams().value("fileName").length() + "--" + (super.getOrderParams().value("fileName") == "") + " **** RENE or RENE **** " + (super.getOrderParams().value("fileName") != null));

        if (super.getOrderParams().value("fileName").length() < 5) {

            spooler_log.info("************************** order loop ******** ");

            BlockingQueue<String> messageQueue = FileQueueingFactory.getMessageQueue();
            runWatcher();

            for(int i = 0; i < 3; i ++) {

                String filePath = messageQueue.poll(1, MINUTES);
                spooler_log.info((i +1) + " ************************** LOOP FILE NAME  ******** " + filePath);

                if (filePath != null) {

                    Variable_set variable_set = spooler.create_variable_set();
                    variable_set.set_value("fileName", "Order to transfer file " + filePath + "From A to B on " + System.currentTimeMillis() + " ");

                    Order order = spooler.create_order();
                    order.set_id("Order to transfer file " + filePath + "From A to B on " + System.currentTimeMillis());
                    order.set_title("With title like this, you are ok");
                    order.set_state_text("This is my state text");


                    String[] states = spooler.job_chain("/myWatcher/myWatcher").states();
                    order.set_state("second");
                    spooler_log.info("******** next state should be " + states[1]);

                    order.set_payload(variable_set);

                    spooler.job_chain("/myWatcher/myWatcher").add_order(order);

                }
            }
            spooler_task.order().set_state("success");
        }


        return signalSuccess();

    }

}
