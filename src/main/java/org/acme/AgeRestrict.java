package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;


@ApplicationScoped
public class AgeRestrict {

    private final Logger logger = Logger.getLogger(AgeRestrict.class);

    @Inject
    @Channel("underage")
    Emitter<Customer> underageEmitter;

    @Incoming("customers")
    public void underage(Customer customer) {
        logger.info("Filtering a customer: " + customer);
        if (customer.age < 20)
            underageEmitter.send(customer);
    }

    @Incoming("underagein")
    public void underage_in(Customer customer) {
        logger.info("Got an underage customer: " + customer);

    }


}