package store.controller

import store.model.*
import store.view.InputView
import store.view.OutputView

class StoreController(
    private val inputView: InputView = InputView(),
    private val outputView: OutputView = OutputView(),
    private val inventoryManager: InventoryManager = InventoryManager(),
    private val promotionManager: PromotionManager = PromotionManager(),
) {
    private lateinit var shoppingCart: ShoppingCart
    private lateinit var receiptMachine: ReceiptMachine
    private lateinit var totalAmountHandler: TotalAmountHandler

    private var inventory: MutableList<Inventory> = inventoryManager.loadInventoryFromFile()
    private var promotions: List<Promotion> = promotionManager.loadPromotionFromFile()

    fun run() {
        greeting()
        shopping()
        showResult()
        endOrContinue()
    }

    private fun greeting() {
        val formattedInventories = inventory.map { it.toFormattedString() }
        outputView.showGreeting(formattedInventories)
        val itemsInput = inputView.getOrder()
        shoppingCart = ShoppingCart(itemsInput)
        shoppingCart.addItem()
    }

    private fun shopping() {
        receiptMachine = ReceiptMachine()
        totalAmountHandler = TotalAmountHandler(shoppingCart, promotionManager, inventory, inventoryManager, promotions, receiptMachine)
        totalAmountHandler.getTotalAmount(
            getIntentionOfPromotionFreeGoods = { name -> inputView.getIntentionOfPromotionFreeGoods(name).stringToBoolean() },
            getIntentionOfPayRegularPrice = { name, quantity -> inputView.getIntentionOfPayRegularPrice(name, quantity).stringToBoolean() }
        )
        receiptMachine.makeItemReceipt(inventory, shoppingCart.items)
    }

    private fun showResult() {
        val intentionOfMembershipDiscount = inputView.getIntentionOfMembershipDiscount().stringToBoolean()
        receiptMachine.updateReceiptToFinal(intentionOfMembershipDiscount, inventory, shoppingCart)
        outputView.showReceipt(receiptMachine.receipt, receiptMachine.itemReceipt)
    }

    private fun endOrContinue() {
        val intentionOfContinueShopping = inputView.getIntentionOfContinueShopping().stringToBoolean()
        if (intentionOfContinueShopping) run()
        inputView.close()
    }

    private fun String.stringToBoolean(): Boolean {
        if (this == "Y") {
            return true
        } else if (this == "N") {
            return false
        }

        return false
    }
}