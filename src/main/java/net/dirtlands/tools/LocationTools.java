package net.dirtlands.tools;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationTools {

    public static String roundedLocationToString(Location loc){
        return loc.getWorld().getName() + ", " + Math.round(loc.getX()*100)/100.0 + ", " +
                Math.round(loc.getY()*100.0)/100.0 + ", " + Math.round(loc.getZ()*100.0)/100.0 + ", " +
                Math.round(loc.getYaw()*100.0)/100.0 + ", " + Math.round(loc.getPitch()*100.0)/100.0;
    }

    public static String locationToString(Location loc){
        return loc.getWorld().getName() + ", " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ", " + loc.getYaw() + ", " + loc.getPitch();
    }

    public static Location stringToLocation(String value){

        value = value.replaceAll(" ", "");

        String[] arg = value.split(",");

        //save coordinates to a Location
        return new Location(Bukkit.getWorld(arg[0]), Double.parseDouble(arg[1]), Double.parseDouble(arg[2]),
                Double.parseDouble(arg[3]), Float.parseFloat(arg[4]), Float.parseFloat(arg[5]));
    }
}
