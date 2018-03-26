package com.rene.testing;

import sos.spooler.Order;
import sos.spooler.Spooler;
import sos.spooler.Variable_set;

import java.io.Serializable;

public class JobChainProcessTrigger implements Serializable{

    private Spooler spooler;

    public JobChainProcessTrigger(Spooler spooler) {

        this.spooler = spooler;

    }

    public boolean triggerJobChain(String fileName){

        Variable_set variable_set = spooler.create_variable_set();
        variable_set.set_value("fileName", "Confident-This-Is-gonna-work " + System.currentTimeMillis());

        Order order = spooler.create_order();
        order.set_id("Confident-This-Is-gonna-work" + System.currentTimeMillis());
        order.set_title("With title like this, you are ok");
        order.set_state_text("This is my state text");

        order.set_payload(variable_set);

        spooler.job_chain("/myWatcher/myWatcher").add_order(order);

        return true;

    }
}
