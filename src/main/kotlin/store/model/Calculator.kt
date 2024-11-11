package store.model

object Calculator {
    fun calculateMembershipDiscount(inventory: List<Inventory>, items: List<PurchaseItem>): Int {
        val totalDiscountableAmount = items.filter { !it.isPromotion }
            .sumOf { item ->
                val matchingInventory = inventory.find { it.name == item.name }
                matchingInventory?.price?.times(item.quantity) ?: 0
            }
        val membershipDiscount = (totalDiscountableAmount * 0.3).toInt()
        val finalMembershipDiscount = membershipDiscount.coerceAtMost(8000)
        return finalMembershipDiscount
    }
}