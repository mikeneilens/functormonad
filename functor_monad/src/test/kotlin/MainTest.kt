import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf

class MainTest: StringSpec({
    "create an empty box of integer" {
        val box = Box.Empty<Int>()
        box.shouldBeInstanceOf<Box<Int>>()
        box.shouldBeInstanceOf<Box.Empty<Int>>()
        box.shouldNotBeInstanceOf<Box.Some<Int>>()
    }
    "create a box containing number 9" {
        val box = Box.Some(9)
        box.shouldBeInstanceOf<Box<Int>>()
        box.content shouldBe 9
        box.shouldNotBeInstanceOf<Box.Empty<Int>>()
    }

    "mapping a box containing a 9 to a box containing a '9' should result in a box with a string value of 9 in it" {
        val box = Box.Some(9)
        val boxContainingString = box.map(Int::toString)
        boxContainingString.shouldBeInstanceOf<Box<String>>()
        boxContainingString.shouldBeInstanceOf<Box.Some<String>>()
        boxContainingString.content shouldBe "9"
    }
    "mapping an empty box for Ints should result in an empty box for strings" {
        val box = Box.Empty<Int>()
        val boxContainingString = box.map(Int::toString)
        boxContainingString.shouldBeInstanceOf<Box<String>>()
        boxContainingString.shouldBeInstanceOf<Box.Empty<String>>()
    }

    "applying a string transform in a box to a box contaning 9 results in a box containing '9'" {
        val transformBox:Box<(Int)->String> = Box.Some { it.toString() }
        val box = Box.Some(9)
        val boxContainingString = box.apply(transformBox)
        boxContainingString.shouldBeInstanceOf<Box<String>>()
        boxContainingString.shouldBeInstanceOf<Box.Some<String>>()
        boxContainingString.content shouldBe "9"
    }

    "using apply to add two boxes together. If either box is empty, the resulting box is empty" {
        val maybeTwo = Box.Some(2)
        val maybeFive = Box.Some(5)
        val maybeNone = Box.Empty<Int>()

        val functionToAddSomething = {f:Int -> {t:Int -> f + t}} // e.g. functionToAddSomething(5)  returns {t:Int -> 5 + t}
        val result:Box<Int> = maybeTwo.apply(maybeFive.map(functionToAddSomething))
        result.shouldBeInstanceOf<Box.Some<Int>>()
        result.content shouldBe 7

        val result2:Box<Int> = maybeTwo.apply(maybeNone.map(functionToAddSomething))
        result2.shouldBeInstanceOf<Box.Empty<Int>>()

        val result3:Box<Int> = maybeNone.apply(maybeFive.map(functionToAddSomething))
        result3.shouldBeInstanceOf<Box.Empty<Int>>()

        //using pure
        val mayBeFiveCanBeAdded =  maybeFive.apply(Box.pure(functionToAddSomething))
        val result4 = maybeTwo.apply(mayBeFiveCanBeAdded)
        result4.shouldBeInstanceOf<Box.Some<Int>>()
        result4.content shouldBe 7
    }

    "Using apply on a list using List.app function" {
        fun <T,U>List<T>.app(fab: List<(T) ->U>): List<U> = fab.flatMap{ f:(T) ->U -> this.map(f)  }

        val applicatives = listOf({x:Int -> x * 2},{x:Int -> x * 3})
        val result = listOf(1,2,3).app(applicatives)
        result shouldBe listOf(2,4,6,3,6,9) // Effectively (1,2,3).map{it * 2} + (1,2,3).map{it * 3}
    }
    "flatmapping an empty box results in an empty box" {
        val box = Box.Empty<String>()

        //Tranform the contents of the box and put the result either in a box or an empty box
        fun toInt(value: String): Box<Int> = if (value.toIntOrNull() == null)  Box.Empty() else Box.Some(value.toInt())

        val flattenedBox = box.flatMap(::toInt)
        flattenedBox.shouldBeInstanceOf<Box<Int>>()
        flattenedBox.shouldBeInstanceOf<Box.Empty<Int>>()
    }

    "flatmapping a box containing a box containing '9' results in a box containing 9" {
        val box:Box<String> = Box.Some("9")

        //Tranform the contents of the box and put the result either in a box or an empty box
        fun toInt(value: String): Box<Int> = if (value.toIntOrNull() == null)  Box.Empty() else Box.Some(value.toInt())

        //using map to transform the box results in a box inside a box, so you have to take the box out of the box to get to the content
        val boxInBox = box.map(::toInt)
        boxInBox.shouldBeInstanceOf<Box<Box<String>>>()
        boxInBox.shouldBeInstanceOf<Box<Box.Some<Int>>>()
        val innerBox = (boxInBox as Box.Some<Box<Int>>).content
        (innerBox as Box.Some<Int>).content shouldBe 9

        //using flatmap to transform the box results in a box
        val boxInFlattendBox:Box<Int> = box.flatMap(::toInt)
        boxInFlattendBox.shouldBeInstanceOf<Box<Int>>()
        boxInFlattendBox.shouldBeInstanceOf<Box.Some<Int>>()
        (boxInFlattendBox as Box.Some<Int>).content shouldBe 9

        val boxContainingLetter = Box.Some("A")
        val errorInFlattendBox:Box<Int> = boxContainingLetter.flatMap(::toInt)
        errorInFlattendBox.shouldBeInstanceOf<Box.Empty<Int>>()
    }
})