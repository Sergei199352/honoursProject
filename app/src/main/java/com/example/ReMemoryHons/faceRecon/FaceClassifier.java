package com.example.ReMemoryHons.faceRecon;

import android.graphics.Bitmap;
import android.graphics.RectF;

/** Generic interface for interacting with different recognition engines. */
public interface FaceClassifier {

    void addNew(String name, Classification classification, String string);

    Classification classify(Bitmap bitmap, boolean bool);

    public class Classification {
        private final String id;

        /** Display name for the recognition. */
        private final String title;
        // A sortable score for how good the recognition is relative to others. Lower should be better.
        private final Float distance;
        private Object embeeding;
        /** Optional location within the source image for the location of the recognized face. */
        private RectF location;
        private Bitmap crop;
        private String memory;

        public Classification(
                final String id, final String title, final Float distance, final RectF location, String memory) {
            this.id = id;
            this.title = title;
            this.distance = distance;
            this.location = location;
            this.memory = memory;
            this.embeeding = null;
            this.crop = null;

        }

        public void setEmbeeding(Object extra) {
            this.embeeding = extra;
        }
        public Object getEmbeeding() {
            return this.embeeding;
        }

        public String getMemory() {
            return memory;
        }

        public void setMemory(String memory) {
            this.memory = memory;
        }

        public String getTitle() {
            return title;
        }

        public Float getDistance() {
            return distance;
        }

        public RectF getLocation() {
            return new RectF(location);
        }






    }
}
