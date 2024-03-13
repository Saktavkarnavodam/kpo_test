package com.example.restaurantapp.services

import com.example.restaurantapp.model.Dish
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

interface ProcessService {
    suspend fun initiateOrderProcess(userIdentifier: String, selection: List<Dish>): Boolean
    suspend fun appendDishesToOrder(userIdentifier: String, additionalDishes: List<Dish>)
    fun abortOrderProcess(userIdentifier: String)
    fun getProcessedDishes(): List<Dish>
}

@Service
class ProcessServiceImpl : ProcessService {

    private lateinit var cookingJobs: List<Job>
    val preparedDishes: MutableList<Dish> = mutableListOf()
    private var orderCancelled: Boolean = false

    override fun getProcessedDishes(): List<Dish> {
        return preparedDishes.toList()
    }

    private suspend fun cookDish(userIdentifier: String, dish: Dish) = coroutineScope {
        orderCancelled = false
        println("Preparing ${dish.title} for $userIdentifier")
        delay(dish.preparationTime.toLong() * 1000)
        preparedDishes.add(dish)
        println("${dish.title} for $userIdentifier is ready!")
    }

    override suspend fun initiateOrderProcess(userIdentifier: String, selection: List<Dish>): Boolean = coroutineScope {
        println("Processing order for $userIdentifier")
        cookingJobs = selection.map { dish ->
            launch { cookDish(userIdentifier, dish) }
        }
        cookingJobs.joinAll()
        if (orderCancelled) println("Order for $userIdentifier was aborted")
        else println("Order for $userIdentifier has been fully prepared")
        return@coroutineScope orderCancelled
    }

    override fun abortOrderProcess(userIdentifier: String) {
        cookingJobs.forEach(Job::cancel)
        orderCancelled = true
    }

    override suspend fun appendDishesToOrder(userIdentifier: String, additionalDishes: List<Dish>) = coroutineScope {
        println("$userIdentifier is adding more dishes to the order")
        val additionalCookingJobs = additionalDishes.map { dish ->
            launch { cookDish(userIdentifier, dish) }
        }
        cookingJobs = cookingJobs.plus(additionalCookingJobs)
    }
}
