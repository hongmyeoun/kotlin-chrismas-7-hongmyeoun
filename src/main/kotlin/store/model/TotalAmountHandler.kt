package store.model

class TotalAmountHandler(
    private val shoppingCart: ShoppingCart,
    private val promotionManager: PromotionManager,
    private val inventory: MutableList<Inventory>,
    private val inventoryManager: InventoryManager,
    private val promotions: List<Promotion>,
    private val receiptMachine: ReceiptMachine,
) {
    private var totalAmount = 0
    private var promotionDiscount = 0
    private var totalPurchasedQuantity = 0

    private val itemsToRemove = mutableListOf<PurchaseItem>()
    private val itemsToPromotionCheck = mutableListOf<PurchaseItem>()

    fun getTotalAmount(
        getIntentionOfPromotionFreeGoods: (String) -> Boolean,
        getIntentionOfPayRegularPrice: (String, Int) -> Boolean
    ) {
        val purchaseItems = shoppingCart.items

        purchaseItems.forEach { purchaseItem ->
            val (regularItem, promotionItem, promotion) = findItems(purchaseItem)

            if (regularItem != null && promotion == null) {
                processRegularItem(purchaseItem, regularItem)
            }

            if (promotion != null && promotionManager.checkIsInPromotionDate(promotion)) {
                processPromotionItem(regularItem, purchaseItem, promotionItem, promotion, itemsToPromotionCheck, itemsToRemove, getIntentionOfPromotionFreeGoods, getIntentionOfPayRegularPrice)
            }
        }

        finalizeShoppingCart(itemsToRemove, itemsToPromotionCheck, totalAmount, totalPurchasedQuantity, promotionDiscount)
    }

    private fun findItems(purchaseItem: PurchaseItem): Triple<Inventory?, Inventory?, Promotion?> {
        val promotionItem = inventory.find { it.name == purchaseItem.name && it.promotion.isNotEmpty() }
        val regularItem = inventory.find { it.name == purchaseItem.name && it.promotion.isEmpty() }
        val promotion = promotions.find { it.name == (promotionItem?.promotion ?: "") }
        return Triple(regularItem, promotionItem, promotion)
    }

    private fun processRegularItem(
        purchaseItem: PurchaseItem,
        regularItem: Inventory,
    ) {
        totalAmount += purchaseItem.quantity * regularItem.price
        totalPurchasedQuantity += purchaseItem.quantity
        updateInventory(regularItem, purchaseItem.quantity)
    }

    private fun processPromotionItem(
        regularItem: Inventory?,
        purchaseItem: PurchaseItem,
        promotionItem: Inventory?,
        promotion: Promotion,
        itemsToPromotionCheck: MutableList<PurchaseItem>,
        itemsToRemove: MutableList<PurchaseItem>,
        getIntentionOfPromotionFreeGoods: (String) -> Boolean,
        getIntentionOfPayRegularPrice: (String, Int) -> Boolean
    ) {
        val freeGoods = purchaseItem.quantity / promotion.buy
        val promotableQuantity = freeGoods * (promotion.buy + promotion.get)

        if (promotableQuantity <= (promotionItem?.quantity ?: 0)) {
            handleFullPromotion(regularItem, purchaseItem, promotionItem, promotion, freeGoods, itemsToPromotionCheck, getIntentionOfPromotionFreeGoods)
        } else {
            handlePartialPromotion(purchaseItem, promotionItem, promotion, itemsToRemove, getIntentionOfPayRegularPrice)
        }
    }

    private fun handleFullPromotion(
        regularItem: Inventory?,
        purchaseItem: PurchaseItem,
        promotionItem: Inventory?,
        promotion: Promotion,
        freeGoods: Int,
        itemsToPromotionCheck: MutableList<PurchaseItem>,
        getIntentionOfPromotionFreeGoods: (String) -> Boolean
    ) {
        val isNPlusOne = purchaseItem.quantity != promotion.buy
        if (isNPlusOne) {
            promotionDiscount += freeGoods * (promotionItem?.price ?: 0)
            totalAmount += purchaseItem.quantity * (promotionItem?.price ?: 0)
            totalPurchasedQuantity += purchaseItem.quantity
            itemsToPromotionCheck.add(purchaseItem)
            shoppingCart.updatePurchaseItem(purchaseItem, purchaseItem.quantity + freeGoods, freeGoods)
            updateInventory(promotionItem, purchaseItem.quantity)
        } else {
            processFreeGoods(regularItem, promotionItem, purchaseItem, freeGoods, itemsToPromotionCheck, getIntentionOfPromotionFreeGoods)
        }
    }

    private fun handlePartialPromotion(
        purchaseItem: PurchaseItem,
        promotionItem: Inventory?,
        promotion: Promotion,
        itemsToRemove: MutableList<PurchaseItem>,
        getIntentionOfPayRegularPrice: (String, Int) -> Boolean
    ) {
        val promotableQuantity = (promotionItem?.quantity ?: 0) * promotion.buy / (promotion.buy + promotion.get)
        val maxFreeGoods = promotableQuantity / promotion.buy

        val remainingPurchaseQuantity = purchaseItem.quantity - (promotableQuantity + maxFreeGoods)
        val intention = getIntentionOfPayRegularPrice(purchaseItem.name, remainingPurchaseQuantity)

        if (intention) {
            promotionDiscount += maxFreeGoods * (promotionItem?.price ?: 0)
            totalAmount += purchaseItem.quantity * (promotionItem?.price ?: 0)
            totalPurchasedQuantity += purchaseItem.quantity
            itemsToPromotionCheck.add(purchaseItem)
            shoppingCart.updatePurchaseItem(purchaseItem, purchaseItem.quantity, maxFreeGoods)
            updateInventory(promotionItem, remainingPurchaseQuantity)
        } else {
            itemsToRemove.add(purchaseItem)
        }
    }

    private fun updateInventory(item: Inventory?, quantity: Int) {
        item?.let {
            val remainingQuantity = item.quantity - quantity
            inventoryManager.updateInventory(inventory, item, remainingQuantity)
        }
    }

    private fun processFreeGoods(
        regularItem: Inventory?,
        promotionItem: Inventory?,
        purchaseItem: PurchaseItem,
        freeGoods: Int,
        itemsToPromotionCheck: MutableList<PurchaseItem>,
        getIntentionOfPromotionFreeGoods: (String) -> Boolean
    ) {
        val intention = getIntentionOfPromotionFreeGoods(purchaseItem.name)
        if (intention) {
            promotionDiscount += freeGoods * (promotionItem?.price ?: 0)
            totalAmount += (purchaseItem.quantity + freeGoods) * (promotionItem?.price ?: 0)
            totalPurchasedQuantity += purchaseItem.quantity + freeGoods
            shoppingCart.updatePurchaseItem(purchaseItem, purchaseItem.quantity + freeGoods, freeGoods)
            itemsToPromotionCheck.add(purchaseItem)
            updateInventory(promotionItem, purchaseItem.quantity + freeGoods)
        } else {
            regularItem?.let { processRegularItem(purchaseItem, regularItem) }
        }
    }

    private fun finalizeShoppingCart(
        itemsToRemove: MutableList<PurchaseItem>,
        itemsToPromotionCheck: MutableList<PurchaseItem>,
        totalAmount: Int,
        totalPurchasedQuantity: Int,
        promotionDiscount: Int
    ) {
        itemsToRemove.forEach { shoppingCart.removeItem(it) }
        itemsToPromotionCheck.forEach { shoppingCart.checkPromotion(it) }
        receiptMachine.calculateNoMembershipReceipt(totalAmount, totalPurchasedQuantity, promotionDiscount)
    }
}