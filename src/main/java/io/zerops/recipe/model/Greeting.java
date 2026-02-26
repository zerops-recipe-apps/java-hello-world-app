package io.zerops.recipe.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "greetings")
public class Greeting {

    @Id
    private Integer id;

    private String message;

    public Integer getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
