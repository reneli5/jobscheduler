package com.rene.testing;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class HelloFirstJobScheduler extends JobSchedulerJobAdapter {

	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			spooler_log.info("Hello, World! We got adaptor at last ...... ");
			super.setJSParam("firstJobParam", "I am coming from first Job");
		} catch (Exception e) {
			e.printStackTrace();
			spooler_log.error(e.getMessage());
			throw new JobSchedulerException(e.getMessage());
		} finally {
		} // finally
		return signalSuccess();
	} 

}
