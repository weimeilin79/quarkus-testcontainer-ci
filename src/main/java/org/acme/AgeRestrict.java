package org.acme;


import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

//import org.eclipse.microprofile.reactive.messaging.Outgoing;
//import io.smallrye.mutiny.Multi;
//import java.time.Duration;
//import java.util.Random;

@ApplicationScoped
public class AgeRestrict {

    private final Logger logger = Logger.getLogger(AgeRestrict.class);
    

    @Inject
    @Channel("underage")
    Emitter<Customer> underageEmitter;

   @Incoming("customers")
    public void underage(Customer customer) {
        logger.info("Filtering a customer: "+ customer);
        if(customer.age<20)
            underageEmitter.send(customer);
    } 

    @Incoming("underagein")
    public void underage_in(Customer customer) {
        logger.info("Got an underage customer: "+ customer);
        
    } 

    
    //private final Random random = new Random();
    //@Outgoing("customers-queue")
    //public Multi<Customer> generate() {
      //  return Multi.createFrom().ticks().every(Duration.ofMillis(5000)).map(x -> new Customer(random.nextInt(),"Random-"+random.nextInt(),random.nextInt(30)));
    //}
    
   
}