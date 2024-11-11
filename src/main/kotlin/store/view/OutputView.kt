package store.view

import store.model.ItemReceipt
import store.model.TotalAmountReceipt

class OutputView {
    fun showGreeting(inventories: List<String>) {
        println("안녕하세요. W편의점입니다.")
        println("현재 보유하고 있는 상품입니다.\n")
        showInventory(inventories)
    }

    private fun showInventory(inventories: List<String>) {
        inventories.forEach { println(it) }
    }

    fun showReceipt(totalAmount: TotalAmountReceipt, itemReceipt: List<ItemReceipt>) {
        println("==============W 편의점================")
        println("상품명              수량        금액")
        itemReceipt.forEach { nameQuantityPrice(it) }
        println("=============증     정===============")
        println("====================================")
        finalAmount(totalAmount)
    }

    private fun nameQuantityPrice(item: ItemReceipt) {
        println("${item.name}              ${item.quantity}        ${item.totalPrice}")
    }

    private fun finalAmount(totalAmount: TotalAmountReceipt) {
        println("총구매액              ${totalAmount.quantity}        ${totalAmount.totalAmount}")
        println("행사할인                ${totalAmount.promotionDiscount}")
        println("멤버십할인                ${totalAmount.membershipDiscount}")
        println("내실돈                ${totalAmount.finalAmount}")
    }
}