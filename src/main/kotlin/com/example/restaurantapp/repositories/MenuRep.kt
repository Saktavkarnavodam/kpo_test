package com.example.restaurantapp.repositories

import com.example.restaurantapp.model.Dish
import io.micrometer.core.instrument.DistributionSummary
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal

interface MenuRepository : JpaRepository<Dish, Int>{
    fun findDishByTitleAndSummaryAndCostAndPreparationTime(title: String, summary: String, price: BigDecimal, preparationTime: Int): List<Dish>?
}
