package sf.closestispark.model;

import java.util.List;

import sf.closestispark.helper.Tag;

/**
 * Created by mesutgenc on 22.07.2017.
 */

public class Park {
    private String code;
    private String name;
    private String location;
    private String address;
    private String city;
    private String type;
    private String xCoor;
    private String yCoor;
    private List<Tag> tags;

    public Park(String code, String name, String location, String address, String city, String type, String xCoor, String yCoor) {
        this.code = code;
        this.name = name;
        this.location = location;
        this.address = address;
        this.city = city;
        this.type = type;
        this.xCoor = xCoor;
        this.yCoor = yCoor;
        //this.tags = tags;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getxCoor() {
        return xCoor;
    }

    public void setxCoor(String xCoor) {
        this.xCoor = xCoor;
    }

    public String getyCoor() {
        return yCoor;
    }

    public void setyCoor(String yCoor) {
        this.yCoor = yCoor;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public boolean hasTag(String string) {

        if (getCity().equals(string)){
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Park park = (Park) o;

        if (code != null ? !code.equals(park.code) : park.code != null) return false;
        if (name != null ? !name.equals(park.name) : park.name != null) return false;
        if (location != null ? !location.equals(park.location) : park.location != null)
            return false;
        if (address != null ? !address.equals(park.address) : park.address != null) return false;
        if (city != null ? !city.equals(park.city) : park.city != null) return false;
        if (type != null ? !type.equals(park.type) : park.type != null) return false;
        if (xCoor != null ? !xCoor.equals(park.xCoor) : park.xCoor != null) return false;
        if (yCoor != null ? !yCoor.equals(park.yCoor) : park.yCoor != null) return false;
        return tags != null ? tags.equals(park.tags) : park.tags == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (xCoor != null ? xCoor.hashCode() : 0);
        result = 31 * result + (yCoor != null ? yCoor.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }
}
