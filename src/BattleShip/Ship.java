/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BattleShip;

import java.util.List;
import java.util.Vector;

/**
 *
 * @author Duy Anh Tang
 */
public class Ship {
    List<Integer> location;
    int sinkcounter;
    public Ship(){
        location=new Vector<>();
        sinkcounter=0;
    }
    public Ship(int[] l){
        location=new Vector<>();
        for (int i:l){
            location.add(i);
        }
        sinkcounter=l.length;
    }
    /**
     * 
     * @param i: location the opponent is hitting
     * @return true if it hits, false if it does not
     */
    public boolean hit(int i){
        if (location.contains(i)){
            sinkcounter--;
            return true;
        }
        return false;
    }
    public void addLocation(int i){
        location.add(i);
        sinkcounter++;
    }
    public boolean isExploded(){
        return sinkcounter==0;
    }
    @Override
    public String toString(){
        StringBuffer s=new StringBuffer("[");
        for(int i=0;i<location.size();i++){
            s.append(location.get(i)+" ");
        }
        s.append("]"+ " Length: "+sinkcounter);
        return s.toString();
    }
}
