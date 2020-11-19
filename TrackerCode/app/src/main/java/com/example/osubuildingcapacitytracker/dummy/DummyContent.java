package com.example.osubuildingcapacitytracker.dummy;

import android.content.Intent;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    public static final Map<String, DummyItem> ITEM_NAME_MAP = new HashMap<String, DummyItem>();
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 5;

    static {
        ITEMS.add(new DummyItem(Integer.toString(1),"Dreese Lab", new Integer(0),"2015 Neil Ave, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(2),"Baker Systems Engineering", new Integer(0),"1971 Neil Ave, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(3),"Journalism Building", new Integer(0),"242 W 18th Ave, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(4),"Caldwell Lab", new Integer(0),"234 W 18th Ave, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(5),"Smith Lab", new Integer(0),"174 W 18th Ave, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(6),"McPherson Chemical Lab", new Integer(0),"140 W 18th Ave #053, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(7),"Hitchcock Hall", new Integer(0),"2070 Neil Ave, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(8),"Physics Research Building", new Integer(0),"191 W Woodruff Ave, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(9),"Thompson Library", new Integer(0),"1858 Neil Ave, \nColumbus, OH 43210", "thompson.jpg", 1000.0 ));
        ITEMS.add(new DummyItem(Integer.toString(10),"18th Avenue Library", new Integer(0),"175 W 18th Ave, \nColumbus, OH 43210", "avenue.jpg", 600.0 ));
        ITEMS.add(new DummyItem(Integer.toString(11),"Stillman Hall", new Integer(0),"1947 College Rd N, \nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(12),"OSU RPAC", new Integer(0),"337 Annie and John Glenn Ave, \nColumbus, OH 43210", "rpac.jpg", 1000.0 ));
        ITEMS.add(new DummyItem(Integer.toString(13),"Bolz Hall", new Integer(0),"2036 Neil Ave,\nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        ITEMS.add(new DummyItem(Integer.toString(14),"Knowlton Hall", new Integer(0),"275 W Woodruff Ave,\nColumbus, OH 43210", "default_no_logo.png", 200.0 ));
        for (DummyItem dummyitem: ITEMS) {
            Log.i("DummyContent", "adding items to map");
            ITEM_MAP.put(dummyitem.id, dummyitem);
            ITEM_NAME_MAP.put(dummyitem.name, dummyitem);
        }
    }




    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public Double percent;
        public final String name;
        public final String details;
        public Integer capacity;
        public final Double maxCap;
        public final String imageName;

        public DummyItem(String id, String name, Integer capacity, String details, String imageName, Double maxCap) {
            this.id = id;
            this.capacity = capacity;
            this.name = name;
            this.details = details;
            this.imageName = imageName;
            this.maxCap = maxCap;
            this.percent = capacity/maxCap;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
            this.percent = capacity/maxCap*100.00;
        }

        public String getId() {
            return id;
        }

        public Integer getCapacity() {
            return capacity;
        }
        public Double getMaxCap(){
            return maxCap;
        }
        public String getPercent(){
            DecimalFormat percentFormat = new DecimalFormat("0.#");
            return percentFormat.format(percent)+"%";
        }
        public String getInfo(){
            DecimalFormat maxCapFormat = new DecimalFormat("0");
            return name+ "\n("+ capacity.toString()+"/"+maxCapFormat.format(maxCap)+")";
        }

        @Override
        public String toString() {
            return name;
        }
    }
}