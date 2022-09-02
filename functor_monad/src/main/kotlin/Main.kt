
sealed class Box<T> {
    class Some<T>(val content:T):Box<T>()
    class Empty<T>:Box<T>()

    //functor is a type, that implements map function. So Box is now a functor
    fun <T,U>map(transform: (T)->U ):Box<U> = when(this) {
        is Some -> Some(transform((this as Some<T>).content))
        is Empty -> Empty()
    }

    //An applicative applies a wrapped function to a wrapped value
    fun <T,U>apply(f: Box<((T) -> U)>):Box<U> = when(f) {
            is Some -> {
                val transform = f.content
                map(transform)
            }
            is Empty -> Empty()
        }

    // Monad applies a function that returns wrapped value to a wrapped value
    fun <T,U>flatMap(f: (T) -> Box<U>): Box<U> {
        when(this) {
            is Some -> return f((this as Some<T>).content)
            else -> return Empty()
        }
    }
}


