package com.rene.testing;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;

public class HelloFirstJobScheduler extends JobSchedulerJobAdapter {

	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			spooler_log.info("Hello, World! We got adaptor at last ...... ");
			spooler_log.info("******* from payload = " + super.getOrderParams().value("fileName"));

			super.setJSParam("firstJobParam", "I am coming from first Job");

			Variable_set taskparams = spooler_task.params();

			taskparams.set_value("scheduler_file_path", "testing-file-passing.xml");

			super.getOrderParams().set_value("scheduler_file_path", "testing-file-passing.xml");

		} catch (Exception e) {
			e.printStackTrace();
			spooler_log.error(e.getMessage());
			throw new JobSchedulerException(e.getMessage());
		} finally {
		} // finally
		return signalSuccess();
	} 

}
