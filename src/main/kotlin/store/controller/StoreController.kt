package store.controller

import store.model.*
import store.util.validation.InputValidator
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
        val itemsInput = getValidatedInput(
            inputFunction = { inputView.getOrder() },
            validationFunction = { InputValidator.itemsValidator(it, inventory) }
        )
        shoppingCart = ShoppingCart(itemsInput)
        shoppingCart.addItem()
    }

    private fun shopping() {
        receiptMachine = ReceiptMachine()
        totalAmountHandler = TotalAmountHandler(shoppingCart, promotionManager, inventory, inventoryManager, promotions, receiptMachine)
        totalAmountHandler.getTotalAmount(
            getIntentionOfPromotionFreeGoods = { name ->
                getValidatedInput(
                    inputFunction = { inputView.getIntentionOfPromotionFreeGoods(name) },
                    validationFunction = { InputValidator.validateYesNoInput(it) }
                ).stringToBoolean()
            },
            getIntentionOfPayRegularPrice = { name, quantity ->
                getValidatedInput(
                    inputFunction = { inputView.getIntentionOfPayRegularPrice(name, quantity) },
                    validationFunction = { InputValidator.validateYesNoInput(it) }
                ).stringToBoolean()
            }
        )
        receiptMachine.makeItemReceipt(inventory, shoppingCart.items)
    }

    private fun showResult() {
        val intentionOfMembershipDiscount = getValidatedInput(
            inputFunction = { inputView.getIntentionOfMembershipDiscount() },
            validationFunction = { InputValidator.validateYesNoInput(it) }
        ).stringToBoolean()
        receiptMachine.updateReceiptToFinal(intentionOfMembershipDiscount, inventory, shoppingCart)
        outputView.showReceipt(receiptMachine.receipt, receiptMachine.itemReceipt)
    }

    private fun endOrContinue() {
        val intentionOfContinueShopping = getValidatedInput(
            inputFunction = { inputView.getIntentionOfContinueShopping() },
            validationFunction = { InputValidator.validateYesNoInput(it) }
        ).stringToBoolean()
        if (intentionOfContinueShopping) run()
        inputView.close()
    }

    private fun String.stringToBoolean(): Boolean {
        if (this.lowercase() == "y") {
            return true
        } else if (this.lowercase() == "n") {
            return false
        }

        return false
    }

    private fun getValidatedInput(
        inputFunction: () -> String,
        validationFunction: (String) -> Unit
    ): String {
        while (true) {
            try {
                val input = inputFunction()
                validationFunction(input)
                return input
            } catch (e: IllegalArgumentException) {
                outputView.showErrorMessage(e.message)
            }
        }
    }
}