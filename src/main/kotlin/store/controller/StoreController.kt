package store.controller

import store.model.Inventory
import store.model.InventoryManager
import store.view.InputView
import store.view.OutputView

class StoreController(
    private val inputView: InputView = InputView(),
    private val outputView: OutputView = OutputView(),
    private val inventoryManager: InventoryManager = InventoryManager()
) {
    private lateinit var items: List<PurchaseItem>
    private var intentionOfPromotionFreeGoods: Boolean = false
    private var intentionOfPayRegularPrice: Boolean = false
    private var intentionOfMembershipDiscount: Boolean = false
    private var intentionOfContinueShopping: Boolean = false

    fun run() {
        greeting()
        if (false) {
            val input = inputView.getIntentionOfPromotionFreeGoods()
            intentionOfPromotionFreeGoods = stringToBoolean(input)
        }

        if (false) {
            val input = inputView.getIntentionOfPayRegularPrice()
            intentionOfPayRegularPrice = stringToBoolean(input)
        }

        val inputMembership = inputView.getIntentionOfMembershipDiscount()
        intentionOfMembershipDiscount = stringToBoolean(inputMembership)

        outputView.showReceipt()

        val inputContinue = inputView.getIntentionOfContinueShopping()
        intentionOfContinueShopping = stringToBoolean(inputContinue)

        if (intentionOfContinueShopping) {
            intentionOfPromotionFreeGoods = false
            intentionOfPayRegularPrice = false
            intentionOfMembershipDiscount = false
            intentionOfContinueShopping = false
            run()
        } else {
            inputView.close()
        }

    }

    fun greeting() {
        val inventories = inventoryManager.loadInventoryFromFile()
        val formattedInventories = inventories.map { it.toFormattedString() }
        outputView.showGreeting(formattedInventories)
        val itemsInput = inputView.getOrder()
        items = formatItems(itemsInput)

    }

    fun formatItems(input: String): List<PurchaseItem> {
        // 괄호를 다 없엔뒤 리스트로 넣는 방식으로 선택
        val splitInput = input.trim().removeSurrounding("[", "]").split("],[")
        val formattedItems = splitInput.map { item ->
            val parts = item.split("-")
            val name = parts[0].trim()
            val quantity = parts[1].trim().toInt()
            PurchaseItem(name, quantity)
        }
        return formattedItems
    }

    fun stringToBoolean(input: String): Boolean {
        if (input == "Y") {
            return true
        } else if (input == "N") {
            return false
        }

        return false
    }
}

data class PurchaseItem(
    val name: String,
    val quantity: Int
)