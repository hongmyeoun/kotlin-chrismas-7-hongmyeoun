package store.model

class ShoppingCart(private val order: String) {
    var items: MutableList<PurchaseItem> = mutableListOf()

    fun addItem() {
        items = formatItems(order)
    }

    fun removeItem(item: PurchaseItem) {
        items.removeIf { it.name == item.name }
    }

    fun checkPromotion(item: PurchaseItem) {
        item.isPromotion = true
    }

    private fun formatItems(input: String): MutableList<PurchaseItem> {
        // 괄호를 다 없엔뒤 리스트로 넣는 방식으로 선택
        val splitInput = input.trim().removeSurrounding("[", "]").split("],[")
        val formattedItems = splitInput.map { item ->
            val parts = item.split("-")
            val name = parts[0].trim()
            val quantity = parts[1].trim().toInt()
            PurchaseItem(name, quantity)
        }.toMutableList()
        return formattedItems
    }

    fun updatePurchaseItem(purchaseItem: PurchaseItem, quantity: Int, freeGoodsQuantity: Int) {
        items.find { it.name == purchaseItem.name }?.let {
            it.quantity = quantity
            it.freeGoodsQuantity = freeGoodsQuantity
        }
    }
}

data class PurchaseItem(
    val name: String,
    var quantity: Int,
    var isPromotion: Boolean = false,
    var freeGoodsQuantity: Int = 0
)