package com.example.restaurantapp.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "menu")
class Dish(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    val title: String,
    val summary : String,
    val cost: BigDecimal,
    val preparationTime: Int,
    val createdBy: String
) {
    override fun toString(): String {
        return "Dish[id=$id, title='$title', summary = '$summary', cost=$cost, preparationTime=$preparationTime, createdBy='$createdBy']"
    }
}
