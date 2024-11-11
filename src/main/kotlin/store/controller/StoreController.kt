package store.controller

import store.model.*
import store.view.InputView
import store.view.OutputView

class StoreController(
    private val inputView: InputView = InputView(),
    private val outputView: OutputView = OutputView(),
    private val inventoryManager: InventoryManager = InventoryManager(),
    private val promotionManager: PromotionManager = PromotionManager()
) {
    private lateinit var shoppingCart: ShoppingCart

    private var inventory: MutableList<Inventory> = inventoryManager.loadInventoryFromFile()
    private var itemReceipt: MutableList<ItemReceipt> = mutableListOf()

    private var promotions: List<Promotion> = promotionManager.loadPromotionFromFile()
    private var intentionOfPromotionFreeGoods: Boolean = false
    private var intentionOfPayRegularPrice: Boolean = false
    private var intentionOfMembershipDiscount: Boolean = false
    private var intentionOfContinueShopping: Boolean = false

    fun run() {
        greeting()

        var totalAmountReceipt = getTotalAmount()

        intentionOfMembershipDiscount = inputView.getIntentionOfMembershipDiscount().stringToBoolean()
        if (intentionOfMembershipDiscount) {
            val membershipDiscount = Calculator.calculateMembershipDiscount(inventory, shoppingCart.items)
            totalAmountReceipt = totalAmountReceipt.copy(membershipDiscount = membershipDiscount)

            val oldFinalAmount = totalAmountReceipt.finalAmount
            totalAmountReceipt = totalAmountReceipt.copy(finalAmount = oldFinalAmount - membershipDiscount)
        }

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
        shoppingCart = ShoppingCart(itemsInput)
        shoppingCart.addItem()
    }

    private fun showResult(totalAmountReceipt: TotalAmountReceipt) {
        outputView.showReceipt(totalAmountReceipt, itemReceipt)
    }

    private fun getTotalAmount(): TotalAmountReceipt {
        var totalAmount = 0
        var promotionDiscount = 0
        var totalPurchasedQuantity = 0

        val purchaseItems = shoppingCart.items
        val itemsToRemove = mutableListOf<PurchaseItem>()
        val itemsToPromotionCheck = mutableListOf<PurchaseItem>()

        purchaseItems.forEach { purchaseItem ->
            val promotionItem = inventory.find { it.name == purchaseItem.name && it.promotion.isNotEmpty() }
            val regularItem = inventory.find { it.name == purchaseItem.name && it.promotion.isEmpty() }
            val promotion = promotions.find { it.name == (promotionItem?.promotion ?: "") }

            if (regularItem != null && promotion == null) {
                // 총 구매액
                totalAmount = totalAmount + purchaseItem.quantity * regularItem.price

                // 총 수량
                totalPurchasedQuantity = totalPurchasedQuantity + purchaseItem.quantity

                // 재고 관리
                val remainingRegularQuantity = regularItem.quantity - purchaseItem.quantity
                inventoryManager.updateInventory(inventory, regularItem, remainingRegularQuantity)
            }

            if (promotion != null && promotionManager.checkIsInPromotionDate(promotion)) {
                if (promotionItem != null && promotionItem.quantity > 0) {

                    val freeGoods = purchaseItem.quantity / promotion.buy
                    val promotableQuantity = freeGoods * (promotion.buy + promotion.get)

                    // 프로모션 재고가 남아서 판매 가능 할 때
                    if (promotableQuantity <= promotionItem.quantity) {

                        // 최소 시작만 확인, 즉 1+1인데 3개부터는 알림 X 이미 혜택을 받음
                        val isNPlusOne = purchaseItem.quantity != promotion.buy
                        if (isNPlusOne) {
                            // 프로모션으로 할인된 가격
                            promotionDiscount = promotionDiscount + freeGoods * promotionItem.price

                            // 총 구매액
                            totalAmount = totalAmount + purchaseItem.quantity * promotionItem.price

                            // 총 수량
                            totalPurchasedQuantity = totalPurchasedQuantity + purchaseItem.quantity

                            // 쇼핑카트 프로모션 체크
                            itemsToPromotionCheck.add(purchaseItem)

                            // 재고 관리
                            val remainingQuantity = promotionItem.quantity - purchaseItem.quantity
                            inventoryManager.updateInventory(inventory, promotionItem, remainingQuantity)
                        } else {
                            val intention = inputView.getIntentionOfPromotionFreeGoods(purchaseItem.name).stringToBoolean()
                            if (intention) {
                                // 프로모션으로 할인된 가격
                                promotionDiscount = promotionDiscount + freeGoods * promotionItem.price

                                // 총 구매액
                                totalAmount = totalAmount + (purchaseItem.quantity + freeGoods) * promotionItem.price

                                // 총 수량
                                totalPurchasedQuantity = totalPurchasedQuantity + purchaseItem.quantity + freeGoods

                                // 쇼핑카트 프로모션 체크
                                itemsToPromotionCheck.add(purchaseItem)

                                // 재고 관리
                                val remainingQuantity = promotionItem.quantity - (purchaseItem.quantity + freeGoods)
                                inventoryManager.updateInventory(inventory, promotionItem, remainingQuantity)
                            } else {
                                regularItem?.let {
                                    // 총 구매액
                                    totalAmount = totalAmount + purchaseItem.quantity * regularItem.price

                                    // 총 수량
                                    totalPurchasedQuantity = totalPurchasedQuantity + purchaseItem.quantity

                                    // 재고 관리
                                    val remainingQuantity = regularItem.quantity - purchaseItem.quantity
                                    inventoryManager.updateInventory(inventory, regularItem, remainingQuantity)
                                }
                            }
                        }
                    }

                    // 프로모션 재고가 부족해 일부 수량을 혜택 없이 결제해야 하는 경우
                    // 콜라 11 -> 프로모션 재고는 10이므로 재고 부족, 최대 6 + 3 혜택,
                    // 나머지 2개는 혜택 없이 결제
                    // 대신 문제에서 보여준 결과는 더이상 프로모션 혜택이 없을때 프로모션 재고를 다 털어버림(이 경우)
                    if (promotableQuantity > promotionItem.quantity) {
                        // 콜라 10 -> 10 * 2 / 3 = 6
                        // 콜라 7 -> 7 * 2 / 3 = 4
                        val maxPromotableQuantity = (promotionItem.quantity * promotion.buy) / (promotion.buy + promotion.get)
                        val maxFreeGoods = maxPromotableQuantity / promotion.buy
                        // 콜라 7,10 -> 10 - ((7*2/3) - 2) =
                        val remainingPurchaseQuantity = purchaseItem.quantity - (maxPromotableQuantity + maxFreeGoods)
                        val intention = inputView.getIntentionOfPayRegularPrice(purchaseItem.name, remainingPurchaseQuantity).stringToBoolean()

                        if (intention) {
                            // 프로모션으로 할인된 가격
                            promotionDiscount = promotionDiscount + maxFreeGoods * promotionItem.price

                            // 총 구매액
                            totalAmount = totalAmount + purchaseItem.quantity * promotionItem.price

                            // 총 수량
                            totalPurchasedQuantity = totalPurchasedQuantity + purchaseItem.quantity

                            // 쇼핑카트 프로모션 체크
                            itemsToPromotionCheck.add(purchaseItem)

                            // 재고 관리
                            var remainingPromotionQuantity = promotionItem.quantity - (maxPromotableQuantity + maxFreeGoods)
                            regularItem?.let {
                                val remainingRegularQuantity =
                                    regularItem.quantity - remainingPurchaseQuantity + remainingPromotionQuantity
                                inventoryManager.updateInventory(inventory, regularItem, remainingRegularQuantity)
                                remainingPromotionQuantity = 0 // 프로모션 재고 다 턺
                            }
                            inventoryManager.updateInventory(inventory, promotionItem, remainingPromotionQuantity)
                        } else {
                            itemsToRemove.add(purchaseItem)
                        }
                    }
                }
            }
        }

        itemsToRemove.forEach { shoppingCart.removeItem(it) }
        itemsToPromotionCheck.forEach { shoppingCart.checkPromotion(it) }

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