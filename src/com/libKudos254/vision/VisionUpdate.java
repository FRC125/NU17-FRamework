package com.libKudos254.vision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * VisionUpdate contains the various attributes outputted by the vision system,
 * namely a list of targets and the timestamp at which it was captured.
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
    Object temp = j.get(key);
    if (temp == null) {
      return Optional.empty();
    } else {
      return Optional.of((double) temp);
    }
  }

  /**
   * Generates a VisionUpdate object given a JSON blob and a timestamp.
   *
   * @param currentTime
   *            timestamp
   * @param updateString
   *            blob with update string, example: { "capturedAgoMs" : 100,
   *            "targets": [{"y": 5.4, "z": 5.5}] }
   * @return VisionUpdate object
   */

  public static VisionUpdate generateFromJsonString(double currentTime, String updateString) {
    VisionUpdate update = new VisionUpdate();
    try {
      JSONObject jsonObj = (JSONObject) parser.parse(updateString);
      long capturedAgoMs = getOptLong(jsonObj.get("capturedAgoMs"), 0);
      if (capturedAgoMs == 0) {
        update.valid = false;
        return update;
      }
      update.capturedAgoMs = capturedAgoMs;
      update.capturedAtTimestamp = currentTime - capturedAgoMs / 1000.0;
      JSONArray targets = (JSONArray) jsonObj.get("targets");
      ArrayList<TargetInfo> targetInfos = new ArrayList<>(targets.size());
      for (Object targetObj : targets) {
        JSONObject target = (JSONObject) targetObj;
        Optional<Double> verticalAxis = parseDouble(target, "y");
        Optional<Double> depthAxis = parseDouble(target, "z");
        if (!(verticalAxis.isPresent() && depthAxis.isPresent())) {
          update.valid = false;
          return update;
        }
        targetInfos.add(new TargetInfo(verticalAxis.get(), depthAxis.get()));
      }
      update.targets = targetInfos;
      update.valid = true;
    } catch (ParseException exc) {
      System.err.println("Parse error: " + exc);
      System.err.println(updateString);
    } catch (ClassCastException exc) {
      System.err.println("Data type error: " + exc);
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
