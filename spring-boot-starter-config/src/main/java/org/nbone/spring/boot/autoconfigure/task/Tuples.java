package org.nbone.spring.boot.autoconfigure.task;

/**
 * @author thinking
 * @version 1.0
 * @since 1/20/21
 */
public class Tuples<T> {

    private String name;
    private String property;
    private T value;


    public Tuples(String name, String property, T value) {
        this.name = name;
        this.property = property;
        this.value = value;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
