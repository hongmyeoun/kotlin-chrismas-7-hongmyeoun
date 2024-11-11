package store.controller

import store.model.Inventory
import store.model.InventoryManager
import store.model.Promotion
import store.model.PromotionManager
import store.view.InputView
import store.view.OutputView

class StoreController(
    private val inputView: InputView = InputView(),
    private val outputView: OutputView = OutputView(),
    private val inventoryManager: InventoryManager = InventoryManager(),
    private val promotionManager: PromotionManager = PromotionManager()
) {
    private lateinit var purchaseItems: List<PurchaseItem>
    private var inventory: MutableList<Inventory> = inventoryManager.loadInventoryFromFile()
    private var itemReceipt: MutableList<ItemReceipt> = mutableListOf()

    private var promotions: List<Promotion> = promotionManager.loadPromotionFromFile()
    private var intentionOfPromotionFreeGoods: Boolean = false
    private var intentionOfPayRegularPrice: Boolean = false
    private var intentionOfMembershipDiscount: Boolean = false
    private var intentionOfContinueShopping: Boolean = false

    fun run() {
        greeting()

        val totalAmountReceipt = getTotalAmount3()

        intentionOfMembershipDiscount = inputView.getIntentionOfMembershipDiscount().stringToBoolean()

        showResult(totalAmountReceipt)

        intentionOfContinueShopping = inputView.getIntentionOfContinueShopping().stringToBoolean()

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

    private fun greeting() {
        val formattedInventories = inventory.map { it.toFormattedString() }
        outputView.showGreeting(formattedInventories)
        val itemsInput = inputView.getOrder()
        purchaseItems = formatItems(itemsInput)
    }

    private fun formatItems(input: String): List<PurchaseItem> {
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

    private fun showResult(totalAmountReceipt: TotalAmountReceipt) {
        outputView.showReceipt(totalAmountReceipt, itemReceipt)
    }

    private fun getTotalAmount3(): TotalAmountReceipt {
        var totalAmount = 0
        var promotionDiscount = 0
        var totalPurchasedQuantity = 0

        purchaseItems.forEach { purchaseItem ->
            val promotionItem = inventory.find { it.name == purchaseItem.name && it.promotion.isNotEmpty() }
            val regularItem = inventory.find { it.name == purchaseItem.name && it.promotion.isEmpty() }
            val promotion = promotions.find { it.name == (promotionItem?.promotion ?: "") }

            var remainingPurchaseItem = purchaseItem.quantity

            if (promotion != null && promotionManager.checkIsInPromotionDate(promotion)) {
                if (promotionItem != null) {
                    // 콜라 4개 -> 2*3 = 6개
                    val promotableQuantity = (purchaseItem.quantity / promotion.buy) * (promotion.buy + promotion.get)
                    // 프로모션 재고가 남아서 판매 가능 할 때
                    // 10개, 4개를 입력 -> 2개를 무료로 더 받을 수 있습니다를 출력
                    // 6개를 입력 -> 3개를 무료로 더 받을 수 있습니다를 출력
                    if (promotableQuantity <= promotionItem.quantity) {
                        val freeGoods = purchaseItem.quantity / promotion.buy
                        val isFreeGoodsNotion = (purchaseItem.quantity + freeGoods) <= promotionItem.quantity
                        if (isFreeGoodsNotion) {
                            val intentionInput = inputView.getIntentionOfPromotionFreeGoods(purchaseItem.name, freeGoods)
                            val intention = intentionInput.stringToBoolean()
                            if (intention) {
                                // 프로모션으로 할인된 가격
                                promotionDiscount = promotionDiscount + freeGoods * promotionItem.price

                                // 총 구매액
                                totalAmount = totalAmount + purchaseItem.quantity * promotionItem.price

                                // 총 수량
                                totalPurchasedQuantity = totalPurchasedQuantity + purchaseItem.quantity + freeGoods

                                // 남는 내 구매 상품
                                remainingPurchaseItem = remainingPurchaseItem - promotableQuantity

                                // 재고 관리
                                val remainingQuantity = promotionItem.quantity - promotableQuantity
                                inventoryManager.updateInventory(inventory, promotionItem, remainingQuantity)
                            } else {
                                regularItem?.let {
                                    // 총 구매액
                                    totalAmount = totalAmount + purchaseItem.quantity * regularItem.price

                                    // 총 수량
                                    totalPurchasedQuantity = totalPurchasedQuantity + purchaseItem.quantity

                                    // 남는 내 구매 상품
                                    remainingPurchaseItem = remainingPurchaseItem - purchaseItem.quantity

                                    // 재고 관리
                                    val remainingQuantity = regularItem.quantity - purchaseItem.quantity
                                    inventoryManager.updateInventory(inventory, regularItem, remainingQuantity)
                                }
                            }
                        }
                    }
                }
            }
        }


        val finalAmount = totalAmount - promotionDiscount

        val totalAmountReceipt = TotalAmountReceipt(
            totalAmount = totalAmount,
            quantity = totalPurchasedQuantity,
            promotionDiscount = promotionDiscount,
            membershipDiscount = 0,
            finalAmount = finalAmount
        )

        return totalAmountReceipt
    }

    fun String.stringToBoolean(): Boolean {
        if (this == "Y") {
            return true
        } else if (this == "N") {
            return false
        }

        return false
    }

}

data class PurchaseItem(
    val name: String,
    val quantity: Int
)

data class ItemReceipt(
    val name: String,
    val quantity: Int,
    val price: Int
)

data class TotalAmountReceipt(
    val totalAmount: Int,
    val quantity: Int,
    val promotionDiscount: Int,
    val membershipDiscount: Int,
    val finalAmount: Int,
)