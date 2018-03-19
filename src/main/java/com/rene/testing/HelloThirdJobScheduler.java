package com.rene.testing;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;

public class HelloThirdJobScheduler extends JobSchedulerJobAdapter {

	public boolean spooler_process() throws Exception {
		try {

			super.spooler_process();
			spooler_log.info("Hello, World! Third job getting parraters...... ");
			Variable_set params = super.getJobOrOrderParameters();
			
			spooler_log.info(params.count() + " ******** Received ***");
			spooler_log.info(params.value("secondJobParam") + " ****** was received");
		} catch (Exception e) {
			e.printStackTrace();
			spooler_log.error(e.getMessage());
			throw new JobSchedulerException(e.getMessage());
		} finally {
		} // finally

		return signalSuccess();
	} 

}
