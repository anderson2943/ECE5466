package com.example.osubuildingcapacitytracker.dummy;

import android.content.Intent;

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

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 5;

    static {
        // Add some sample items.

        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        if (position == 1) {
            return new DummyItem(Integer.toString(position),Integer.toString(80) + "%",
                    "Thompson Library","Thompson Library\n(320 out of 400)",
                    makeDetails(position), "thompson");
        }
        else if (position == 4) {
            return new DummyItem(Integer.toString(position),Integer.toString(50) + "%",
                    "The RPAC","The RPAC\n(500 out of 1000)",
                    makeDetails(position), "rpac");
        }
        else if(position == 3) {
            return new DummyItem(Integer.toString(position),Integer.toString(60) + "%",
                    "The Student Union" ,"The Student Union\n(60 out of 100)",
                    makeDetails(position), "union");
        }
        else if (position == 2) {
            return new DummyItem(Integer.toString(position),Integer.toString(50) +"%",
                    "South Jesse Owens", "South Jesse Owens Rec Center\n(20 out of 40)",
                    makeDetails(position), "southrec");
        }
        else {
            return new DummyItem(Integer.toString(position),Integer.toString(30) + "%",
                    "18th Avenue Library","18th Avenue Library\n(90 out of 300)",
                    makeDetails(position), "avenue");
        }


    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        //builder.append("Details about Item: ").append(position);
        if (position == 1) {
            builder.append("1858 Neil Ave\nColumbus, OH 43210");
        }
        else if (position == 4) {
            builder.append("337 Annie and John Glenn Ave\nColumbus, OH 43210");
        }
        else if (position == 3) {
            builder.append("1739 N High St\nColumbus, OH 43210");
        }
        else if (position == 2){
            builder.append("175 W 11th Ave\nColumbus, OH 43201");
        }
        else {
            builder.append("175 W 18th Ave\nColumbus, OH 43210");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String percent;
        public final String name;
        public final String details;
        public final String capacity;
        public final String imageName;

        public DummyItem(String id, String percent, String name, String capacity, String details, String imageName) {
            this.id = id;
            this.capacity = capacity;
            this.percent = percent;
            this.name = name;
            this.details = details;
            this.imageName = imageName;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}