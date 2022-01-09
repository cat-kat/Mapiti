package com.example.mapiti;

public class Place {
    public String name, route, about, photo, ID;
    public Double first_coord, second_coord;

    public Place(String name, String route, String about, String photo, Double first_coord, Double second_coord, String ID) {
        this.name = name;
        this.route = route;
        this.about = about;
        this.first_coord = first_coord;
        this.second_coord = second_coord;
        this.photo = photo;
        this.ID = ID;
    }
}
