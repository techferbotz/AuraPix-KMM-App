package com.ferbotz.aurapix.billing.data

import com.ferbotz.aurapix.billing.data.dto.BillingResultDto
import com.ferbotz.aurapix.core.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Fast-path purchase verification — called right after a RevenueCat purchase so balances update
 * immediately instead of waiting for the webhook. Idempotent. (`/billing/sync` is intentionally
 * not wired yet.)
 */
class BillingRemoteDataSource(private val client: HttpClient) {

    /** §4.15 — confirm a subscription purchase with the backend. */
    suspend fun verifySubscription(productId: String, transactionId: String?): Result<BillingResultDto> =
        safeApiCall {
            client.post("billing/subscription/verify") {
                contentType(ContentType.Application.Json)
                setBody(buildMap {
                    put("productId", productId)
                    if (transactionId != null) put("transactionId", transactionId)
                })
            }
        }

    /** §4.16 — confirm a credit-pack purchase with the backend. */
    suspend fun verifyPurchase(transactionId: String, productId: String): Result<BillingResultDto> =
        safeApiCall {
            client.post("billing/purchase/verify") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("transactionId" to transactionId, "productId" to productId))
            }
        }
}
