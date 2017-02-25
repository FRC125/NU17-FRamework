package com.nutrons.libKudos254.vision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * VisionUpdate contains the various attributes outputted by the vision system.
 */
public class VisionUpdate {

  protected boolean valid = false;
  protected long capturedAgoMs;
  protected List<TargetInfo> targets;
  protected double capturedAtTimestamp = 0;

  private static long getOptLong(Object n, long defaultValue) {
    if (n == null) {
      return defaultValue;
    }
    return (long) n;
  }

  private static JSONParser parser = new JSONParser();

  private static Optional<Double> parseDouble(JSONObject j, String key) throws ClassCastException {
    Object given = j.get(key);
    if (given == null) {
      return Optional.empty();
    } else {
      return Optional.of((double) given);
    }
  }

  /**
   * Generates a VisionUpdate object given a JSON blob and a timestamp.
   *
   * @param currentTime timestamp
   * @param updateString blob with update string.
   * @return VisionUpdate object
   */
  //
  public static VisionUpdate generateFromJsonString(double currentTime, String updateString) {
    VisionUpdate update = new VisionUpdate();
    try {
      JSONObject objJ = (JSONObject) parser.parse(updateString);
      long capturedAgoMs = getOptLong(objJ.get("capturedAgoMs"), 0);
      if (capturedAgoMs == 0) {
        update.valid = false;
        return update;
      }
      update.capturedAgoMs = capturedAgoMs;
      update.capturedAtTimestamp = currentTime - capturedAgoMs / 1000.0;
      JSONArray targets = (JSONArray) objJ.get("targets");
      ArrayList<TargetInfo> targetInfos = new ArrayList<>(targets.size());
      for (Object targetObj : targets) {
        JSONObject target = (JSONObject) targetObj;
        Optional<Double> doublesY = parseDouble(target, "posY");
        Optional<Double> doublesZ = parseDouble(target, "posZ");
        if (!(doublesY.isPresent() && doublesZ.isPresent())) {
          update.valid = false;
          return update;
        }
        targetInfos.add(new TargetInfo(doublesY.get(), doublesZ.get()));
      }
      update.targets = targetInfos;
      update.valid = true;
    } catch (ParseException exception) {
      System.err.println("Parse error: " + exception);
      System.err.println(updateString);
    } catch (ClassCastException exception) {
      System.err.println("Data type error: " + exception);
      System.err.println(updateString);
    }
    return update;
  }

  public List<TargetInfo> getTargets() {
    return targets;
  }

  public boolean isValid() {
    return valid;
  }

  public long getCapturedAgoMs() {
    return capturedAgoMs;
  }

  public double getCapturedAtTimestamp() {
    return capturedAtTimestamp;
  }

}
