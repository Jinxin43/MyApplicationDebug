package com.example.dingtu2.myapplication.model;

public class CityName {
    private final String name;

    public CityName(String name) {
        this.name=name;
    }

    public boolean equals(Object obj){
        if(this==obj){
            return true ;
        }
        if(!(obj instanceof CityName)){
            return false ;
        }
        CityName p = (CityName) obj ;
        if(this.name.equals(p.name)){
            return true ;
        }else{
            return false ;
        }
    }
    public int hashCode(){
        return this.name.hashCode()  ;
    }
    public String toString(){
        return "姓名：" + this.name  ;
    }
}
