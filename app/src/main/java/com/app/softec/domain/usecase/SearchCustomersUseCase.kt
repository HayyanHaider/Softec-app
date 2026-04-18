package com.app.softec.domain.usecase

import com.app.softec.data.repository.CustomerRepository
import com.app.softec.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for searching and filtering the customer list.
 * Matches the search query against the customer's name, contact number, and email.
 */
class SearchCustomersUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {

    /**
     * Retrieves a reactive stream of customers filtered by the provided query.
     *
     * @param userId The ID of the current authenticated user.
     * @param query The search string entered by the user.
     * @return A Flow emitting the filtered list of [Customer].
     */
    operator fun invoke(userId: String, query: String): Flow<List<Customer>> {
        val customerFlow = customerRepository.getAllCustomers(userId)

        return customerFlow.map { customers ->
            if (query.isBlank()) {
                // If the search bar is empty, return the full list
                customers
            } else {
                val lowercaseQuery = query.lowercase().trim()
                
                // Filter the list based on matches in name, phone, or email
                customers.filter { customer ->
                    customer.customerName.lowercase().contains(lowercaseQuery) ||
                    customer.contactNumber.contains(lowercaseQuery) ||
                    (customer.email?.lowercase()?.contains(lowercaseQuery) == true)
                }
            }
        }
    }
}