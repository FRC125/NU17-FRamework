package com.nutrons.steamworks.TrapezoidalMotionProfiling;

import com.nutrons.steamworks.TrapezoidalMotionProfiling.*;
import java.util.StringTokenizer;

/**
 * Class that serves to take a pre-generated motion profile as input and output
 * a followable trajectory for both sides of a drive train.
 */
public class ProfileDeserializer {

  public ProfileDeserializer() {
    // left empty intentionally
  }

  public Path deserialize(String serializedProfile) {
    StringTokenizer tokenizer = new StringTokenizer(serializedProfile, "\n");
    System.out.println("Reading Profiles");
    System.out.println("String has " + serializedProfile.length() + " chars");
    System.out.println("Found " + tokenizer.countTokens() + " tokens");

    String name = tokenizer.nextToken();
    int num_elements = Integer.parseInt(tokenizer.nextToken());

    Trajectory left = new Trajectory(num_elements);
    for (int i = 0; i < num_elements; ++i) {
      Trajectory.Segment segment = new Trajectory.Segment();
      StringTokenizer line_tokenizer = new StringTokenizer(
          tokenizer.nextToken(), " ");

      segment.pos = Double.parseDouble(line_tokenizer.nextToken());
      segment.vel = Double.parseDouble(line_tokenizer.nextToken());
      segment.acc = Double.parseDouble(line_tokenizer.nextToken());
      segment.jerk = Double.parseDouble(line_tokenizer.nextToken());
      segment.heading = Double.parseDouble(line_tokenizer.nextToken());
      segment.dt = Double.parseDouble(line_tokenizer.nextToken());
      segment.x = Double.parseDouble(line_tokenizer.nextToken());
      segment.y = Double.parseDouble(line_tokenizer.nextToken());

      left.setSegment(i, segment);
    }

    Trajectory right = new Trajectory(num_elements);
    for (int i = 0; i < num_elements; ++i) {
      Trajectory.Segment segment = new Trajectory.Segment();
      StringTokenizer line_tokenizer = new StringTokenizer(
          tokenizer.nextToken(), " ");

      segment.pos = Double.parseDouble(line_tokenizer.nextToken());
      segment.vel = Double.parseDouble(line_tokenizer.nextToken());
      segment.acc = Double.parseDouble(line_tokenizer.nextToken());
      segment.jerk = Double.parseDouble(line_tokenizer.nextToken());
      segment.heading = Double.parseDouble(line_tokenizer.nextToken());
      segment.dt = Double.parseDouble(line_tokenizer.nextToken());
      segment.x = Double.parseDouble(line_tokenizer.nextToken());
      segment.y = Double.parseDouble(line_tokenizer.nextToken());

      right.setSegment(i, segment);
    }

    System.out.println("...finished parsing path from string.");
    return new Path(name, new Trajectory.Pair(left, right));
  }

}

