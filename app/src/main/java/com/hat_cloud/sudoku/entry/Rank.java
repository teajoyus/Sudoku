package com.hat_cloud.sudoku.entry;

/**
 * Created by linmh on 2017/8/18.
 */

public class Rank {
    private String name;
    private String time;
    private int time_num;
    private int type;
    private static final String SPLITE = "==";
    public Rank(){

    }
    public static boolean isValid(String s){
        try{
            String[] arr = s.split(SPLITE);
           Integer.parseInt(arr[2]);
            Integer.parseInt(arr[3]);
            return  arr.length==4;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public Rank(String s){
        String[] arr = s.split(SPLITE);
        name = arr[0];
        time = arr[1];
        time_num = Integer.parseInt(arr[2]);
        type = Integer.parseInt(arr[3]);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTime_num() {
        return time_num;
    }

    public void setTime_num(int time_num) {
        this.time_num = time_num;
    }

    @Override
    public String toString() {
        return name+SPLITE+time+SPLITE+time_num+SPLITE+type;
    }
}
