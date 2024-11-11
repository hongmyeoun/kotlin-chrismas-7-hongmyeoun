package store.util.validation

import store.model.Inventory
import store.model.PurchaseItem

object InputValidator {
    fun itemsValidator(itemsInput: String, inventory: List<Inventory>) {
        validateInputFormat(itemsInput)
        validateProduct(itemsInput, inventory)
    }

    private fun validateInputFormat(itemsInput: String) {
        val regex = Regex("""^\[([가-힣a-zA-Z0-9_-]+-\d+)](,\[([가-힣a-zA-Z0-9_-]+-\d+)])*$""")

        require(regex.matches(itemsInput)) { "[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요." }
    }

    private fun validateProduct(itemsInput: String, inventory: List<Inventory>) {
        val items = parseItems(itemsInput)
        items.forEach { item ->
            val totalQuantity = inventory.filter { it.name == item.name }.sumOf { it.quantity }
            require(totalQuantity > 0) { "[ERROR] 존재하지 않는 상품입니다. 다시 입력해 주세요." }
            require(item.quantity <= totalQuantity) { "[ERROR] 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요." }
        }
    }

    private fun parseItems(input: String): List<PurchaseItem> {
        val splitInput = input.trim().removeSurrounding("[", "]").split("],[")
        val formattedItems = splitInput.map { item ->
            val parts = item.split("-")
            val name = parts[0].trim()
            val quantity = parts[1].trim().toInt()
            PurchaseItem(name, quantity)
        }
        return formattedItems
    }

    fun validateYesNoInput(input: String) {
        require(input.lowercase() == "y" || input.lowercase() == "n") { "[ERROR] 잘못된 입력입니다. 다시 입력해 주세요." }
    }
}