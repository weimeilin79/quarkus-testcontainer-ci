package org.acme;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Customer {

    public int id;
    public String name;
    public int age;


    public Customer() {
    }

    public Customer(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public String toString(){
        return "Customer --> id:["+id+"] name:["+name+"] age:["+age+"]";

    }
}