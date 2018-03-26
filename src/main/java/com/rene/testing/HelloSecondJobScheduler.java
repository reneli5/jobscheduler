package com.rene.testing;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;

public class HelloSecondJobScheduler extends JobSchedulerJobAdapter {

	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			spooler_log.info("Hello, World! Second job getting parraters...... ");
			Variable_set params = super.getJobOrOrderParameters();
			
			spooler_log.info(params.count() + " ******** Received ***");
			spooler_log.info(params.value("firstJobParam") + " ****** was received");
			
			super.setJSParam("secondJobParam", "I am coming from second Job");

			System.out.println("********* task xml = " + spooler_task.params().xml());
			System.out.println("********* order param = " + getOrder().params().xml());

			//spooler_task.order().params().value("scheduler_file_path");

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
