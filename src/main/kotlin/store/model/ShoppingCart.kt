package store.model

class ShoppingCart(private val order: String) {
    var items: MutableList<PurchaseItem> = mutableListOf()

    fun addItem() {
        items = formatItems(order)
    }

    fun removeItem(item: PurchaseItem) {
        items.removeIf { it.name == item.name }
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
}

data class PurchaseItem(
    val name: String,
    val quantity: Int
)