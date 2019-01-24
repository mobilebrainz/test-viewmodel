package com.kate.testviewmodel;

public class Artist {

    private String name;
    private int age;
    private String year;

    public Artist(String name, int age, String year) {
        this.name = name;
        this.age = age;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
