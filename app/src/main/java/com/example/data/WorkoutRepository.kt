package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
    val totalWorkouts: Flow<Int> = workoutDao.getWorkoutCount()
    val totalDistanceMeters: Flow<Float> = workoutDao.getTotalDistanceMeters()
    val maxSpeedRecord: Flow<Float> = workoutDao.getMaxSpeedRecord()
    val totalDurationSeconds: Flow<Int> = workoutDao.getTotalDurationSeconds()

    suspend fun saveWorkout(workout: WorkoutEntity): Long {
        return workoutDao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(id: Long) {
        workoutDao.deleteWorkout(id)
    }
}
