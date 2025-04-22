package com.krachbank.api.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class FooTestObject {
    @Id
    private Long id;

    private String name;

    @OneToMany(mappedBy = "id")
    private List<BarTestObject> barTestObjects;

   
}
