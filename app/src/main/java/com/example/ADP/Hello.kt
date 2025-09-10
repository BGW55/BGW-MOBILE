package com.example.adp
fun sum(no: Int): Int {
    var total = 0
    for (i in 1..no) {
        total += i
    }
    return total
}

fun add(a: Int, b: Int): Int {
    return a + b
}

// Start
fun main() {
    println("Hello, world!!!")
    print("Hello world")
    println("Hello world")

    val name = "Geon Woo"
    println("my name = $name")
    println("my name = " + name)

    val num = 10
    println("nnum = $num")
    println("num = " + num)

    val name1: String = "Bae"
    println("name1 : $name1, sum : ${sum(10)}, plus : ${10 + 20}")

    val result = add(a = 10, b = 20)
    println("result = $result") // 결과: result = 30

    println("1부터 5까지:")
    for (i in 1..5) {
        println(i)
    }

    // `until`은 마지막 숫자를 포함하지 않으므로 (1, 2, 3, 4)가 출력됩니다.
    println("\n1부터 5 이전까지:")
    for (i in 1 until 5) {
        println(i)
    }

    val fruits = listOf("사과", "바나나", "오렌지")
    println("\n과일 목록 (인덱스 포함):")

    for ((index, fruit) in fruits.withIndex()) {
        println("$index: $fruit")
    }
    // 람다 함수
    fun main() {
        val sub = { a: Int, b: Int -> a - b }
        val result = sub(10, 20)
        println("result = $result")
    }
}
