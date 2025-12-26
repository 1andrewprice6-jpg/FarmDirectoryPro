package com.example.farmdirectoryupgraded.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Employee entity for tracking workers (catchers, drivers, etc.)
 * Used for attendance tracking
 */
@Entity(
    tableName = "employees",
    indices = [
        Index(value = ["name"], name = "idx_employee_name"),
        Index(value = ["role"], name = "idx_employee_role"),
        Index(value = ["isActive"], name = "idx_employee_active")
    ]
)
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val role: String, // CATCHER, DRIVER, SUPERVISOR, ADMIN, OTHER
    val phone: String = "",
    val email: String = "",
    val photoPath: String? = null,
    val isActive: Boolean = true,
    val hireDate: Long? = null, // Timestamp in milliseconds
    val notes: String = "",
    val version: Int = 1 // Optimistic locking
)
