package com.example.restaurantapp.controllers

import com.example.restaurantapp.model.Dish
import com.example.restaurantapp.model.Order
import com.example.restaurantapp.model.User
import com.example.restaurantapp.repositories.OrderRepository
import com.example.restaurantapp.services.ProcessServiceImpl
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

interface ClientController {
    @PostMapping("/createOrder")
    suspend fun createOrder(@RequestParam dishId: List<Int>): String

    @PostMapping("/payOrder")
    fun payOrder(
        @RequestParam id: Int,
        @RequestParam sum: BigDecimal
    ): String

    @DeleteMapping("/cancelOrder")
    fun cancelOrder(): String

    @PutMapping("/expandOrder")
    suspend fun expandOrder(@RequestParam dishId: List<Int>): String

    @GetMapping("/getCurrentClient")
    fun getCurrentClient(): Any
}


@RestController
@RequestMapping("/api/client")
class ClientControllerImpl(
    @Autowired private val orderRep: OrderRepository,
    @Autowired private val processService: ProcessServiceImpl,
) : ClientController {

    var currClient: User? = null

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @PostMapping("/createOrder")
    override suspend fun createOrder(@RequestParam dishId: List<Int>): String {
        if (currClient == null) {
            return "No logged client"
        }

        val timeStart = LocalDateTime.now()
        val dishes: List<Dish>
        try {
            dishes = getAllDishesByIds(dishId)
        } catch (e: Exception) {
            return e.message!!
        }

        return if (processService.initiateOrderProcess(currClient!!.accountName, dishes)) {
            "Order was cancelled"
        } else {
            val timeEnd = LocalDateTime.now()
            val price = processService.getProcessedDishes().sumOf { dish -> dish.cost }
            val order = Order(0, dishId.toMutableList(), timeStart, timeEnd, price, currClient!!.accountName)
            withContext(Dispatchers.IO) {
                orderRep.save(order)
            }
            processService.getProcessedDishes().toMutableList().clear()
            "Order was successfully created and saved to the database"
        }
    }

    @PostMapping("/payOrder")
    override fun payOrder(
        @RequestParam id: Int,
        @RequestParam sum: BigDecimal
    ): String {
        if (currClient == null) {
            return "No logged client"
        }

        val query = entityManager.createNativeQuery(
            "SELECT * FROM orders WHERE id = '$id'",
            Order::class.java
        )

        val resultList = query.resultList
        if (resultList.isEmpty()) {
            return "Order with this id doesn't exist"
        }

        val order = resultList.first() as Order

        if (sum < order.totalCost) {
            return "Not enough money to pay the order"
        }

        println("Check for payment is is $sum")

        return "Order was successfully paid"
    }


    @DeleteMapping("/cancelOrder")
    override fun cancelOrder(): String {
        if (currClient == null) {
            return "No logged client"
        }

        processService.abortOrderProcess(currClient!!.accountName)
        return "Order was successfully cancelled"
    }

    @PutMapping("/expandOrder")
    override suspend fun expandOrder(@RequestParam dishId: List<Int>): String {
        if (currClient == null) {
            return "No logged client"
        }

        val dish: List<Dish>
        try {
            dish = getAllDishesByIds(dishId)
        } catch (e: Exception) {
            return e.message!!
        }

        processService.appendDishesToOrder(currClient!!.accountName, dish)
        return "Order was successfully expanded"
    }

    @GetMapping("/getCurrentClient")
    override fun getCurrentClient(): Any {
        return if (currClient == null) {
            "There is no logged client"
        } else {
            currClient!!
        }
    }

    private fun getAllDishesByIds(dishIds: List<Int>): List<Dish> {
        return dishIds.map { id ->
            val query = entityManager.createNativeQuery(
                "SELECT * FROM menu WHERE id = '$id'",
                Dish::class.java
            )

            val resultList = query.resultList
            if (resultList.isEmpty()) {
                throw Exception("Dish with id $id doesn't exist")
            }

            resultList.first() as Dish
        }
    }
}
