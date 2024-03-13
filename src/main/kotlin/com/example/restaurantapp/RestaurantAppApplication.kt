package com.example.restaurantapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestaurantAppApplication

fun main(args: Array<String>) {
    runApplication<RestaurantAppApplication>(*args)
}
