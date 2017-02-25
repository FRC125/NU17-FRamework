package com.nutrons.libKudos254;

import java.text.DecimalFormat;

/**
 * A rotation in a 2d coordinate frame represented a point on the unit circle
 * (cosine and sine).
 * Inspired by Sophus (https://github.com/strasdat/Sophus/tree/master/sophus)
 */
public class Rotation2d {
  protected static final double kEpsilon = 1E-9;

  protected double cosAngle;
  protected double sinAngle;

  public Rotation2d() {
    this(1, 0, false);
  }

  /**
   * Makes a Rotation2d.
   * @param xparam posX
   * @param yparam posY
   * @param normalize normalize
   */
  public Rotation2d(double xparam, double yparam, boolean normalize) {
    cosAngle = xparam;
    sinAngle = yparam;
    if (normalize) {
      normalize();
    }
  }

  public Rotation2d(Rotation2d other) {
    cosAngle = other.cosAngle;
    sinAngle = other.sinAngle;
  }

  public static Rotation2d fromRadians(double angleRadians) {
    return new Rotation2d(Math.cos(angleRadians), Math.sin(angleRadians), false);
  }

  public static Rotation2d fromDegrees(double angleDegrees) {
    return fromRadians(Math.toRadians(angleDegrees));
  }

  /**
   * From trig, we know that sin^2 + cos^2 == 1, but as we do math on this
   * object we might accumulate rounding errors. Normalizing forces us to
   * re-scale the sin and cos to reset rounding errors.
   */
  public void normalize() {
    double magnitude = Math.hypot(cosAngle, sinAngle);
    if (magnitude > kEpsilon) {
      sinAngle /= magnitude;
      cosAngle /= magnitude;
    } else {
      sinAngle = 0;
      cosAngle = 1;
    }
  }

  /**
   * Returns the cos angle of the object.
   * @return the cos angle.
   */
  public double cos() {
    return cosAngle;
  }

  /**
   * Returns the sin angle of the object.
   * @return the sin angle.
   */
  public double sin() {
    return sinAngle;
  }

  /**
   * Returns the tan angle of the object.
   * @return the tan angle.
   */
  public double tan() {
    if (cosAngle < kEpsilon) {
      if (sinAngle >= 0.0) {
        return Double.POSITIVE_INFINITY;
      } else {
        return Double.NEGATIVE_INFINITY;
      }
    }
    return sinAngle / cosAngle;
  }

  public double getRadians() {
    return Math.atan2(sinAngle, cosAngle);
  }

  public double getDegrees() {
    return Math.toDegrees(getRadians());
  }

  /**
   * We can rotate this Rotation2d by adding together the effects of it and
   * another rotation.
   *
   * @param other The other rotation. See:
   *              https://en.wikipedia.org/wiki/Rotation_matrix
   * @return This rotation rotated by other.
   */
  public Rotation2d rotateBy(Rotation2d other) {
    return new Rotation2d(cosAngle * other.cosAngle - sinAngle * other.sinAngle,
        cosAngle * other.sinAngle + sinAngle * other.cosAngle, true);
  }

  /**
   * The inverse of a Rotation2d "undoes" the effect of this rotation.
   *
   * @return The opposite of this rotation.
   */
  public Rotation2d inverse() {
    return new Rotation2d(cosAngle, -sinAngle, false);
  }

  @Override
  public String toString() {
    final DecimalFormat fmt = new DecimalFormat("#0.000");
    return "(" + fmt.format(getDegrees()) + " deg)";
  }
}
