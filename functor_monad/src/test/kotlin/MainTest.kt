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
        val transformBox:Box<(Int)->String> = Box.Some(Int::toString)
        val box = Box.Some(9)
        val boxContainingString = box.apply(transformBox)
        boxContainingString.shouldBeInstanceOf<Box<String>>()
        boxContainingString.shouldBeInstanceOf<Box.Some<String>>()
        boxContainingString.content shouldBe "9"
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