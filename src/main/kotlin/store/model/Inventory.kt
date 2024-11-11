package store.model

import java.io.File

// 재고 관리 클래스
// 재고를 초기화
// 재고를 업데이트
class InventoryManager {

    fun loadInventoryFromFile(): MutableList<Inventory> {
        val fileRoute = getInventoryFileRoute()
        val inventory = File(fileRoute).readLines().drop(1).map { line -> parseInventory(line) }.toMutableList()
        return inventory
    }

    private fun getInventoryFileRoute(): String {
        val classLoader = Thread.currentThread().contextClassLoader
        val file = classLoader.getResource("products.md")?.file ?: throw IllegalArgumentException("[ERROR] 파일이 없습니다.")
        return file
    }

    private fun parseInventory(line: String): Inventory {
        val parts = line.split(",")
        val inventory = Inventory(
                name = parts[0].trim(),
                price = parts[1].trim().toInt(),
                quantity = parts[2].trim().toInt(),
                promotion = parts[3].trim().takeIf { it != "null" } ?: ""
            )
        return inventory
    }

    fun updateInventory(inventoryList: MutableList<Inventory>, beforeInventory: Inventory, remainingQuantity: Int) {
        val index = inventoryList.indexOf(beforeInventory)
        if (index != -1) {
            inventoryList[index] = beforeInventory.copy(quantity = remainingQuantity)
        }
    }

}

data class Inventory(
    val name: String,
    val price: Int,
    var quantity: Int,
    val promotion: String
) {
    fun toFormattedString(): String {
        val formattedString = buildString {
            append("- $name")
            append(" %,d원".format(price))
            if (quantity == 0) append(" 재고 없음") else append(" ${quantity}개")
            if (promotion.isNotEmpty()) append(" $promotion")
        }
        return formattedString
    }
}