package com.rene.testing;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class NextOperationInWatcher extends JobSchedulerJobAdapter {

    public boolean process() throws Exception {

        spooler_log.info( "trigger_files=" + spooler_task.trigger_files() );
        return true;

    }

}
