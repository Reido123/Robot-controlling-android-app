package com.example.aplikacja_final

import android.util.Log
import kotlin.math.*


class KinematykaManipulatora(private val l1: Double, private val l2: Double) {

    data class Angles(var theta1: Double, var theta2: Double)

    /**
     * Oblicza kąty theta1 i theta2 dla zadanych współrzędnych (x, y) punktu końcowego
     * @param x współrzędna x punktu końcowego
     * @param y współrzędna y punktu końcowego
     * @return kąty theta1 i theta2
     */
    fun obliczKinematykeOdwrotna(x: Double, y: Double): Angles? {
        // Oblicz odległość od punktu bazowego do punktu końcowego
        val distance = sqrt(x * x + y * y)
        if (distance > l1 + l2) {
            // Punkt nieosiągalny
            return null
        }

        val cosTheta2 = ((x * x) + (y * y) - l1 * l1 - l2 * l2) / (2 * l1 * l2)
        var theta2 = acos(cosTheta2)

        val sinTheta2 = sqrt(1 - cosTheta2 * cosTheta2)
        val k1 = l1 + l2 * cosTheta2
        val k2 = l2 * sinTheta2
        var theta1 = (atan2(y, x) - atan2(k2, k1))
        if (abs(Math.toDegrees(theta1).toInt()) >= 178)
        {
            theta1 = Math.toRadians(178.0)
        }
        if ((Math.toDegrees(theta1).toInt()) >= 0)
        {
            theta1 = Math.toRadians(0.0)
        }
        if (abs(Math.toDegrees(theta2)).toInt() >= 170)
        {
            theta2 = Math.toRadians(170.0)
        }
        Log.e("XD",(Math.toDegrees(theta1)).toString())
        Log.e("XD",(Math.toDegrees(theta2)).toString())
        return Angles(theta1, theta2)
    }

    /**
     * Przemieszcza punkt końcowy manipulatora o zadane odległości wzdłuż osi x lub y
     * @param dx przemieszczenie wzdłuż osi x
     * @param dy przemieszczenie wzdłuż osi y
     * @param currentAngles aktualne kąty theta1 i theta2
     * @return nowe kąty theta1 i theta2
     */
    fun przemiescPunktKoncowy(
        dx: Double,
        dy: Double,
        currentAngles: Angles,
        blokujTheta1: Boolean = false,
        blokujTheta2: Boolean = false
    ): Angles? {
        // Oblicz współrzędne aktualnego punktu końcowego
        var currentX = l1 * cos(currentAngles.theta1) + l2 * cos(currentAngles.theta1 + currentAngles.theta2)
        var currentY = l1 * sin(currentAngles.theta1) + l2 * sin(currentAngles.theta1 + currentAngles.theta2)

        // Zabezpieczenia przed przekroczeniem zakresów
        if (currentX <= 4.0) currentX = 4.0
        if (currentY >= 0.0) currentY = 0.0

        // Oblicz nowe współrzędne punktu końcowego
        var newX = currentX + dx
        var newY = currentY + dy

        // Jeśli blokujemy theta1, używamy starego theta1
        val updatedAngles = obliczKinematykeOdwrotna(newX, newY) ?: return null

        return Angles(
            theta1 = if (blokujTheta1) currentAngles.theta1 else updatedAngles.theta1,
            theta2 = if (blokujTheta2) currentAngles.theta2 else updatedAngles.theta2
        )
    }

    data class Angles_MANUAL(val theta1: Double, val theta2: Double)
    data class Position_MANUAL(var x: Double, var y: Double)

    fun kinematyka_MANUAL(theta1: Double, theta2: Double ): Position_MANUAL {
        // Wyznaczanie pozycji końca manipulatora
        var x = l1 * Math.cos(theta1) + l2 * Math.cos(theta1 + theta2)
        var y = l1 * Math.sin(theta1) + l2 * Math.sin(theta1 + theta2)
        return Position_MANUAL(x, y)
    }

}