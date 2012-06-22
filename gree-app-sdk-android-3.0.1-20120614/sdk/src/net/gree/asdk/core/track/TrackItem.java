package net.gree.asdk.core.track;

import net.gree.asdk.core.codec.GreeHmac;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the values being tracked.
 */
class TrackItem {

  @SuppressWarnings("unused")
private final static String TAG = "TrackItem";
  private TrackItemStorage storage;

  public TrackItem(String type, String key, String data, String mixer, String uploaderClzName) {
    this.type = type;
    this.key = key;
    this.data = data;
    this.mixer = mixer;
    this.uploaderClzName = uploaderClzName;
  }

  public int id;
  public String type;
  public String key;
  public String data;
  public String seal;
  public String mixer;
  public String uploaderClzName;

  public String sign() {
    seal = "";
    try {
      seal = GreeHmac.sha1(type + key + data + mixer);
    } catch (NoSuchAlgorithmException nsa) {
      // do nothing
    }
    return seal;
  }

  class Item {
    public String type;
    public String key;

    public Item(String type, String key) {
      this.type = type;
      this.key = key;
    }
  }

  public List<Item> deletionQueue = new ArrayList<Item>();

  public boolean check(String seal) {
    String checkSeal = "";
    try {
      checkSeal = GreeHmac.sha1(type + key + data + mixer);
    } catch (NoSuchAlgorithmException nsa) {
      deletionQueue.add(new Item(type, key));
    }
    return (seal == null && checkSeal == null) || (seal != null && seal.equals(checkSeal));
  }

  public void save() {
    storage.save(this);
  }

  public void delete() {
    storage.delete(this);
  }

  public void setStorage(TrackItemStorage storage) {
    this.storage = storage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TrackItem trackItem = (TrackItem) o;

    if (id != trackItem.id) return false;
    if (data != null ? !data.equals(trackItem.data) : trackItem.data != null) return false;
    if (key != null ? !key.equals(trackItem.key) : trackItem.key != null) return false;
    if (mixer != null ? !mixer.equals(trackItem.mixer) : trackItem.mixer != null) return false;
    if (seal != null ? !seal.equals(trackItem.seal) : trackItem.seal != null) return false;
    if (type != null ? !type.equals(trackItem.type) : trackItem.type != null) return false;
    if (uploaderClzName != null ? !uploaderClzName.equals(trackItem.uploaderClzName) : trackItem.uploaderClzName != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (key != null ? key.hashCode() : 0);
    result = 31 * result + (data != null ? data.hashCode() : 0);
    result = 31 * result + (seal != null ? seal.hashCode() : 0);
    result = 31 * result + (mixer != null ? mixer.hashCode() : 0);
    result = 31 * result + (uploaderClzName != null ? uploaderClzName.hashCode() : 0);
    return result;
  }
}
