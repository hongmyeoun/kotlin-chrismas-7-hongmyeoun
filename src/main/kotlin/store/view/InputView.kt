package store.view

import camp.nextstep.edu.missionutils.Console

class InputView {
    private fun getInput(message: String): String {
        println(message)
        val input = Console.readLine()
        return input
    }

    fun close() = Console.close()

    fun getOrder(): String {
        val order = getInput(ORDER_MESSAGE)
        return order
    }

    fun getIntentionOfPromotionFreeGoods(name: String): String {
        val intentionOfPromotionFreeGoods = getInput(PROMOTION_FREE_GOODS_NOTION.format(name))
        return intentionOfPromotionFreeGoods
    }

    fun getIntentionOfPayRegularPrice(name: String, quantity: Int): String {
        val intentionOfPayRegularPrice = getInput(NO_PROMOTION_PAY_REGULAR_PRICE.format(name, quantity))
        return intentionOfPayRegularPrice
    }

    fun getIntentionOfMembershipDiscount(): String {
        val intentionOfMembershipDiscount = getInput(MEMBERSHIP_DISCOUNT)
        return intentionOfMembershipDiscount
    }

    fun getIntentionOfContinueShopping(): String {
        val intentionOfContinueShopping = getInput(CONTINUE_SHOPPING)
        return intentionOfContinueShopping
    }

    companion object {
        const val ORDER_MESSAGE = "\n구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])"
        const val PROMOTION_FREE_GOODS_NOTION = "\n현재 %s은(는) 1개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)"
        const val NO_PROMOTION_PAY_REGULAR_PRICE = "\n현재 %s %s개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)"
        const val MEMBERSHIP_DISCOUNT = "\n멤버십 할인을 받으시겠습니까? (Y/N)"
        const val CONTINUE_SHOPPING = "\n감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)"
    }
}