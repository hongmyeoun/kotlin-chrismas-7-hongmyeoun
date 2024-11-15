package store.model

class ReceiptMachine {
    var receipt = TotalAmountReceipt()
    var itemReceipt: MutableList<ItemReceipt> = mutableListOf()

    fun calculateNoMembershipReceipt(
        totalAmount: Int,
        quantity: Int,
        promotionDiscount: Int,
    ) {
        val finalAmount = totalAmount - promotionDiscount

        receipt = TotalAmountReceipt(
            totalAmount = totalAmount,
            quantity = quantity,
            promotionDiscount = promotionDiscount,
            membershipDiscount = 0,
            finalAmount = finalAmount
        )
    }

    fun updateReceiptToFinal(
        intention: Boolean,
        inventory: List<Inventory>,
        shoppingCart: ShoppingCart,
    ) {
        if (intention) {
            val membershipDiscount = calculateMembershipDiscount(inventory, shoppingCart.items)
            receipt = receipt.copy(membershipDiscount = membershipDiscount)
            val oldFinalAmount = receipt.finalAmount
            receipt = receipt.copy(finalAmount = oldFinalAmount - membershipDiscount)
        }
    }

    private fun calculateMembershipDiscount(inventory: List<Inventory>, items: List<PurchaseItem>): Int {
        val totalDiscountableAmount = items.filter { !it.isPromotion }
            .sumOf { item ->
                val matchingInventory = inventory.find { it.name == item.name }
                matchingInventory?.price?.times(item.quantity) ?: 0
            }
        val membershipDiscount = (totalDiscountableAmount * 0.3).toInt()
        val finalMembershipDiscount = membershipDiscount.coerceAtMost(8000)
        return finalMembershipDiscount
    }

    fun makeItemReceipt(inventory: MutableList<Inventory>, items: List<PurchaseItem>) {
        itemReceipt = items.mapNotNull { item ->
            val matchingInventory = inventory.find { it.name == item.name }
            matchingInventory?.let {
                ItemReceipt(
                    name = item.name,
                    quantity = item.quantity,
                    totalPrice = it.price * item.quantity,
                    freeGoodsQuantity = item.freeGoodsQuantity
                )
            }
        }.toMutableList()
    }
}

data class ItemReceipt(
    val name: String,
    val quantity: Int,
    val totalPrice: Int,
    var freeGoodsQuantity: Int = 0
)

data class TotalAmountReceipt(
    val totalAmount: Int = 0,
    val quantity: Int = 0,
    val promotionDiscount: Int = 0,
    val membershipDiscount: Int = 0,
    val finalAmount: Int = 0,
)