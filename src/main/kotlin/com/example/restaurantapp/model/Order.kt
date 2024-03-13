package com.example.restaurantapp.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int?,
    @ElementCollection
    val dishIds: MutableList<Int>,
    val orderStartTime: LocalDateTime,
    val orderEndTime: LocalDateTime,
    val totalCost: BigDecimal,
    val customerName: String
)
{
    constructor() : this(0, mutableListOf(), LocalDateTime.now(), LocalDateTime.now(), BigDecimal(0), "")
}