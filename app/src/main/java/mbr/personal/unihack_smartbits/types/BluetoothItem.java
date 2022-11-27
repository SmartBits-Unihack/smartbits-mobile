package mbr.personal.unihack_smartbits.types;

public class BluetoothItem {

    String name;
    String address;

    public BluetoothItem(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

}
