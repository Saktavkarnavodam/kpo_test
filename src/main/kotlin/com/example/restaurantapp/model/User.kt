package com.example.restaurantapp.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val accountId: UUID? = null,
    val accountName: String = "",
    val secret: String = "",
    val role: String = ""
)
