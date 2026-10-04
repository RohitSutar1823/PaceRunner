package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_records ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT COUNT(*) FROM workout_records")
    fun getWorkoutCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(distanceMeters), 0.0) FROM workout_records")
    fun getTotalDistanceMeters(): Flow<Float>

    @Query("SELECT COALESCE(MAX(maxSpeedKmh), 0.0) FROM workout_records")
    fun getMaxSpeedRecord(): Flow<Float>

    @Query("SELECT COALESCE(SUM(totalDurationSeconds), 0) FROM workout_records")
    fun getTotalDurationSeconds(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("DELETE FROM workout_records WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    @Query("DELETE FROM workout_records")
    suspend fun clearAllWorkouts()
}
